/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.response;

import java.nio.ByteBuffer;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.Fields;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.mysql.PacketUtil;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.FieldPacket;
import com.baidu.hsb.net.mysql.ResultSetHeaderPacket;
import com.baidu.hsb.net.mysql.RowDataPacket;
import com.baidu.hsb.statistic.SQLRecord;
import com.baidu.hsb.util.IntegerUtil;
import com.baidu.hsb.util.LongUtil;
import com.baidu.hsb.util.StringUtil;

/**
 * 查询执行时间超过设定阈值的SQL
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: ShowSQLSlow.java, v 0.1 2013年12月31日 下午1:25:19 HI:brucest0078 Exp $
 */
public final class ShowSQLSlow {

    private static final int FIELD_COUNT = 7;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("SCHEMA", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("DATASOURCE", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("INDEX", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("START_TIME", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("EXECUTE_TIME", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("SQL", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void execute(ManagerConnection c) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c);

        // write fields
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c);
        }

        // write eof
        buffer = eof.write(buffer, c);

        // write rows
        byte packetId = eof.packetId;
        SQLRecord[] records = HeisenbergServer.getInstance().getSqlRecorder().getRecords();
        for (int i = records.length - 1; i >= 0; i--) {
            if (records[i] != null) {
                RowDataPacket row = getRow(records[i], c.getCharset());
                row.packetId = ++packetId;
                buffer = row.write(buffer, c);
            }
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // write buffer
        c.write(buffer);
    }

    private static RowDataPacket getRow(SQLRecord sql, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(sql.host, charset));
        row.add(StringUtil.encode(sql.schema, charset));
        row.add(StringUtil.encode(sql.dataNode, charset));
        row.add(IntegerUtil.toBytes(sql.dataNodeIndex));
        row.add(LongUtil.toBytes(sql.startTime));
        row.add(LongUtil.toBytes(sql.executeTime));
        row.add(StringUtil.encode(sql.statement, charset));
        return row;
    }

}
