/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server.handler;

import java.nio.ByteBuffer;

import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.server.ServerConnection;

/**
 * @author xiongzhao@baidu.com
 */
public final class CommitHandler {

    public static void handle(ServerConnection c) {
        ByteBuffer buffer = c.allocate();
        c.write(c.writeToBuffer(OkPacket.OK, buffer));
    }

}
