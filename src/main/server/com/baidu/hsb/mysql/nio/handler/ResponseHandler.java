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
public interface ResponseHandler {
    /**
     * 已获得有效连接的响应处理
     */
    void connectionAcquired(MySQLConnection conn);

    /**
     * 发生异常的响应处理
     */
    void connectionError(Throwable e, MySQLConnection conn);

    /**
     * 收到错误数据包的响应处理
     */
    void errorResponse(byte[] err, MySQLConnection conn);

    /**
     * 收到OK数据包的响应处理
     */
    void okResponse(byte[] ok, MySQLConnection conn);

    /**
     * 收到字段数据包结束的响应处理
     */
    void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn);

    /**
     * 收到行数据包的响应处理
     */
    void rowResponse(byte[] row, MySQLConnection conn);

    /**
     * 收到行数据包结束的响应处理
     */
    void rowEofResponse(byte[] eof, MySQLConnection conn);

}
