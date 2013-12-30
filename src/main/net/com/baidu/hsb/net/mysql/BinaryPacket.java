/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.mysql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.baidu.hsb.mysql.BufferUtil;
import com.baidu.hsb.mysql.StreamUtil;
import com.baidu.hsb.net.FrontendConnection;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: BinaryPacket.java, v 0.1 2013年12月26日 下午6:02:59 HI:brucest0078 Exp $
 */
public class BinaryPacket extends MySQLPacket {
    public static final byte OK         = 1;
    public static final byte ERROR      = 2;
    public static final byte HEADER     = 3;
    public static final byte FIELD      = 4;
    public static final byte FIELD_EOF  = 5;
    public static final byte ROW        = 6;
    public static final byte PACKET_EOF = 7;

    public byte[]            data;

    public void read(InputStream in) throws IOException {
        packetLength = StreamUtil.readUB3(in);
        packetId = StreamUtil.read(in);
        byte[] ab = new byte[packetLength];
        StreamUtil.read(in, ab, 0, ab.length);
        data = ab;
    }

    public byte[] getData() {
        return data != null ? data : new byte[0];
    }

    @Override
    public ByteBuffer write(ByteBuffer buffer, FrontendConnection c) {
        buffer = c.checkWriteBuffer(buffer, c.getPacketHeaderSize());
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetId);
        buffer = c.writeToBuffer(data, buffer);
        return buffer;
    }

    @Override
    public int calcPacketSize() {
        return data == null ? 0 : data.length;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Binary Packet";
    }

}
