/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.nio.handler;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.baidu.hsb.HeisenbergConfig;
import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.mysql.nio.MySQLConnection;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.route.RouteResultsetNode;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.server.session.NonBlockingSession;
import com.baidu.hsb.util.StringUtil;

/**
 * @author xiongzhao@baidu.com
 */
public class SingleNodeHandler implements ResponseHandler, Terminatable {
    private final RouteResultsetNode route;
    private final NonBlockingSession session;
    private byte packetId;
    private volatile ByteBuffer buffer;
    private ReentrantLock lock = new ReentrantLock();
    private boolean isRunning;
    private Runnable terminateCallBack;

    public SingleNodeHandler(RouteResultsetNode route, NonBlockingSession session) {
        if (route == null) {
            throw new IllegalArgumentException("routeNode is null!");
        }
        if (session == null) {
            throw new IllegalArgumentException("session is null!");
        }
        this.session = session;
        this.route = route;
    }

    @Override
    public void terminate(Runnable callback) {
        boolean zeroReached = false;
        lock.lock();
        try {
            if (isRunning) {
                terminateCallBack = callback;
            } else {
                zeroReached = true;
            }
        } finally {
            lock.unlock();
        }
        if (zeroReached) {
            callback.run();
        }
    }

    private void endRunning() {
        Runnable callback = null;
        lock.lock();
        try {
            if (isRunning) {
                isRunning = false;
                callback = terminateCallBack;
                terminateCallBack = null;
            }
        } finally {
            lock.unlock();
        }
        if (callback != null) {
            callback.run();
        }
    }

    public void execute() throws Exception {
        lock.lock();
        try {
            this.isRunning = true;
            this.packetId = 0;
            this.buffer = session.getSource().allocate();
        } finally {
            lock.unlock();
        }
        final MySQLConnection conn = session.getTarget(route);
        if (conn == null) {
            HeisenbergConfig conf = HeisenbergServer.getInstance().getConfig();
            MySQLDataNode dn = conf.getDataNodes().get(route.getName());
            dn.getConnection(this, null);
        } else {
            conn.setRunning(true);
            session.getSource().getProcessor().getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    _execute(conn);
                }
            });
        }
    }

    @Override
    public void connectionAcquired(final MySQLConnection conn) {
        conn.setRunning(true);
        session.bindConnection(route, conn);
        session.getSource().getProcessor().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                _execute(conn);
            }
        });
    }

    private void _execute(MySQLConnection conn) {
        if (session.closed()) {
            conn.setRunning(false);
            endRunning();
            session.clearConnections();
            return;
        }
        conn.setResponseHandler(this);
        try {
            conn.execute(route, session.getSource(), session.getSource().isAutocommit());
        } catch (UnsupportedEncodingException e1) {
            executeException(conn);
            return;
        }
    }

    private void executeException(MySQLConnection c) {
        c.setRunning(false);
        endRunning();
        session.clearConnections();
        ErrorPacket err = new ErrorPacket();
        err.packetId = ++packetId;
        err.errno = ErrorCode.ER_YES;
        err.message = StringUtil.encode("unknown backend charset: " + c.getCharset(), session.getSource().getCharset());
        ServerConnection source = session.getSource();
        source.write(err.write(buffer, source));
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection conn) {
        if (!session.closeConnection(route)) {
            conn.close();
        }
        endRunning();
        ErrorPacket err = new ErrorPacket();
        err.packetId = ++packetId;
        err.errno = ErrorCode.ER_YES;
        err.message = StringUtil.encode(e.getMessage(), session.getSource().getCharset());
        ServerConnection source = session.getSource();
        source.write(err.write(buffer, source));
    }

    @Override
    public void errorResponse(byte[] err, MySQLConnection conn) {
        conn.setRunning(false);
        if (conn.isAutocommit()) {
            session.clearConnections();
        }
        endRunning();
        session.getSource().write(err);
    }

    @Override
    public void okResponse(byte[] data, MySQLConnection conn) {
        boolean executeResponse = false;
        try {
            executeResponse = conn.syncAndExcute();
        } catch (UnsupportedEncodingException e) {
            executeException(conn);
        }
        if (executeResponse) {
            conn.setRunning(false);
            ServerConnection source = session.getSource();
            if (source.isAutocommit()) {
                session.clearConnections();
            }
            endRunning();
            OkPacket ok = new OkPacket();
            ok.read(data);
            source.setLastInsertId(ok.insertId);
            buffer = source.writeToBuffer(data, buffer);
            source.write(buffer);
        }
    }

    @Override
    public void rowEofResponse(byte[] eof, MySQLConnection conn) {
        ServerConnection source = session.getSource();
        conn.setRunning(false);
        conn.recordSql(source.getHost(), source.getSchema(), route.getStatement());
        if (source.isAutocommit()) {
            session.clearConnections();
        }
        endRunning();
        buffer = source.writeToBuffer(eof, buffer);
        source.write(buffer);
    }

    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn) {
        ServerConnection source = session.getSource();
        buffer = session.getSource().allocate();
        ++packetId;
        buffer = source.writeToBuffer(header, buffer);
        for (int i = 0, len = fields.size(); i < len; ++i) {
            ++packetId;
            buffer = source.writeToBuffer(fields.get(i), buffer);
        }
        ++packetId;
        buffer = source.writeToBuffer(eof, buffer);
        source.write(buffer);
    }

    @Override
    public void rowResponse(byte[] row, MySQLConnection conn) {
        ++packetId;
        buffer = session.getSource().writeToBuffer(row, buffer);
    }

}
