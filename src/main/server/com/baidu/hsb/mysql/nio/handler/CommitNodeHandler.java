/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.nio.handler;

import java.util.List;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import com.baidu.hsb.mysql.nio.MySQLConnection;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.route.RouteResultsetNode;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.server.session.NonBlockingSession;

/**
 * @author xiongzhao@baidu.com
 */
public class CommitNodeHandler extends MultiNodeHandler {
    private static final Logger logger = Logger.getLogger(CommitNodeHandler.class);
    private OkPacket okPacket;

    public CommitNodeHandler(NonBlockingSession session) {
        super(session);
    }

    public void commit() {
        commit(null);
    }

    public void commit(OkPacket packet) {
        final int initCount = session.getTargetCount();
        lock.lock();
        try {
            reset(initCount);
            okPacket = packet;
        } finally {
            lock.unlock();
        }
        if (session.closed()) {
            decrementCountToZero();
            return;
        }

        // 执行
        Executor executor = session.getSource().getProcessor().getExecutor();
        int started = 0;
        for (RouteResultsetNode rrn : session.getTargetKeys()) {
            if (rrn == null) {
                try {
                    logger.error("null is contained in RoutResultsetNodes, source = " + session.getSource());
                } catch (Exception e) {
                }
                continue;
            }
            final MySQLConnection conn = session.getTarget(rrn);
            if (conn != null) {
                conn.setRunning(true);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isFail.get() || session.closed()) {
                            backendConnError(conn, "cancelled by other thread");
                            return;
                        }
                        conn.setResponseHandler(CommitNodeHandler.this);
                        conn.commit();
                    }
                });
                ++started;
            }
        }

        if (started < initCount && decrementCountBy(initCount - started)) {
            /**
             * assumption: only caused by front-end connection close. <br/>
             * Otherwise, packet must be returned to front-end
             */
            session.clearConnections();
        }
    }

    @Override
    public void connectionAcquired(MySQLConnection conn) {
        logger.error("unexpected invocation: connectionAcquired from commit");
        conn.release();
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection conn) {
        backendConnError(conn, "connection err for " + conn);
    }

    @Override
    public void okResponse(byte[] ok, MySQLConnection conn) {
        conn.setRunning(false);
        if (decrementCountBy(1)) {
            if (isFail.get() || session.closed()) {
                notifyError((byte) 1);
            } else {
                session.releaseConnections();
                if (okPacket == null) {
                    ServerConnection source = session.getSource();
                    source.write(ok);
                } else {
                    okPacket.write(session.getSource());
                }
            }
        }
    }

    @Override
    public void errorResponse(byte[] data, MySQLConnection conn) {
        ErrorPacket err = new ErrorPacket();
        err.read(data);
        backendConnError(conn, err);
    }

    @Override
    public void rowEofResponse(byte[] eof, MySQLConnection conn) {
        backendConnError(conn, "Unknown response packet for back-end commit");
    }

    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn) {
        logger.error(new StringBuilder().append("unexpected packet for ").append(conn).append(" bound by ")
                .append(session.getSource()).append(": field's eof").toString());
    }

    @Override
    public void rowResponse(byte[] row, MySQLConnection conn) {
        logger.warn(new StringBuilder().append("unexpected packet for ").append(conn).append(" bound by ")
                .append(session.getSource()).append(": row data packet").toString());
    }
}
