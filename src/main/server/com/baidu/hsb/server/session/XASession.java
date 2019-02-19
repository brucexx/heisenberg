/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.server.session;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.baidu.hsb.HeisenbergConfig;
import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.mysql.bio.Channel;
import com.baidu.hsb.mysql.bio.MySQLChannel;
import com.baidu.hsb.mysql.bio.executor.XAUtil;
import com.baidu.hsb.mysql.xa.XAOp;
import com.baidu.hsb.route.RouteResultsetNode;
import com.baidu.hsb.server.ServerConnection;

/**
 * 保存dtm信息 xa当前事务的上下文
 * 
 * @author brucexx
 *
 */
public class XASession {
	private static final Logger LOGGER = Logger.getLogger(XASession.class);

	private String xid;
	private Set<RouteResultsetNode> shardingRecord = null;
	private Map<Integer, Set<RouteResultsetNode>> op = null;

	private ServerConnection c = null;
	private XAOp status = null;
	private AtomicBoolean inXa = new AtomicBoolean(false);

	/** 由于xa必须要物理连接复用性，这里使用复用性 **/
	private Map<String, Channel> xaPool = new ConcurrentHashMap<String, Channel>();

	/** key为 sql+ addr **/
	private Set<String> exeRecord = Collections.synchronizedSet(new HashSet<String>());

	/** key为channel id **/
	private Map<String, String> xaStartConn = new ConcurrentHashMap<String, String>();

	/**
	 * 
	 * @param rn
	 * @param i
	 * @return
	 * @throws Exception
	 */
	public Channel getChannel(RouteResultsetNode rn, int i) throws Exception {
		HeisenbergConfig conf = HeisenbergServer.getInstance().getConfig();
		String name = rn.getName().indexOf("#") > 0 ? rn.getName().substring(0, rn.getName().indexOf("#"))
				: rn.getName();
		final MySQLDataNode dn = conf.getDataNodes().get(name);
		if (dn == null) {
			throw new RuntimeException("no datanode found! ");
		}
		String addr = dn.getSource().getConfig().getHost() + ":" + dn.getSource().getConfig().getPort();
		if (xaPool.get(addr) == null) {
			xaPool.put(addr, i == -1 ? dn.getChannel() : dn.getChannel(i));
		}
		return xaPool.get(addr);
	}

	/**
	 * 是否可重复执行，底层不允许一个物理机多次执行xa 操作，如有，会阻塞后续事务
	 * 
	 * 1.先findConn 2.再判断canRepeat
	 * 
	 * @param cc
	 * @param sql
	 * @return
	 */
	public boolean canRepeat(Channel cc, String sql) {
		// 记录dn的物理执行
		if (XAUtil.isXAOp(sql)) {
			MySQLChannel c = (MySQLChannel) cc;
			String k1 = c.getDataSource().getConfig().getHost() + ":" + c.getDataSource().getConfig().getPort();
			if (exeRecord.contains(k1 + "_" + sql)) {
				return false;
			} else {
				exeRecord.add(k1 + "_" + sql);
			}
		}
		return true;
	}

	public boolean xaMustReuse(Channel cc, String sql, int op) {
		MySQLChannel c = (MySQLChannel) cc;
		if (xaStartConn.containsKey(c.getId())) {
			return true;
		}
		return false;
	}

	/**
	 * 只用于buf op=0的连接
	 * 
	 * @param cc
	 * @param sql
	 * @return
	 */
	public void bufConn(Channel cc, String sql, int op) {
		MySQLChannel c = (MySQLChannel) cc;
		// xa start
		if (op == 0 && XAUtil.isXAOp(sql)) {
			xaStartConn.put(c.getId(), sql);
		}
	}

	public XASession(ServerConnection c) {
		this.c = c;
		shardingRecord = new TreeSet<RouteResultsetNode>();
		op = new ConcurrentHashMap<Integer, Set<RouteResultsetNode>>();
		for (XAOp o : XAOp.values()) {
			op.put(o.getCode(), new TreeSet<RouteResultsetNode>());
		}

		genXid();
	}

	/**
	 * @return the inXa
	 */
	public boolean isInXa() {
		return inXa.get();
	}

	/**
	 * @param inXa the inXa to set
	 */
	public void start() {
		this.inXa.set(true);
	}

	public void release() {
		this.inXa.set(false);
	}

	private void genXid() {
		// macAdrr+timeline
		this.xid = "HSB_XA_" + HeisenbergServer.getInstance().getHostKey() + System.currentTimeMillis();
	}

	public String getXid() {
		return xid;
	}

	public void init() {
		xaStartConn.clear();
		exeRecord.clear();
		shardingRecord.clear();
		genXid();
	}

	/**
	 * 如果1399方案不能解决，采用sql执行记录式
	 * 
	 * @param o
	 * @param rrn
	 */
	public void recordXaExecute(XAOp o, RouteResultsetNode[] rrn) {
		Set<RouteResultsetNode> rnSet = new TreeSet<RouteResultsetNode>();
		for (RouteResultsetNode rn : rrn) {
			rnSet.add(rn);
		}
		op.get(o).addAll(rnSet);
	}

	/**
	 * 记录分片
	 */
	public void record(String sql, RouteResultsetNode[] rrn) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("record-->" + sql + ",rrn->" + rrn);
		}
		synchronized (shardingRecord) {
			for (RouteResultsetNode rn : rrn) {
				shardingRecord.add(rn);
			}
		}
	}

	// 将已记录的sql对应的分片存储至redis
	public void saveStore(String status) {
		// TODO
	}

	public void delKey() {
		// TODO
	}

	public Set<RouteResultsetNode> getRecords() {
		return shardingRecord;
	}

	/**
	 * @return the status
	 */
	public XAOp getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(XAOp status) {
		this.status = status;
	}

}
