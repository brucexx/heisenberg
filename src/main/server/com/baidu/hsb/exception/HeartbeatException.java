/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.exception;

/**
 * @author xiongzhao@baidu.com
 */
public class HeartbeatException extends RuntimeException {
    private static final long serialVersionUID = 7639414445868741580L;

    public HeartbeatException() {
        super();
    }

    public HeartbeatException(String message, Throwable cause) {
        super(message, cause);
    }

    public HeartbeatException(String message) {
        super(message);
    }

    public HeartbeatException(Throwable cause) {
        super(cause);
    }

}
