/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.mysql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.baidu.hsb.exception.ErrorPacketException;
import com.baidu.hsb.exception.UnknownPacketException;
import com.baidu.hsb.mysql.BufferUtil;
import com.baidu.hsb.mysql.StreamUtil;
import com.baidu.hsb.net.FrontendConnection;
import com.baidu.hsb.route.util.ByteUtil;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: BinaryPacket.java, v 0.1 2013年12月26日 下午6:02:59 HI:brucest0078 Exp $
 */
public class BinaryPacket extends MySQLPacket {
    public static final byte OK = 1;
    public static final byte ERROR = 2;
    public static final byte HEADER = 3;
    public static final byte FIELD = 4;
    public static final byte FIELD_EOF = 5;
    public static final byte ROW = 6;
    public static final byte PACKET_EOF = 7;
    public static final byte CONN_ERROR = (byte) 0xff;

    public byte[] data;

    public void read(InputStream in) throws IOException {
        packetLength = StreamUtil.readUB3(in);
        packetId = StreamUtil.read(in);
        byte[] ab = new byte[packetLength];
        StreamUtil.read(in, ab, 0, ab.length);
        data = ab;
    }

    public void hsRead(InputStream in) throws IOException {
        packetLength = StreamUtil.readUB3(in);
        packetId = StreamUtil.read(in);
        byte[] ab = new byte[packetLength];
        StreamUtil.read(in, ab, 0, ab.length);
        data = ab;
        switch (data[0]) {
            case CONN_ERROR:
                throw new ErrorPacketException(new String(data, 1, data.length - 1));
        }
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
