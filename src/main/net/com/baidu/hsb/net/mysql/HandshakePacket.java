/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.mysql;

import java.nio.ByteBuffer;

import com.baidu.hsb.mysql.BufferUtil;
import com.baidu.hsb.mysql.MySQLMessage;
import com.baidu.hsb.net.FrontendConnection;

/**
 * From server to client during initial handshake.
 * 
 * <pre>
 * Bytes                        Name
 * -----                        ----
 * 1                            protocol_version
 * n (Null-Terminated String)   server_version
 * 4                            thread_id
 * 8                            scramble_buff
 * 1                            (filler) always 0x00
 * 2                            server_capabilities
 * 1                            server_language
 * 2                            server_status
 * 13                           (filler) always 0x00 ...
 * 13                           rest of scramble_buff (4.1)
 * 
 * @see http://forge.mysql.com/wiki/MySQL_Internals_ClientServer_Protocol#Handshake_Initialization_Packet
 * </pre>
 * 
 * @author xiongzhao@baidu.com
 */
public class HandshakePacket extends MySQLPacket {
    private static final byte[] FILLER_13 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    public byte protocolVersion;
    public byte[] serverVersion;
    public long threadId;
    public byte[] seed;
    public int serverCapabilities;
    public byte serverCharsetIndex;
    public int serverStatus;
    public byte[] restOfScrambleBuff;

    public void read(BinaryPacket bin) {
        packetLength = bin.packetLength;
        packetId = bin.packetId;
        MySQLMessage mm = new MySQLMessage(bin.data);
        protocolVersion = mm.read();
        serverVersion = mm.readBytesWithNull();
        threadId = mm.readUB4();
        seed = mm.readBytesWithNull();
        serverCapabilities = mm.readUB2();
        serverCharsetIndex = mm.read();
        serverStatus = mm.readUB2();
        mm.move(13);
        restOfScrambleBuff = mm.readBytesWithNull();
    }

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        protocolVersion = mm.read();
        serverVersion = mm.readBytesWithNull();
        threadId = mm.readUB4();
        seed = mm.readBytesWithNull();
        serverCapabilities = mm.readUB2();
        serverCharsetIndex = mm.read();
        serverStatus = mm.readUB2();
        mm.move(13);
        restOfScrambleBuff = mm.readBytesWithNull();
    }

    public void write(FrontendConnection c) {
        ByteBuffer buffer = c.allocate();
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetId);
        buffer.put(protocolVersion);
        BufferUtil.writeWithNull(buffer, serverVersion);
        BufferUtil.writeUB4(buffer, threadId);
        BufferUtil.writeWithNull(buffer, seed);
        BufferUtil.writeUB2(buffer, serverCapabilities);
        buffer.put(serverCharsetIndex);
        BufferUtil.writeUB2(buffer, serverStatus);
        buffer.put(FILLER_13);
        //        buffer.position(buffer.position() + 13);
        BufferUtil.writeWithNull(buffer, restOfScrambleBuff);
        c.write(buffer);
    }

    @Override
    public int calcPacketSize() {
        int size = 1;
        size += serverVersion.length;// n
        size += 5;// 1+4
        size += seed.length;// 8
        size += 19;// 1+2+1+2+13
        size += restOfScrambleBuff.length;// 12
        size += 1;// 1
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Handshake Packet";
    }

}
