/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.nio.handler;

import java.util.List;

import org.apache.log4j.Logger;

import com.baidu.hsb.mysql.nio.MySQLConnection;

/**
 * @author xiongzhao@baidu.com
 */
public class RollbackReleaseHandler implements ResponseHandler {
    private static final Logger logger = Logger.getLogger(RollbackReleaseHandler.class);

    public RollbackReleaseHandler() {
    }

    @Override
    public void connectionAcquired(MySQLConnection conn) {
        logger.error("unexpected invocation: connectionAcquired from rollback-release");
        conn.close();
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection conn) {
        logger.error("unexpected invocation: connectionError from rollback-release");
        conn.close();
    }

    @Override
    public void errorResponse(byte[] err, MySQLConnection conn) {
        conn.quit();
    }

    @Override
    public void okResponse(byte[] ok, MySQLConnection conn) {
        conn.release();
    }

    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn) {
    }

    @Override
    public void rowResponse(byte[] row, MySQLConnection conn) {
    }

    @Override
    public void rowEofResponse(byte[] eof, MySQLConnection conn) {
        logger.error("unexpected packet: EOF of resultSet from rollback-release");
        conn.close();
    }

}
