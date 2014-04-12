/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.baidu.hsb.HeisenbergConfig;
import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.Fields;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.mysql.MySQLDataSource;
import com.baidu.hsb.mysql.PacketUtil;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.FieldPacket;
import com.baidu.hsb.net.mysql.ResultSetHeaderPacket;
import com.baidu.hsb.net.mysql.RowDataPacket;
import com.baidu.hsb.parser.util.Pair;
import com.baidu.hsb.parser.util.PairUtil;
import com.baidu.hsb.util.IntegerUtil;
import com.baidu.hsb.util.StringUtil;

/**
 * 查看数据源信息
 * 
 * @author xiongzhao@baidu.com 2010-9-26 下午04:56:26
 * @author wenfeng.cenwf 2011-4-25
 */
public final class ShowDataSource {

    private static final int FIELD_COUNT = 5;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("TYPE", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("PORT", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("SCHEMA", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void execute(ManagerConnection c, String name) {
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
        HeisenbergConfig conf = HeisenbergServer.getInstance().getConfig();
        Map<String, DataSourceConfig> dataSources = conf.getDataSources();
        List<String> keys = new ArrayList<String>();
        if (null != name) {
            MySQLDataNode dn = conf.getDataNodes().get(name);
            if (dn != null)
                for (MySQLDataSource ds : dn.getSources()) {
                    if (ds != null) {
                        keys.add(ds.getName());
                    }
                }
        } else {
            keys.addAll(dataSources.keySet());
        }
        Collections.sort(keys, new Comparators<String>());
        for (String key : keys) {
            RowDataPacket row = getRow(dataSources.get(key), c.getCharset());
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

    private static RowDataPacket getRow(DataSourceConfig dsc, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(dsc.getName(), charset));
        row.add(StringUtil.encode(dsc.getType(), charset));
        row.add(StringUtil.encode(dsc.getHost(), charset));
        row.add(IntegerUtil.toBytes(dsc.getPort()));
        row.add(StringUtil.encode(dsc.getDatabase(), charset));
        return row;
    }

    private static final class Comparators<T> implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            Pair<String, Integer> p1 = PairUtil.splitIndex(s1, '[', ']');
            Pair<String, Integer> p2 = PairUtil.splitIndex(s2, '[', ']');
            if (p1.getKey().compareTo(p2.getKey()) == 0) {
                return p1.getValue() - p2.getValue();
            } else {
                return p1.getKey().compareTo(p2.getKey());
            }
        }
    }

}
