/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.mysql;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.baidu.hsb.mysql.BufferUtil;
import com.baidu.hsb.mysql.MySQLMessage;
import com.baidu.hsb.net.FrontendConnection;

/**
 * From server to client. One packet for each row in the result set.
 * 
 * <pre>
 * Bytes                   Name
 * -----                   ----
 * n (Length Coded String) (column value)
 * ...
 * 
 * (column value):         The data in the column, as a character string.
 *                         If a column is defined as non-character, the
 *                         server converts the value into a character
 *                         before sending it. Since the value is a Length
 *                         Coded String, a NULL can be represented with a
 *                         single byte containing 251(see the description
 *                         of Length Coded Strings in section "Elements" above).
 * 
 * @see http://forge.mysql.com/wiki/MySQL_Internals_ClientServer_Protocol#Row_Data_Packet
 * </pre>
 * 
 * @author xiongzhao@baidu.com
 */
public class RowDataPacket extends MySQLPacket {
    private static final byte NULL_MARK = (byte) 251;

    public final int fieldCount;
    public final List<byte[]> fieldValues;

    public RowDataPacket(int fieldCount) {
        this.fieldCount = fieldCount;
        this.fieldValues = new ArrayList<byte[]>(fieldCount);
    }

    public void add(byte[] value) {
        fieldValues.add(value);
    }

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        for (int i = 0; i < fieldCount; i++) {
            fieldValues.add(mm.readBytesWithLength());
        }
    }

    @Override
    public ByteBuffer write(ByteBuffer bb, FrontendConnection c) {
        bb = c.checkWriteBuffer(bb, c.getPacketHeaderSize());
        BufferUtil.writeUB3(bb, calcPacketSize());
        bb.put(packetId);
        for (int i = 0; i < fieldCount; i++) {
            byte[] fv = fieldValues.get(i);
            if (fv == null || fv.length == 0) {
                bb = c.checkWriteBuffer(bb, 1);
                bb.put(RowDataPacket.NULL_MARK);
            } else {
                bb = c.checkWriteBuffer(bb, BufferUtil.getLength(fv.length));
                BufferUtil.writeLength(bb, fv.length);
                bb = c.writeToBuffer(fv, bb);
            }
        }
        return bb;
    }

    @Override
    public int calcPacketSize() {
        int size = 0;
        for (int i = 0; i < fieldCount; i++) {
            byte[] v = fieldValues.get(i);
            size += (v == null || v.length == 0) ? 1 : BufferUtil.getLength(v);
        }
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL RowData Packet";
    }

}
