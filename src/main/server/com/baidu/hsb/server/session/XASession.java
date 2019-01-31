/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.server.session;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.mysql.xa.XAOp;
import com.baidu.hsb.route.RouteResultsetNode;
import com.baidu.hsb.server.ServerConnection;

/**
 * 保存dtm信息 xa当前事务的上下文
 * 
 * @author brucexx
 *
 */
public class XASession implements Observer {
    private static final Logger LOGGER = Logger.getLogger(XASession.class);

    private String xid;
    private Set<RouteResultsetNode> shardingRecord = null;
    private Map<Integer, Set<RouteResultsetNode>> op = null;

    private ServerConnection c = null;
    private Boolean xaExecutedSuc = null;
    private XAOp status = null;
    private AtomicBoolean inXa = new AtomicBoolean(false);

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
    public void setInXa(boolean inXa) {
        this.inXa.set(inXa);
    }

    private void genXid() {
        // macAdrr+timeline
        this.xid = HeisenbergServer.getInstance().getHostKey() + System.currentTimeMillis();
    }

    public String getXid() {
        return xid;
    }

    public void init() {
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

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        // 直接返回结果
        String[] result = (String[]) arg;
        //
        int code = NumberUtils.toInt(result[1]);
        boolean suc = BooleanUtils.toBoolean(result[0]);
        xaExecutedSuc = suc;
    }

    public void clearResult() {
        xaExecutedSuc = null;
    }

    public Boolean getResult() {
        return xaExecutedSuc;
    }

}
