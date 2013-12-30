/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.hsb.server.response;

import java.nio.ByteBuffer;

import com.baidu.hsb.config.Fields;
import com.baidu.hsb.mysql.PacketUtil;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.FieldPacket;
import com.baidu.hsb.net.mysql.ResultSetHeaderPacket;
import com.baidu.hsb.net.mysql.RowDataPacket;
import com.baidu.hsb.parser.util.ParseUtil;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.util.LongUtil;

/**
 * @author xianmao.hexm
 */
public class SelectLastInsertId {

    private static final String ORG_NAME = "LAST_INSERT_ID()";
    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    static {
        byte packetId = 0;
        header.packetId = ++packetId;
    }

    public static void response(ServerConnection c, String stmt, int aliasIndex) {
        String alias = ParseUtil.parseAlias(stmt, aliasIndex);
        if (alias == null) {
            alias = ORG_NAME;
        }

        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c);

        // write fields
        byte packetId = header.packetId;
        FieldPacket field = PacketUtil.getField(alias, ORG_NAME, Fields.FIELD_TYPE_LONGLONG);
        field.packetId = ++packetId;
        buffer = field.write(buffer, c);

        // write eof
        EOFPacket eof = new EOFPacket();
        eof.packetId = ++packetId;
        buffer = eof.write(buffer, c);

        // write rows
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(LongUtil.toBytes(c.getLastInsertId()));
        row.packetId = ++packetId;
        buffer = row.write(buffer, c);

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // post write
        c.write(buffer);
    }

}
