/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server;

import java.io.EOFException;
import java.nio.channels.SocketChannel;
import java.sql.SQLNonTransientException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.mysql.xa.XAOp;
import com.baidu.hsb.net.FrontendConnection;
import com.baidu.hsb.route.HServerRouter;
import com.baidu.hsb.route.RouteResultset;
import com.baidu.hsb.route.RouteResultsetNode;
import com.baidu.hsb.server.parser.ServerParse;
import com.baidu.hsb.server.response.Heartbeat;
import com.baidu.hsb.server.response.Ping;
import com.baidu.hsb.server.session.BlockingSession;
import com.baidu.hsb.server.session.NonBlockingSession;
import com.baidu.hsb.server.session.Session;
import com.baidu.hsb.server.session.XASession;
import com.baidu.hsb.util.TimeUtil;

/**
 * @author xiongzhao@baidu.com 2011-4-21 上午11:22:57
 */
public class ServerConnection extends FrontendConnection {
	private static final Logger LOGGER = Logger.getLogger(ServerConnection.class);

	private static final Logger routeLogger = Logger.getLogger("route-digest");

	private static final long AUTH_TIMEOUT = 15 * 1000L;

	public static final boolean IS_DEBUG = true;

	private volatile int txIsolation;
	private volatile boolean autocommit;
	private volatile boolean txInterrupted;
	private AtomicBoolean isDtmOn = new AtomicBoolean(false);
	private XASession xaSession;

	private long lastInsertId;
	private BlockingSession session;
	private NonBlockingSession session2;

	public ServerConnection(SocketChannel channel) {
		super(channel);
		this.txInterrupted = false;
		this.autocommit = true;
	}

	@Override
	public boolean isIdleTimeout() {
		if (isAuthenticated) {
			return super.isIdleTimeout();
		} else {
			return TimeUtil.currentTimeMillis() > Math.max(lastWriteTime, lastReadTime) + AUTH_TIMEOUT;
		}
	}

	public int getTxIsolation() {
		return txIsolation;
	}

	public void setTxIsolation(int txIsolation) {
		this.txIsolation = txIsolation;
	}

	public boolean isAutocommit() {
		return autocommit;
	}

	public void setAutocommit(boolean autocommit) {
		this.autocommit = autocommit;
	}

	public long getLastInsertId() {
		return lastInsertId;
	}

	public void setLastInsertId(long lastInsertId) {
		this.lastInsertId = lastInsertId;
	}

	/**
	 * @return the isDtmOn
	 */
	public boolean isDtmOn() {
		return isDtmOn.get();
	}

	/**
	 * @param isDtmOn the isDtmOn to set
	 */
	public void setDtmOn(boolean isDtmOn) {
		// 如果已是开的，关闭就需要全部回滚
		if (this.isDtmOn() && !isDtmOn) {
			// TODO 将保存的全部回滚，并且清掉
		}
		this.isDtmOn.set(isDtmOn);
	}

	/**
	 * 设置是否需要中断当前事务
	 */
	public void setTxInterrupt() {
		// 直接把错误传给上面即可
		// if (!autocommit && !txInterrupted) {
		// txInterrupted = true;
		// }
	}

	public void setSession(BlockingSession session) {
		this.session = session;
	}

	public void setSession2(NonBlockingSession session2) {
		this.session2 = session2;
	}

	@Override
	public void ping() {
		Ping.response(this);
	}

	@Override
	public void heartbeat(byte[] data) {
		Heartbeat.response(this, data);
	}

	public XASession getXaSession() {
		return xaSession;
	}

	/**
	 * 状态置为 start
	 * 
	 * @param type
	 */
	private void detectStart(int type) {

		// start transaction...
		if (type == ServerParse.START && isDtmOn()) {

			if (xaSession == null) {
				synchronized (this) {
					if (xaSession == null) {
						xaSession = new XASession(this);
					}
				}
			} else {
				xaSession.init();
			}
			xaSession.start();
			if (IS_DEBUG) {
				LOGGER.info("detect start...and xid" + xaSession.getXid());
			}
			// 加入会话
			// XAContext.addSession(xaSession.getXid(), xaSession);
			// 对上返回ok
			getSession().writeOk();
		}
	}

	protected void xaStart(RouteResultset rrs, String oldSql, int type) {
		// 查看上下文件
		if (xaSession == null) {
			writeErrMessage(ErrorCode.ER_XA_CONTEXT_MISSING, "XAContext is losing,need to rollback...");
			return;
		}

		// 记录一下
		xaSession.record(oldSql, rrs.getNodes());

		((BlockingSession) getSession()).xaStart(rrs, xaSession.getXid(), oldSql, type);

	}

	private void detectCommit(int type) {
		if (type == ServerParse.COMMIT && isDtmOn()) {

			if (xaSession == null) {
				writeErrMessage(ErrorCode.ER_XA_CONTEXT_MISSING, "XAContext is losing,need to rollback...");
				return;
			}
			if (xaSession.getRecords().size() == 0) {
				// 直接commit
				getSession().writeOk();
			}

			if (xaSession.getStatus() != XAOp.START) {
				writeErrMessage(ErrorCode.ER_XA_STATUS_ERROR, "xa status is wrong,may not be commit.");
				return;
			}

			// 直接把记录下来的去xa end prepare
			RouteResultsetNode[] rrn = xaSession.getRecords()
					.toArray(new RouteResultsetNode[xaSession.getRecords().size()]);
			// 此处的stmt无效
			RouteResultset rrs = new RouteResultset("xa end '" + xaSession.getXid() + "'");
			rrs.setNodes(rrn);
			//
			((BlockingSession) getSession()).endAndPrepare(rrs, xaSession.getXid(), type);
		}
	}

	public void selfRollback() {
		detectRollback(ServerParse.ROLLBACK);
	}

	public void detectRollback(int type) {
		if (isDtmOn() && type == ServerParse.ROLLBACK) {
			if (xaSession == null) {
				// 此处有可能是换连接过来的 由于没有上下文,只能告诉client rollback也失败。。
				writeErrMessage(ErrorCode.ER_XA_CONTEXT_MISSING, "XAContext is losing,  rollback fail...");
				return;
			}

			if (xaSession.getRecords().size() == 0) {
				// 直接commit
				getSession().writeOk();
				return;
			}

			// 直接把记录下来的去xa end
			RouteResultsetNode[] rrn = xaSession.getRecords()
					.toArray(new RouteResultsetNode[xaSession.getRecords().size()]);
			// 此处的stmt无效
			RouteResultset rrs = new RouteResultset("xa end '" + xaSession.getXid() + "'");
			rrs.setNodes(rrn);
			try {
				((BlockingSession) getSession()).rollback(rrs, xaSession.getXid());
			} catch (Exception e) {
				LOGGER.error("save rollback '" + xaSession.getXid() + "' fail", e);
			}
			//
		}
	}

	public void execute(String sql, int type) {
		// detect init
		if (type == ServerParse.START && isDtmOn()) {
			detectStart(type);
			return;
		}

		// 状态检查
		if (txInterrupted) {
			writeErrMessage(ErrorCode.ER_YES, "Transaction error, need to rollback.");
			return;
		}

		// 检查当前使用的DB
		String db = this.schema;
		if (db == null) {
			writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
			return;
		}
		SchemaConfig schema = HeisenbergServer.getInstance().getConfig().getSchemas().get(db);
		if (schema == null) {
			writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
			return;
		}

		// 路由计算
		RouteResultset rrs = null;
		long st = System.currentTimeMillis();
		long et = -1L;
		try {
			rrs = HServerRouter.route(schema, sql, this.charset, this);
		} catch (SQLNonTransientException e) {
			StringBuilder s = new StringBuilder();
			LOGGER.warn(s.append(this).append(sql).toString(), e);
			String msg = e.getMessage();
			writeErrMessage(ErrorCode.ER_PARSE_ERROR, msg == null ? e.getClass().getSimpleName() : msg);
			return;
		} finally {
			et = System.currentTimeMillis();
			routeLogger.info((et - st) + "ms,[" + sql + "]");
		}
		// start就开始保存sql执行，保存路由计划，判断是否已经进入 xa，并且是属于DML类型
		if (isDtmOn() && xaSession.isInXa() && ServerParse.isTransType(type)) {
			// xaSave(sql, rrs.getNodes());
			// 联合执行xastart +sql
			xaStart(rrs, sql, type);
		} else {
			getSession().execute(rrs, sql, type);
		}

	}

	public Session getSession() {
		if (HeisenbergServer.getInstance().getConfig().getSystem().isBackNIO()) {
			return session2;
		} else {
			return session;
		}
	}

	/**
	 * 提交事务
	 */
	public void commit() {
		if (txInterrupted) {
			writeErrMessage(ErrorCode.ER_YES, "Transaction error, need to rollback.");
		} else {
			if (isDtmOn()) {
				detectCommit(ServerParse.COMMIT);
			} else {
				getSession().commit();
			}

		}
	}

	/**
	 * 回滚事务
	 */
	public void rollback() {
		// 状态检查
		if (txInterrupted) {
			txInterrupted = false;
		}
		if (isDtmOn()) {
			detectRollback(ServerParse.ROLLBACK);
		} else {
			// 执行回滚
			getSession().rollback();
		}

	}

	/**
	 * 撤销执行中的语句
	 * 
	 * @param sponsor 发起者为null表示是自己
	 */
	public void cancel(final FrontendConnection sponsor) {
		processor.getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				getSession().cancel(sponsor);
			}
		});
	}

	@Override
	public void error(int errCode, Throwable t) {
		// 根据异常类型和信息，选择日志输出级别。
		if (t instanceof EOFException) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(toString(), t);
			}
		} else if (isConnectionReset(t)) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(toString(), t);
			}
		} else {
			LOGGER.warn(toString(), t);
		}

		// 异常返回码处理
		switch (errCode) {
		case ErrorCode.ERR_HANDLE_DATA:
			String msg = t.getMessage();
			writeErrMessage(ErrorCode.ER_YES, msg == null ? t.getClass().getSimpleName() : msg);
			break;
		default:
			close();
		}
	}

	@Override
	public boolean close() {
		if (super.close()) {
			processor.getExecutor().execute(new Runnable() {
				@Override
				public void run() {
					getSession().terminate();
				}
			});
			return true;
		} else {
			return false;
		}
	}

}
