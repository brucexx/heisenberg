/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: RouteException.java, v 0.1 2013年12月31日 下午1:14:08 HI:brucest0078 Exp $
 */
public class RouteException extends Exception {
    private static final long serialVersionUID = -5859343132692693104L;

    public RouteException() {
        super();
    }

    public RouteException(String message, Throwable cause) {
        super(message, cause);
    }

    public RouteException(String message) {
        super(message);
    }

    public RouteException(Throwable cause) {
        super(cause);
    }

}
