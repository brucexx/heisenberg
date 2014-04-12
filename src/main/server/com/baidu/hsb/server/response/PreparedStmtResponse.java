/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server.response;

import java.nio.ByteBuffer;

import com.baidu.hsb.mysql.PreparedStatement;
import com.baidu.hsb.net.FrontendConnection;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.FieldPacket;
import com.baidu.hsb.net.mysql.PreparedOkPacket;

/**
 * @author xiongzhao@baidu.com 2012-8-28
 */
public class PreparedStmtResponse {

    public static void response(PreparedStatement pstmt, FrontendConnection c) {
        byte packetId = 0;

        // write preparedOk packet
        PreparedOkPacket preparedOk = new PreparedOkPacket();
        preparedOk.packetId = ++packetId;
        preparedOk.statementId = pstmt.getId();
        preparedOk.columnsNumber = pstmt.getColumnsNumber();
        preparedOk.parametersNumber = pstmt.getParametersNumber();
        ByteBuffer buffer = preparedOk.write(c.allocate(), c);

        // write parameter field packet
        int parametersNumber = preparedOk.parametersNumber;
        if (parametersNumber > 0) {
            for (int i = 0; i < parametersNumber; i++) {
                FieldPacket field = new FieldPacket();
                field.packetId = ++packetId;
                buffer = field.write(buffer, c);
            }
            EOFPacket eof = new EOFPacket();
            eof.packetId = ++packetId;
            buffer = eof.write(buffer, c);
        }

        // write column field packet
        int columnsNumber = preparedOk.columnsNumber;
        if (columnsNumber > 0) {
            for (int i = 0; i < columnsNumber; i++) {
                FieldPacket field = new FieldPacket();
                field.packetId = ++packetId;
                buffer = field.write(buffer, c);
            }
            EOFPacket eof = new EOFPacket();
            eof.packetId = ++packetId;
            buffer = eof.write(buffer, c);
        }

        // send buffer
        c.write(buffer);
    }

}
