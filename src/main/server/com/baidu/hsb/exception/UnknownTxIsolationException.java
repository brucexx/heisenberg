/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.exception;

/**
 * 未知事物隔离级别异常
 * 
 * @author xiongzhao@baidu.com
 */
public class UnknownTxIsolationException extends RuntimeException {
    private static final long serialVersionUID = -3911059999308980358L;

    public UnknownTxIsolationException() {
        super();
    }

    public UnknownTxIsolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownTxIsolationException(String message) {
        super(message);
    }

    public UnknownTxIsolationException(Throwable cause) {
        super(cause);
    }

}
