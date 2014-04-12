/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.nio;

import java.util.ArrayList;
import java.util.List;

import com.baidu.hsb.mysql.ByteUtil;
import com.baidu.hsb.mysql.nio.handler.ResponseHandler;
import com.baidu.hsb.net.handler.BackendAsyncHandler;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.OkPacket;

/**
 * life cycle: from connection establish to close <br/>
 * 
 * @author xiongzhao@baidu.com 2012-4-12
 */
public class MySQLConnectionHandler extends BackendAsyncHandler {
    private static final int RESULT_STATUS_INIT = 0;
    private static final int RESULT_STATUS_HEADER = 1;
    private static final int RESULT_STATUS_FIELD_EOF = 2;

    private final MySQLConnection source;
    private volatile int resultStatus;
    private volatile byte[] header;
    private volatile List<byte[]> fields;

    /**
     * life cycle: one SQL execution
     */
    private volatile ResponseHandler responseHandler;

    public MySQLConnectionHandler(MySQLConnection source) {
        this.source = source;
        this.resultStatus = RESULT_STATUS_INIT;
    }

    public void connectionError(Throwable e) {
        // connError = e;
        // handleQueue();
    }

    public MySQLConnection getSource() {
        return source;
    }

    @Override
    public void handle(byte[] data) {
        offerData(data, source.getProcessor().getExecutor());
    }

    @Override
    protected void offerDataError() {
        dataQueue.clear();
        resultStatus = RESULT_STATUS_INIT;
        throw new RuntimeException("offer data error!");
    }

    @Override
    protected void handleData(byte[] data) {
        switch (resultStatus) {
        case RESULT_STATUS_INIT:
            switch (data[4]) {
            case OkPacket.FIELD_COUNT:
                handleOkPacket(data);
                break;
            case ErrorPacket.FIELD_COUNT:
                handleErrorPacket(data);
                break;
            default:
                resultStatus = RESULT_STATUS_HEADER;
                header = data;
                fields = new ArrayList<byte[]>((int) ByteUtil.readLength(data, 4));
            }
            break;
        case RESULT_STATUS_HEADER:
            switch (data[4]) {
            case ErrorPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_INIT;
                handleErrorPacket(data);
                break;
            case EOFPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_FIELD_EOF;
                handleFieldEofPacket(data);
                break;
            default:
                fields.add(data);
            }
            break;
        case RESULT_STATUS_FIELD_EOF:
            switch (data[4]) {
            case ErrorPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_INIT;
                handleErrorPacket(data);
                break;
            case EOFPacket.FIELD_COUNT:
                resultStatus = RESULT_STATUS_INIT;
                handleRowEofPacket(data);
                break;
            default:
                handleRowPacket(data);
            }
            break;
        default:
            throw new RuntimeException("unknown status!");
        }
    }

    public void setResponseHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    protected void handleDataError(Throwable t) {
        dataQueue.clear();
        resultStatus = RESULT_STATUS_INIT;
        responseHandler.connectionError(t, source);
    }

    /**
     * OK数据包处理
     */
    private void handleOkPacket(byte[] data) {
        responseHandler.okResponse(data, source);
    }

    /**
     * ERROR数据包处理
     */
    private void handleErrorPacket(byte[] data) {
        responseHandler.errorResponse(data, source);
    }

    /**
     * 字段数据包结束处理
     */
    private void handleFieldEofPacket(byte[] data) {
        responseHandler.fieldEofResponse(header, fields, data, source);
    }

    /**
     * 行数据包处理
     */
    private void handleRowPacket(byte[] data) {
        responseHandler.rowResponse(data, source);
    }

    /**
     * 行数据包结束处理
     */
    private void handleRowEofPacket(byte[] data) {
        responseHandler.rowEofResponse(data, source);
    }

}
