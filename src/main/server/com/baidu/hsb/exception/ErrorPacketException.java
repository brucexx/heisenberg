/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.exception;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: ErrorPacketException.java, v 0.1 2013年12月31日 下午1:14:34 HI:brucest0078 Exp $
 */
public class ErrorPacketException extends RuntimeException {
    private static final long serialVersionUID = -2692093550257870555L;

    public ErrorPacketException() {
        super();
    }

    public ErrorPacketException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorPacketException(String message) {
        super(message);
    }

    public ErrorPacketException(Throwable cause) {
        super(cause);
    }

}
