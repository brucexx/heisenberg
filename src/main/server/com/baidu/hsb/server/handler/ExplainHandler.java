/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server.handler;

import java.nio.ByteBuffer;
import java.sql.SQLNonTransientException;

import org.apache.log4j.Logger;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.config.Fields;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.mysql.PacketUtil;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.FieldPacket;
import com.baidu.hsb.net.mysql.ResultSetHeaderPacket;
import com.baidu.hsb.net.mysql.RowDataPacket;
import com.baidu.hsb.route.HServerRouter;
import com.baidu.hsb.route.RouteResultset;
import com.baidu.hsb.route.RouteResultsetNode;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.util.StringUtil;

/**
 * @author xiongzhao@baidu.com
 */
public class ExplainHandler {

    private static final Logger               logger      = Logger.getLogger(ExplainHandler.class);
    private static final RouteResultsetNode[] EMPTY_ARRAY = new RouteResultsetNode[0];
    private static final int                  FIELD_COUNT = 2;
    private static final FieldPacket[]        fields      = new FieldPacket[FIELD_COUNT];
    static {
        fields[0] = PacketUtil.getField("DATA_NODE", Fields.FIELD_TYPE_VAR_STRING);
        fields[1] = PacketUtil.getField("SQL", Fields.FIELD_TYPE_VAR_STRING);
    }

    public static void handle(String stmt, ServerConnection c, int offset) {
        stmt = stmt.substring(offset);

        RouteResultset rrs = getRouteResultset(c, stmt);
        if (rrs == null)
            return;

        ByteBuffer buffer = c.allocate();

        // write header
        ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
        byte packetId = header.packetId;
        buffer = header.write(buffer, c);

        // write fields
        for (FieldPacket field : fields) {
            field.packetId = ++packetId;
            buffer = field.write(buffer, c);
        }

        // write eof
        EOFPacket eof = new EOFPacket();
        eof.packetId = ++packetId;
        buffer = eof.write(buffer, c);

        // write rows
        RouteResultsetNode[] rrsn = (rrs != null) ? rrs.getNodes() : EMPTY_ARRAY;
        for (RouteResultsetNode node : rrsn) {
            String[] statement = node.getStatement();
            for (String sqlStmt : statement) {
                RowDataPacket row = getRow(node.getName(), sqlStmt, c.getCharset());
                row.packetId = ++packetId;
                buffer = row.write(buffer, c);
            }
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // post write
        c.write(buffer);

    }

    private static RowDataPacket getRow(String nodeName, String stmt, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(nodeName, charset));
        row.add(StringUtil.encode(stmt, charset));
        return row;
    }

    private static RouteResultset getRouteResultset(ServerConnection c, String stmt) {
        String db = c.getSchema();
        if (db == null) {
            c.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
            return null;
        }
        SchemaConfig schema = HeisenbergServer.getInstance().getConfig().getSchemas().get(db);
        if (schema == null) {
            c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
            return null;
        }
        try {
            return HServerRouter.route(schema, stmt, c.getCharset(), c);
        } catch (SQLNonTransientException e) {
            StringBuilder s = new StringBuilder();
            logger.warn(s.append(c).append(stmt).toString(), e);
            String msg = e.getMessage();
            c.writeErrMessage(ErrorCode.ER_PARSE_ERROR, msg == null ? e.getClass().getSimpleName()
                : msg);
            return null;
        }
    }

}
