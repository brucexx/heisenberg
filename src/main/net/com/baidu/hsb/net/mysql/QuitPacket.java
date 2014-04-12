/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.mysql;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: QuitPacket.java, v 0.1 2013年12月26日 下午6:05:23 HI:brucest0078 Exp $
 */
public class QuitPacket extends MySQLPacket {
    public static final byte[] QUIT = new byte[] { 1, 0, 0, 0, 1 };

    @Override
    public int calcPacketSize() {
        return 1;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Quit Packet";
    }

}
