/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.nio.handler;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.baidu.hsb.mysql.nio.MySQLConnection;
import com.baidu.hsb.net.mysql.CommandPacket;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.MySQLPacket;
import com.baidu.hsb.server.session.NonBlockingSession;

/**
 * @author xiongzhao@baidu.com
 */
public class KillConnectionHandler implements ResponseHandler {
    private static final Logger LOGGER = Logger.getLogger(KillConnectionHandler.class);

    private final MySQLConnection killee;
    private final NonBlockingSession session;
    private final Runnable finishHook;
    private final AtomicInteger counter;

    public KillConnectionHandler(MySQLConnection killee, NonBlockingSession session, Runnable finishHook,
            AtomicInteger counter) {
        this.killee = killee;
        this.session = session;
        this.finishHook = finishHook;
        this.counter = counter;
    }

    @Override
    public void connectionAcquired(MySQLConnection conn) {
        conn.setResponseHandler(this);
        CommandPacket packet = new CommandPacket();
        packet.packetId = 0;
        packet.command = MySQLPacket.COM_QUERY;
        packet.arg = new StringBuilder("KILL ").append(killee.getThreadId()).toString().getBytes();
        packet.write(conn);
    }

    private void finished() {
        if (counter.decrementAndGet() <= 0) {
            finishHook.run();
        }
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection conn) {
        if (conn != null) {
            conn.close();
        }
        killee.close();
        finished();
    }

    @Override
    public void okResponse(byte[] ok, MySQLConnection conn) {
        conn.release();
        killee.close();
        finished();
    }

    @Override
    public void rowEofResponse(byte[] eof, MySQLConnection conn) {
        LOGGER.error(new StringBuilder().append("unexpected packet for ").append(conn).append(" bound by ")
                .append(session.getSource()).append(": field's eof").toString());
        conn.quit();
        killee.close();
        finished();
    }

    @Override
    public void errorResponse(byte[] data, MySQLConnection conn) {
        ErrorPacket err = new ErrorPacket();
        err.read(data);
        String msg = null;
        try {
            msg = new String(err.message, conn.getCharset());
        } catch (UnsupportedEncodingException e) {
            msg = new String(err.message);
        }
        LOGGER.warn("kill backend connection " + killee + " failed: " + msg);
        conn.release();
        killee.close();
        finished();
    }

    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn) {
    }

    @Override
    public void rowResponse(byte[] row, MySQLConnection conn) {
    }

}
