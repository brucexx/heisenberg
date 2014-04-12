/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.exception;

/**
 * 未知数据包异常
 * 
 * @author xiongzhao@baidu.com
 */
public class UnknownPacketException extends RuntimeException {
    private static final long serialVersionUID = 3152986441780514147L;

    public UnknownPacketException() {
        super();
    }

    public UnknownPacketException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownPacketException(String message) {
        super(message);
    }

    public UnknownPacketException(Throwable cause) {
        super(cause);
    }

}
