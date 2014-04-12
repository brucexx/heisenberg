/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager;

import org.apache.log4j.Logger;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.manager.handler.ClearHandler;
import com.baidu.hsb.manager.handler.ReloadHandler;
import com.baidu.hsb.manager.handler.RollbackHandler;
import com.baidu.hsb.manager.handler.SelectHandler;
import com.baidu.hsb.manager.handler.ShowHandler;
import com.baidu.hsb.manager.handler.StopHandler;
import com.baidu.hsb.manager.handler.SwitchHandler;
import com.baidu.hsb.manager.parser.ManagerParse;
import com.baidu.hsb.manager.response.KillConnection;
import com.baidu.hsb.manager.response.Offline;
import com.baidu.hsb.manager.response.Online;
import com.baidu.hsb.net.handler.FrontendQueryHandler;
import com.baidu.hsb.net.mysql.OkPacket;

/**
 * @author xiongzhao@baidu.com
 */
public class ManagerQueryHandler implements FrontendQueryHandler {
    private static final Logger LOGGER = Logger.getLogger(ManagerQueryHandler.class);

    private final ManagerConnection source;

    public ManagerQueryHandler(ManagerConnection source) {
        this.source = source;
    }

    @Override
    public void query(String sql) {
        ManagerConnection c = this.source;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder().append(c).append(sql).toString());
        }
        int rs = ManagerParse.parse(sql);
        switch (rs & 0xff) {
        case ManagerParse.SELECT:
            SelectHandler.handle(sql, c, rs >>> 8);
            break;
        case ManagerParse.SET:
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
            break;
        case ManagerParse.SHOW:
            ShowHandler.handle(sql, c, rs >>> 8);
            break;
        case ManagerParse.SWITCH:
            SwitchHandler.handler(sql, c, rs >>> 8);
            break;
        case ManagerParse.KILL_CONN:
            KillConnection.response(sql, rs >>> 8, c);
            break;
        case ManagerParse.OFFLINE:
            Offline.execute(sql, c);
            break;
        case ManagerParse.ONLINE:
            Online.execute(sql, c);
            break;
        case ManagerParse.STOP:
            StopHandler.handle(sql, c, rs >>> 8);
            break;
        case ManagerParse.RELOAD:
            ReloadHandler.handle(sql, c, rs >>> 8);
            break;
        case ManagerParse.ROLLBACK:
            RollbackHandler.handle(sql, c, rs >>> 8);
            break;
        case ManagerParse.CLEAR:
            ClearHandler.handle(sql, c, rs >>> 8);
            break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
