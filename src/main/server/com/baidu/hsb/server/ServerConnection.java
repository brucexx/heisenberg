/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server;

import java.io.EOFException;
import java.nio.channels.SocketChannel;
import java.sql.SQLNonTransientException;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.ArrayUtils;
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
import com.baidu.hsb.server.session.FailCondCallback;
import com.baidu.hsb.server.session.NonBlockingSession;
import com.baidu.hsb.server.session.Session;
import com.baidu.hsb.server.session.SucCondCallback;
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
            xaSession.setInXa(true);
            if (IS_DEBUG) {
                LOGGER.info("detect start...and xid" + xaSession.getXid());
            }
            // 加入会话
            // XAContext.addSession(xaSession.getXid(), xaSession);
        }
    }

    private void saveShardInfo(RouteResultsetNode[] rrn, XAOp status) {
        // TODO
        if (IS_DEBUG) {
            LOGGER.info("save redis status:" + xaSession.getXid() + "," + status.name());
        }
    }

    private void delShareInfo() {
        // TODO
        if (IS_DEBUG) {
            LOGGER.info("del redis status:" + xaSession.getXid());
        }
    }

    private void detectCommit(int type) {
        if (type == ServerParse.COMMIT && isDtmOn() && xaSession.getStatus() == XAOp.START) {
            if (xaSession == null) {
                writeErrMessage(ErrorCode.ER_XA_CONTEXT_MISSING, "XAContext is losing,need to rollback...");
                return;
            }
            // 直接把记录下来的去xa end
            RouteResultsetNode[] rrn =
                    xaSession.getRecords().toArray(new RouteResultsetNode[xaSession.getRecords().size()]);
            // 保存分片到redis,start status
            saveShardInfo(rrn, XAOp.START);

            // xa end & prepare 并保存之， 此处返回给上层即可,后面的commit无所谓提不提交，有异步流程来保证提交
            xaEndAndPrepare(rrn);
            if (IS_DEBUG) {
                LOGGER.info("end and prepare over...xid:" + xaSession.getXid());
            }

            // xa commit
            xaCommit(rrn);
            //
            delShareInfo();

            if (IS_DEBUG) {
                LOGGER.info("detect commit...and commit over..");
            }
            // remove会话
            // XAContext.removeSession(xaSession.getXid());
        }
    }

    private void detectRollback(int type) {
        if (type == ServerParse.ROLLBACK && isDtmOn()) {
            if (xaSession == null) {
                // 此处有可能是换连接过来的 由于没有上下文,只能告诉client rollback也失败。。
                writeErrMessage(ErrorCode.ER_XA_CONTEXT_MISSING, "XAContext is losing,  rollback fail...");
                return;
            }

            // 直接把记录下来的去xa rollback
            RouteResultsetNode[] rrn =
                    xaSession.getRecords().toArray(new RouteResultsetNode[xaSession.getRecords().size()]);

            xaRollback(rrn);
            if (IS_DEBUG) {
                LOGGER.info("rollback...xid:" + xaSession.getXid());
            }
            // remove会话
            // XAContext.removeSession(xaSession.getXid());

        }
    }

    private void xaSave(String sql, RouteResultsetNode[] rrn) {
        if (IS_DEBUG) {
            LOGGER.info("save sql:" + sql);
        }
        // 查看上下文件
        if (xaSession == null) {
            writeErrMessage(ErrorCode.ER_XA_CONTEXT_MISSING, "XAContext is losing,need to rollback...");
            return;
        }
        // 开始记录当前语句
        xaSession.record(sql, rrn);
    }

    /**
     * 复制一遍datanode
     * 
     * @param sql
     * @param rrn
     * @return
     */
    private RouteResultset genRrs(RouteResultsetNode[] rrn, String...sql) {
        RouteResultsetNode[] rn_cp = new RouteResultsetNode[rrn.length];
        for (int i = 0; i < rrn.length; i++) {
            rn_cp[i] = new RouteResultsetNode(rrn[i].getName(), rrn[i].getReplicaIndex(), sql);
        }
        RouteResultset rrs = new RouteResultset(sql[0]);
        rrs.setNodes(rn_cp);
        return rrs;
    }

    private void xaStart(RouteResultsetNode[] rrn, String oldSql) {
        // 查看上下文件
        if (xaSession == null) {
            writeErrMessage(ErrorCode.ER_XA_CONTEXT_MISSING, "XAContext is losing,need to rollback...");
            return;
        }
        // 很重要
        xaSession.clearResult();

        if (IS_DEBUG) {
            LOGGER.info("xastart...xid:" + xaSession.getXid() + ",start...");
        }
        String xaStart = "xa start '" + xaSession.getXid() + "'";

        for (RouteResultsetNode rn : rrn) {
            String[] s = new String[rn.getSqlCount() + 1];
            s[0] = xaStart;
            System.arraycopy(rn.getStatement(), 0, s, 1, rn.getSqlCount());
            rn.setStatement(s);
        }

        RouteResultset rrs = new RouteResultset(oldSql);
        rrs.setNodes(rrn);
        // 先联合执行一把xa start + real sql
        getSession().multiExecute(rrs, new String[] { xaStart, oldSql }, ServerParse.XA, new SucCondCallback() {

            @Override
            public void condition() {
                // do nothing

            }

        }, new FailCondCallback() {

            @Override
            public void condition(int code, String msg) {
                if (IS_DEBUG) {
                    LOGGER.info("xastart...xid:" + xaSession.getXid() + "[" + oldSql + "]fail.." + code + "," + msg);
                }
                // 直接 end and rollback..
                xaEndAndRollbackWithOuthAck(rrn);
            }
        }, true);

        if (IS_DEBUG) {
            LOGGER.info("xastart...xid:" + xaSession.getXid() + "[" + oldSql + "]over..");
        }

    }

    private void xaEndAndRollbackWithOuthAck(RouteResultsetNode[] rrn) {
        if (xaSession == null) {
            writeErrMessage(ErrorCode.ER_XA_CONTEXT_MISSING, "XAContext is losing, end and rollback fail...");
            return;
        }
        // 来一发end
        xaSession.clearResult();
        String[] sql = new String[] { "xa end '" + xaSession.getXid() + "'",
                "xa rollback '" + xaSession.getXid() + "' start.." };

        // 先联合执行一把xa end + xa prepare
        getSession().multiExecute(genRrs(rrn, sql), sql, ServerParse.XA, new SucCondCallback() {

            @Override
            public void condition() {
                LOGGER.info("end&rollback " + xaSession.getXid() + " suc ");
            }
        }, new FailCondCallback() {

            @Override
            public void condition(int code, String msg) {
                LOGGER.info("end&rollback " + xaSession.getXid() + " fail,code: " + code + ",msg:" + msg);

            }
        }, false);

    }

    private void xaRollback(RouteResultsetNode[] rrn) {
        if (xaSession == null) {
            writeErrMessage(ErrorCode.ER_XA_CONTEXT_MISSING, "XAContext is losing,need to rollback...");
            return;
        }
        xaSession.clearResult();
        String sql = "xa rollback '" + xaSession.getXid() + "'";

        // 先执行一把xa end
        getSession().execute(genRrs(rrn, sql), sql, ServerParse.XA);
        if (xaSession.getResult() != null && xaSession.getResult()) {
            if (xaSession.getStatus() == null) {
                xaSession.setStatus(XAOp.ROLLBACK);
            }
        }
    }

    private void xaEndAndPrepare(RouteResultsetNode[] rrn) {
        if (xaSession == null) {
            writeErrMessage(ErrorCode.ER_XA_CONTEXT_MISSING, "XAContext is losing,need to rollback...");
            return;
        }
        // 来一发end
        xaSession.clearResult();
        String[] sql =
                new String[] { "xa end '" + xaSession.getXid() + "'", "xa prepare '" + xaSession.getXid() + "'" };

        // 先联合执行一把xa end + xa prepare
        getSession().multiExecute(genRrs(rrn, sql), sql, ServerParse.XA, new SucCondCallback() {

            @Override
            public void condition() {
                // 保存
                saveShardInfo(rrn, XAOp.PREPARE);

            }
        }, new FailCondCallback() {

            @Override
            public void condition(int code, String msg) {
                // do nothing
            }
        }, true);

        if (xaSession.getResult() != null && xaSession.getResult()) {
            if (xaSession.getStatus() == null) {
                xaSession.setStatus(XAOp.PREPARE);
            }
        }

    }

    private void xaCommit(RouteResultsetNode[] rrn) {
        if (xaSession == null) {
            writeErrMessage(ErrorCode.ER_XA_CONTEXT_MISSING, "XAContext is losing,need to rollback...");
            return;
        }
        String sql = "xa commit '" + xaSession.getXid() + "'";

        // 先执行一把xa commit
        getSession().execute(genRrs(rrn, sql), sql, ServerParse.XA);
        if (xaSession.getResult() != null && xaSession.getResult()) {
            if (xaSession.getStatus() == null) {
                xaSession.setStatus(XAOp.COMMIT);
            }
        }
    }

    public void execute(String sql, int type) {
        // detect init
        if (type == ServerParse.START && isDtmOn()) {
            detectStart(type);
            return;
        }

        detectCommit(type);

        // 先鉴定rollback
        detectRollback(type);

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
        // start就开始保存sql执行，保存路由计划，判断是否已经进入 xa
        if (isDtmOn() && xaSession.isInXa() && ServerParse.isTransType(type)) {
            xaSave(sql, rrs.getNodes());
            // 联合执行xastart
            xaStart(rrs.getNodes(), sql);
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
            getSession().commit();
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

        // 执行回滚
        getSession().rollback();
    }

    // 用于监听收到的错误码
    @Override
    public void writeCode(boolean isSuc, int code) {
        if (IS_DEBUG) {
            LOGGER.info("execute resp code:" + code + ",isSuc:" + isSuc);
        }
        if (obList.size() == 0)
            return;
        String[] data = new String[] { new String("" + isSuc), code + "" };
        for (Observer o : obList) {
            // 通知
            o.update(null, data);
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
