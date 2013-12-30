/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.handler;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: FrontendPrepareHandler.java, v 0.1 2013年12月26日 下午6:02:18 HI:brucest0078 Exp $
 */
public interface FrontendPrepareHandler {

    void prepare(String sql);

    void execute(byte[] data);

    void close();

}
