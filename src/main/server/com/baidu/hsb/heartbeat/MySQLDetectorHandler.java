/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.heartbeat;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.exception.HeartbeatException;
import com.baidu.hsb.net.handler.BackendAsyncHandler;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.OkPacket;

/**
 * @author xiongzhao@baidu.com
 */
public class MySQLDetectorHandler extends BackendAsyncHandler {
    private static final int RESULT_STATUS_INIT = 0;
    private static final int RESULT_STATUS_HEADER = 1;
    private static final int RESULT_STATUS_FIELD_EOF = 2;

    private final MySQLDetector source;
    private volatile int resultStatus;

    public MySQLDetectorHandler(MySQLDetector source) {
        this.source = source;
        this.resultStatus = RESULT_STATUS_INIT;
    }

    @Override
    public void handle(byte[] data) {
        offerData(data, source.getProcessor().getExecutor());
    }

    @Override
    protected void offerDataError() {
        dataQueue.clear();
        resultStatus = RESULT_STATUS_INIT;
        throw new HeartbeatException("offer data error!");
    }

    @Override
    protected void handleData(byte[] data) {
        switch (resultStatus) {
        case RESULT_STATUS_INIT:
            switch (data[4]) {
            case OkPacket.FIELD_COUNT:
                handleOkPacket();
                break;
            case ErrorPacket.FIELD_COUNT:
                handleErrorPacket(data);
                break;
            default:
                resultStatus = RESULT_STATUS_HEADER;
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
                break;
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
                handleRowEofPacket();
                break;
            }
            break;
        default:
            throw new HeartbeatException("unknown status!");
        }
    }

    @Override
    protected void handleDataError(Throwable t) {
        dataQueue.clear();
        resultStatus = RESULT_STATUS_INIT;
        source.error(ErrorCode.ERR_HANDLE_DATA, t);
    }

    /**
     * OK数据包处理
     */
    private void handleOkPacket() {
        source.getHeartbeat().setResult(MySQLHeartbeat.OK_STATUS, source, false);
    }

    /**
     * ERROR数据包处理
     */
    private void handleErrorPacket(byte[] data) {
        ErrorPacket err = new ErrorPacket();
        err.read(data);
        throw new HeartbeatException(new String(err.message));
    }

    /**
     * 行数据包结束处理
     */
    private void handleRowEofPacket() {
        source.getHeartbeat().setResult(MySQLHeartbeat.OK_STATUS, source, false);
    }

}
