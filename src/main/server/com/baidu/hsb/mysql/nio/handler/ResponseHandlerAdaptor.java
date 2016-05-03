/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.mysql.nio.handler;

import java.util.List;

import org.apache.log4j.Logger;

import com.baidu.hsb.mysql.nio.MySQLConnection;

/**
 * @author brucexx
 *
 */
public class ResponseHandlerAdaptor implements ResponseHandler{
    
    private static final Logger LOGGER = Logger.getLogger(ResponseHandlerAdaptor.class);


    /* (non-Javadoc)
     * @see com.baidu.hsb.mysql.nio.handler.ResponseHandler#connectionAcquired(com.baidu.hsb.mysql.nio.MySQLConnection)
     */
    @Override
    public void connectionAcquired(MySQLConnection conn) {
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("NIO "+conn.getChannel()+"is connected!");
        }
    }

    /* (non-Javadoc)
     * @see com.baidu.hsb.mysql.nio.handler.ResponseHandler#connectionError(java.lang.Throwable, com.baidu.hsb.mysql.nio.MySQLConnection)
     */
    @Override
    public void connectionError(Throwable e, MySQLConnection conn) {
        conn.release();
    }

    /* (non-Javadoc)
     * @see com.baidu.hsb.mysql.nio.handler.ResponseHandler#errorResponse(byte[], com.baidu.hsb.mysql.nio.MySQLConnection)
     */
    @Override
    public void errorResponse(byte[] err, MySQLConnection conn) {
        
    }

    /* (non-Javadoc)
     * @see com.baidu.hsb.mysql.nio.handler.ResponseHandler#okResponse(byte[], com.baidu.hsb.mysql.nio.MySQLConnection)
     */
    @Override
    public void okResponse(byte[] ok, MySQLConnection conn) {
        
    }

    /* (non-Javadoc)
     * @see com.baidu.hsb.mysql.nio.handler.ResponseHandler#fieldEofResponse(byte[], java.util.List, byte[], com.baidu.hsb.mysql.nio.MySQLConnection)
     */
    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, MySQLConnection conn) {
        
    }

    /* (non-Javadoc)
     * @see com.baidu.hsb.mysql.nio.handler.ResponseHandler#rowResponse(byte[], com.baidu.hsb.mysql.nio.MySQLConnection)
     */
    @Override
    public void rowResponse(byte[] row, MySQLConnection conn) {
        
    }

    /* (non-Javadoc)
     * @see com.baidu.hsb.mysql.nio.handler.ResponseHandler#rowEofResponse(byte[], com.baidu.hsb.mysql.nio.MySQLConnection)
     */
    @Override
    public void rowEofResponse(byte[] eof, MySQLConnection conn) {
        
    }

}
