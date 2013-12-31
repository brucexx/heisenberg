/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.response;

import org.apache.log4j.Logger;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.net.mysql.OkPacket;

/**
 * @author xiongzhao@baidu.com
 */
public final class ReloadUser {

    private static final Logger logger = Logger.getLogger(ReloadUser.class);

    public static void execute(ManagerConnection c) {
        boolean status = false;
        if (status) {
            StringBuilder s = new StringBuilder();
            s.append(c).append("Reload userConfig success by manager");
            logger.warn(s.toString());
            OkPacket ok = new OkPacket();
            ok.packetId = 1;
            ok.affectedRows = 1;
            ok.serverStatus = 2;
            ok.message = "Reload userConfig success".getBytes();
            ok.write(c);
        } else {
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
