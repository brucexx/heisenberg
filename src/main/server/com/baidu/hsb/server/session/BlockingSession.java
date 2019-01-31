/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.exception.UnknownPacketException;
import com.baidu.hsb.mysql.bio.Channel;
import com.baidu.hsb.mysql.bio.MySQLChannel;
import com.baidu.hsb.mysql.bio.executor.DefaultCommitExecutor;
import com.baidu.hsb.mysql.bio.executor.MultiNodeMultiSqlTask;
import com.baidu.hsb.mysql.bio.executor.MultiNodeTask;
import com.baidu.hsb.mysql.bio.executor.NodeExecutor;
import com.baidu.hsb.mysql.bio.executor.RollbackExecutor;
import com.baidu.hsb.mysql.bio.executor.SingleNodeExecutor;
import com.baidu.hsb.mysql.bio.executor.SingleNodeMultiSqlExecutor;
import com.baidu.hsb.net.FrontendConnection;
import com.baidu.hsb.net.mysql.BinaryPacket;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.route.RouteResultset;
import com.baidu.hsb.route.RouteResultsetNode;
import com.baidu.hsb.route.util.StringUtil;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.server.parser.ServerParse;

/**
 * 由前后端参与的一次执行会话过程
 * 
 * @author xiongzhao@baidu.com
 */
public class BlockingSession implements Session {
    private static final Logger LOGGER = Logger.getLogger(BlockingSession.class);

    private final ServerConnection source;
    private final ConcurrentHashMap<RouteResultsetNode, Channel> target;
    private final SingleNodeExecutor singleNodeExecutor;
    private final SingleNodeMultiSqlExecutor singleNodeMultiSqlExecutor;
    private MultiNodeTask task;
    private MultiNodeMultiSqlTask mTask;
    private final DefaultCommitExecutor commitExecutor;
    private final RollbackExecutor rollbackExecutor;

    public BlockingSession(ServerConnection source) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("-->new source" + source.getHost() + "," + source.getPort());
        }
        this.source = source;
        this.target = new ConcurrentHashMap<RouteResultsetNode, Channel>();
        this.singleNodeExecutor = new SingleNodeExecutor();
        this.singleNodeMultiSqlExecutor = new SingleNodeMultiSqlExecutor();
        this.commitExecutor = new DefaultCommitExecutor();
        this.rollbackExecutor = new RollbackExecutor();
    }

    @Override
    public ServerConnection getSource() {
        return source;
    }

    @Override
    public int getTargetCount() {
        return target.size();
    }

    public ConcurrentHashMap<RouteResultsetNode, Channel> getTarget() {
        return target;
    }

    /*
     * 多条语句联合执行 (non-Javadoc)
     * 
     * @see com.baidu.hsb.server.session.Session#multiExecute(com.baidu.hsb.route.RouteResultset, java.lang.String[],
     * int)
     */
    @Override
    public void multiExecute(RouteResultset rrs, String[] sql, int type, SucCondCallback sc, FailCondCallback f) {
        if (LOGGER.isDebugEnabled()) {
            StringBuilder s = new StringBuilder();
            LOGGER.debug(s.append(source).append(rrs).toString());
            LOGGER.debug("execute sql-->" + sql);
        }

        // 检查路由结果是否为空
        RouteResultsetNode[] nodes = rrs.getNodes();
        if (nodes == null || nodes.length == 0) {
            source.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No dataNode selected");
            return;
        }
        // // 选择执行方式
        if (nodes.length == 1 && nodes[0].getSqlCount() == 1) {
            singleNodeMultiSqlExecutor.execute(nodes[0], this, rrs.getFlag(), sql, type, sc, f);
        } else {
            boolean autocommit = source.isAutocommit();
            if (autocommit && isModifySQL(type)) {
                autocommit = false;
            }
            mTask = new MultiNodeMultiSqlTask(nodes, autocommit, this, rrs.getFlag(), sql, type, sc, f);
            mTask.execute();
        }

    }

    @Override
    public void execute(RouteResultset rrs, String sql, int type) {
        if (LOGGER.isDebugEnabled()) {
            StringBuilder s = new StringBuilder();
            LOGGER.debug(s.append(source).append(rrs).toString());
            LOGGER.debug("execute sql-->" + sql);
        }

        // 检查路由结果是否为空
        RouteResultsetNode[] nodes = rrs.getNodes();
        if (nodes == null || nodes.length == 0) {
            source.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No dataNode selected");
            return;
        }
        // // 选择执行方式
        if (nodes.length == 1 && nodes[0].getSqlCount() == 1) {
            singleNodeExecutor.execute(nodes[0], this, rrs.getFlag(), sql, type);
        } else {
            executeMutil(rrs, nodes, type, sql);
        }
    }

    private void executeMutil(RouteResultset rrs, RouteResultsetNode[] nodes, int type, String sql) {

        boolean autocommit = source.isAutocommit();
        if (autocommit && isModifySQL(type)) {
            autocommit = false;
        }
        task = new MultiNodeTask(nodes, autocommit, this, rrs.getFlag(), sql, type);
        task.execute();
    }

    private String getDataNode(String dsName) {
        if (StringUtil.isEmpty(dsName)) {
            return StringUtil.EMPTY;
        }
        int idx = dsName.indexOf('[');
        return StringUtil.trim(idx > 0 ? StringUtil.substrings(dsName, 0, idx) : dsName);
    }

    @Override
    public void commit() {
        final int initCount = target.size();
        if (initCount <= 0) {
            ByteBuffer buffer = source.allocate();
            buffer = source.writeToBuffer(OkPacket.OK, buffer);
            source.write(buffer);
            return;
        }
        commitExecutor.commit(null, this, initCount);
    }

    @Override
    public void rollback() {
        rollbackExecutor.rollback(this);
    }

    @Override
    public void cancel(FrontendConnection sponsor) {
        // TODO terminate session
        source.writeErrMessage(ErrorCode.ER_QUERY_INTERRUPTED, "Query execution was interrupted");
        if (sponsor != null) {
            OkPacket packet = new OkPacket();
            packet.packetId = 1;
            packet.affectedRows = 0;
            packet.serverStatus = 2;
            packet.write(sponsor);
        }
    }

    @Override
    public void terminate() {
        // 终止所有正在执行的任务
        kill();

        // 等待所有任务结束，包括还未执行的，执行中的，执行完的。
        try {
            singleNodeExecutor.terminate();
            if (task != null) {
                task.terminate();
            }
            commitExecutor.terminate();
            rollbackExecutor.terminate();
        } catch (InterruptedException e) {
            for (RouteResultsetNode rrn : target.keySet()) {
                Channel c = target.remove(rrn);
                if (c != null) {
                    c.close();
                }
            }
            LOGGER.warn("termination interrupted: " + source, e);
        }

        // 清理绑定的资源
        clear(false);
    }

    /**
     * 释放session关联的资源
     */
    public void release() {
        // if (LOGGER.isDebugEnabled()) {
        // LOGGER.debug("block session release...");
        // }
        for (RouteResultsetNode rrn : target.keySet()) {
            Channel c = target.remove(rrn);
            if (c != null) {
                if (c.isRunning()) {
                    c.close();
                    try {
                        throw new IllegalStateException("running connection is found: " + c);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                } else if (!c.isClosed()) {
                    if (source.isClosed()) {
                        c.close();
                    } else {
                        c.release();
                    }
                }
            }
        }
    }

    public void clear() {
        clear(true);
    }

    /**
     * MUST be called at the end of {@link NodeExecutor}
     * 
     * @param pessimisticRelease true if this method might be invoked concurrently with {@link #kill()}
     */
    private void clear(boolean pessimisticRelease) {
        for (RouteResultsetNode rrn : target.keySet()) {
            Channel c = target.remove(rrn);

            // 通道不存在或者已被关闭
            if (c == null || c.isClosed()) {
                continue;
            }

            // 如果通道正在运行中，则关闭当前通道。
            if (c.isRunning() || (pessimisticRelease && source.isClosed())) {
                c.close();
                continue;
            }

            // 非事务中的通道，直接释放资源。
            if (c.isAutocommit()) {
                c.release();
                continue;
            }

            // 事务中的通道，需要先回滚后再释放资源。
            MySQLChannel mc = (MySQLChannel) c;
            try {
                BinaryPacket bin = mc.rollback();
                switch (bin.data[0]) {
                    case OkPacket.FIELD_COUNT:
                        mc.release();
                        break;
                    case ErrorPacket.FIELD_COUNT:
                        mc.close();
                        break;
                    default:
                        throw new UnknownPacketException(bin.toString());
                }
            } catch (IOException e) {
                StringBuilder s = new StringBuilder();
                LOGGER.warn(s.append(mc).append("rollback").toString(), e);
                mc.close();
            } catch (RuntimeException e) {
                StringBuilder s = new StringBuilder();
                LOGGER.warn(s.append(mc).append("rollback").toString(), e);
                mc.close();
            }
        }
    }

    /**
     * 终止执行中的通道
     */
    private void kill() {
        for (RouteResultsetNode rrn : target.keySet()) {
            Channel c = target.get(rrn);
            if (c != null && c.isRunning()) {
                c.kill();
            }
        }
    }

    /**
     * 检查是否会引起数据变更的语句
     */
    private static boolean isModifySQL(int type) {
        switch (type) {
            case ServerParse.INSERT:
            case ServerParse.DELETE:
            case ServerParse.UPDATE:
            case ServerParse.REPLACE:
                return true;
            default:
                return false;
        }
    }

}
