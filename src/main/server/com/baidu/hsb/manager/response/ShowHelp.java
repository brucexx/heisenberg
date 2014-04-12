/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.hsb.config.Fields;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.mysql.PacketUtil;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.FieldPacket;
import com.baidu.hsb.net.mysql.ResultSetHeaderPacket;
import com.baidu.hsb.net.mysql.RowDataPacket;
import com.baidu.hsb.util.StringUtil;

/**
 * 打印CobarServer所支持的语句
 * 
 * @author xiongzhao@baidu.com 2010-9-29 下午05:17:15
 * @author wenfeng.cenwf 2011-4-13
 */
public final class ShowHelp {

    private static final int FIELD_COUNT = 2;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("STATEMENT", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("DESCRIPTION", Fields.FIELD_TYPE_VAR_STRING);
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
        for (String key : keys) {
            RowDataPacket row = getRow(key, helps.get(key), c.getCharset());
            row.packetId = ++packetId;
            buffer = row.write(buffer, c);
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // post write
        c.write(buffer);
    }

    private static RowDataPacket getRow(String stmt, String desc, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(stmt, charset));
        row.add(StringUtil.encode(desc, charset));
        return row;
    }

    private static final Map<String, String> helps = new HashMap<String, String>();
    private static final List<String> keys = new ArrayList<String>();
    static {
        // show
        helps.put("show @@time.current", "Report current timestamp");
        helps.put("show @@time.startup", "Report startup timestamp");
        helps.put("show @@version", "Report Cobar Server version");
        helps.put("show @@server", "Report server status");
        helps.put("show @@threadpool", "Report threadPool status");
        helps.put("show @@database", "Report databases");
        helps.put("show @@datanode", "Report dataNodes");
        helps.put("show @@datanode where schema = ?", "Report dataNodes");
        helps.put("show @@datasource where dataNode = ?", "Report dataSources");
        helps.put("show @@datasource", "Report dataSources");
        helps.put("show @@processor", "Report processor status");
        helps.put("show @@command", "Report commands status");
        helps.put("show @@connection", "Report connection status");
        helps.put("show @@backend", "Report backend connection status");
        helps.put("show @@connection.sql", "Report connection sql");
        helps.put("show @@sql.execute", "Report execute status");
        helps.put("show @@sql.detail where id = ?", "Report execute detail status");
        helps.put("show @@sql where id = ?", "Report specify SQL");
        helps.put("show @@sql.slow", "Report slow SQL");
        helps.put("show @@parser", "Report parser status");
        helps.put("show @@router", "Report router status");
        helps.put("show @@heartbeat", "Report heartbeat status");
        helps.put("show @@slow where schema = ?", "Report schema slow sql");
        helps.put("show @@slow where datanode = ?", "Report datanode slow sql");

        // switch
        helps.put("switch @@datasource name:index", "Switch dataSource");

        // kill
        helps.put("kill @@connection id1,id2,...", "Kill the specified connections");

        // stop
        helps.put("stop @@heartbeat name:time", "Pause dataNode heartbeat");

        // reload
        helps.put("reload @@config", "Reload all config from file");
        helps.put("reload @@route", "Reload route config from file");
        helps.put("reload @@user", "Reload user config from file");

        // rollback
        helps.put("rollback @@config", "Rollback all config from memory");
        helps.put("rollback @@route", "Rollback route config from memory");
        helps.put("rollback @@user", "Rollback user config from memory");

        // offline/online
        helps.put("offline", "Change Cobar status to OFF");
        helps.put("online", "Change Cobar status to ON");

        // clear
        helps.put("clear @@slow where schema = ?", "Clear slow sql by schema");
        helps.put("clear @@slow where datanode = ?", "Clear slow sql by datanode");

        // list sort
        keys.addAll(helps.keySet());
        Collections.sort(keys);
    }

}
