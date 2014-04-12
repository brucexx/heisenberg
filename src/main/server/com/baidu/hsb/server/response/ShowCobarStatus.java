/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server.response;

import java.nio.ByteBuffer;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.Fields;
import com.baidu.hsb.mysql.PacketUtil;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.FieldPacket;
import com.baidu.hsb.net.mysql.ResultSetHeaderPacket;
import com.baidu.hsb.net.mysql.RowDataPacket;
import com.baidu.hsb.server.ServerConnection;

/**
 * 加入了offline状态推送，用于心跳语句。
 * 
 * @author xiongzhao@baidu.com
 * @author xiongzhao@baidu.com
 */
public class ShowCobarStatus {

    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    private static final RowDataPacket status = new RowDataPacket(FIELD_COUNT);
    private static final EOFPacket lastEof = new EOFPacket();
    private static final ErrorPacket error = PacketUtil.getShutdown();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("STATUS", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
        status.add("ON".getBytes());
        status.packetId = ++packetId;
        lastEof.packetId = ++packetId;
    }

    public static void response(ServerConnection c) {
        if (HeisenbergServer.getInstance().isOnline()) {
            ByteBuffer buffer = c.allocate();
            buffer = header.write(buffer, c);
            for (FieldPacket field : fields) {
                buffer = field.write(buffer, c);
            }
            buffer = eof.write(buffer, c);
            buffer = status.write(buffer, c);
            buffer = lastEof.write(buffer, c);
            c.write(buffer);
        } else {
            error.write(c);
        }
    }

}
