/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.mysql;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: PingPacket.java, v 0.1 2013年12月26日 下午6:05:11 HI:brucest0078 Exp $
 */
public class PingPacket extends MySQLPacket {
    public static final byte[] PING = new byte[] { 1, 0, 0, 0, 14 };

    @Override
    public int calcPacketSize() {
        return 1;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Ping Packet";
    }

}
