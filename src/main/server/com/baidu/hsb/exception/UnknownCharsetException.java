/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.exception;

/**
 * 未知字符集异常
 * 
 * @author xiongzhao@baidu.com
 */
public class UnknownCharsetException extends RuntimeException {
    private static final long serialVersionUID = 552833416065882969L;

    public UnknownCharsetException() {
        super();
    }

    public UnknownCharsetException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownCharsetException(String message) {
        super(message);
    }

    public UnknownCharsetException(Throwable cause) {
        super(cause);
    }

}
