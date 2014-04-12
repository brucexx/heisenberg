/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.response;

import java.util.Map;

import org.apache.log4j.Logger;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.manager.parser.ManagerParseStop;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.parser.util.Pair;
import com.baidu.hsb.util.FormatUtil;
import com.baidu.hsb.util.TimeUtil;

/**
 * 暂停数据节点心跳检测
 * 
 * @author xiongzhao@baidu.com
 */
public final class StopHeartbeat {

    private static final Logger logger = Logger.getLogger(StopHeartbeat.class);

    public static void execute(String stmt, ManagerConnection c) {
        int count = 0;
        Pair<String[], Integer> keys = ManagerParseStop.getPair(stmt);
        if (keys.getKey() != null && keys.getValue() != null) {
            long time = keys.getValue().intValue() * 1000L;
            Map<String, MySQLDataNode> dns = HeisenbergServer.getInstance().getConfig().getDataNodes();
            for (String key : keys.getKey()) {
                MySQLDataNode dn = dns.get(key);
                if (dn != null) {
                    dn.setHeartbeatRecoveryTime(TimeUtil.currentTimeMillis() + time);
                    ++count;
                    StringBuilder s = new StringBuilder();
                    s.append(dn.getName()).append(" stop heartbeat '");
                    logger.warn(s.append(FormatUtil.formatTime(time, 3)).append("' by manager."));
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
