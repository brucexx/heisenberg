/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server.response;

import org.apache.log4j.Logger;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.HeartbeatPacket;
import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.util.TimeUtil;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: Heartbeat.java, v 0.1 2013年12月31日 下午1:33:43 HI:brucest0078 Exp $
 */
public class Heartbeat {

    private static final Logger HEARTBEAT = Logger.getLogger("heartbeat");

    public static void response(ServerConnection c, byte[] data) {
        HeartbeatPacket hp = new HeartbeatPacket();
        hp.read(data);
        if (HeisenbergServer.getInstance().isOnline()) {
            OkPacket ok = new OkPacket();
            ok.packetId = 1;
            ok.affectedRows = hp.id;
            ok.serverStatus = 2;
            ok.write(c);
            if (HEARTBEAT.isInfoEnabled()) {
                HEARTBEAT.info(responseMessage("OK", c, hp.id));
            }
        } else {
            ErrorPacket error = new ErrorPacket();
            error.packetId = 1;
            error.errno = ErrorCode.ER_SERVER_SHUTDOWN;
            error.message = String.valueOf(hp.id).getBytes();
            error.write(c);
            if (HEARTBEAT.isInfoEnabled()) {
                HEARTBEAT.info(responseMessage("ERROR", c, hp.id));
            }
        }
    }

    private static String responseMessage(String action, ServerConnection c, long id) {
        return new StringBuilder("RESPONSE:").append(action).append(", id=").append(id)
            .append(", host=").append(c.getHost()).append(", port=").append(c.getPort())
            .append(", time=").append(TimeUtil.currentTimeMillis()).toString();
    }

}
