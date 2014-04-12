/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.response;

import java.util.Map;

import com.baidu.hsb.HeisenbergConfig;
import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.mysql.MySQLDataSource;
import com.baidu.hsb.net.mysql.OkPacket;

/**
 * @author xiongzhao@baidu.com 2012-4-16
 */
public class ClearSlow {

    public static void dataNode(ManagerConnection c, String name) {
        MySQLDataNode dn = HeisenbergServer.getInstance().getConfig().getDataNodes().get(name);
        MySQLDataSource ds = null;
        if (dn != null && (ds = dn.getSource()) != null) {
            ds.getSqlRecorder().clear();
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        } else {
            c.writeErrMessage(ErrorCode.ER_YES, "Invalid DataNode:" + name);
        }
    }

    public static void schema(ManagerConnection c, String name) {
        HeisenbergConfig conf = HeisenbergServer.getInstance().getConfig();
        SchemaConfig schema = conf.getSchemas().get(name);
        if (schema != null) {
            Map<String, MySQLDataNode> dataNodes = conf.getDataNodes();
            for (String n : schema.getAllDataNodes()) {
                MySQLDataNode dn = dataNodes.get(n);
                MySQLDataSource ds = null;
                if (dn != null && (ds = dn.getSource()) != null) {
                    ds.getSqlRecorder().clear();
                }
            }
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        } else {
            c.writeErrMessage(ErrorCode.ER_YES, "Invalid Schema:" + name);
        }
    }

}
