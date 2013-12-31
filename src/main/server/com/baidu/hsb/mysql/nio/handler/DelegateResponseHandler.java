/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.nio.handler;

import java.util.List;

import com.baidu.hsb.mysql.nio.MySQLConnection;

/**
 * @author xiongzhao@baidu.com
 */
public class DelegateResponseHandler implements ResponseHandler {
    private final ResponseHandler target;

    public DelegateResponseHandler(ResponseHandler target) {
        if (target == null) {
            throw new IllegalArgumentException("delegate is null!");
        }
        this.target = target;
    }

    @Override
    public void connectionAcquired(MySQLConnection conn) {
        target.connectionAcquired(conn);
    }

    @Override
    public void connectionError(Throwable e, MySQLConnection conn) {
        target.connectionError(e, conn);
    }

    @Override
    public void okResponse(byte[] ok, MySQLConnection conn) {
        target.okResponse(ok, conn);
    }

    @Override
    public void errorResponse(byte[] err, MySQLConnection conn) {
        target.errorResponse(err, conn);
    }

    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn) {
        target.fieldEofResponse(header, fields, eof, conn);
    }

    @Override
    public void rowResponse(byte[] row, MySQLConnection conn) {
        target.rowResponse(row, conn);
    }

    @Override
    public void rowEofResponse(byte[] eof, MySQLConnection conn) {
        target.rowEofResponse(eof, conn);
    }

}
