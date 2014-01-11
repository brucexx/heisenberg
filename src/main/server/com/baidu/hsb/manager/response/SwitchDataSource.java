/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.response;

import java.util.Map;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.manager.parser.ManagerParseSwitch;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.parser.util.Pair;

/**
 * 切换数据节点的数据源
 * 
 * @author xiongzhao@baidu.com 2011-5-31 下午01:19:36
 */
public final class SwitchDataSource {

    public static void response(String stmt, ManagerConnection c) {
        int count = 0;
        Pair<String[], Integer> pair = ManagerParseSwitch.getPair(stmt);
        Map<String, MySQLDataNode> dns = HeisenbergServer.getInstance().getConfig().getDataNodes();
        Integer idx = pair.getValue();
        for (String key : pair.getKey()) {
            MySQLDataNode dn = dns.get(key);
            if (dn != null) {
                int m = dn.getActivedIndex();
                int n = (idx == null) ? dn.next(m) : idx.intValue();
                if (dn.switchSource(n, false, "MANAGER")) {
                    ++count;
                }
            }
        }
        OkPacket packet = new OkPacket();
        packet.packetId = 1;
        packet.affectedRows = count;
        packet.serverStatus = 2;
        packet.write(c);
    }

}
