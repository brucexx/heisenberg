/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server;

import org.apache.log4j.Logger;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.net.handler.FrontendQueryHandler;
import com.baidu.hsb.server.handler.BeginHandler;
import com.baidu.hsb.server.handler.ExplainHandler;
import com.baidu.hsb.server.handler.KillHandler;
import com.baidu.hsb.server.handler.SavepointHandler;
import com.baidu.hsb.server.handler.SelectHandler;
import com.baidu.hsb.server.handler.SetHandler;
import com.baidu.hsb.server.handler.ShowHandler;
import com.baidu.hsb.server.handler.StartHandler;
import com.baidu.hsb.server.handler.UseHandler;
import com.baidu.hsb.server.parser.ServerParse;

/**
 * @author xiongzhao@baidu.com
 */
public class ServerQueryHandler implements FrontendQueryHandler {
    private static final Logger LOGGER = Logger.getLogger(ServerQueryHandler.class);

    private final ServerConnection source;

    public ServerQueryHandler(ServerConnection source) {
        this.source = source;
    }

    @Override
    public void query(String sql) {
        ServerConnection c = this.source;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder().append(c).append(sql).toString());
        }
        int rs = ServerParse.parse(sql);
        switch (rs & 0xff) {
        case ServerParse.EXPLAIN:
            ExplainHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.SET:
            SetHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.SHOW:
            ShowHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.SELECT:
            SelectHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.START:
            StartHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.BEGIN:
            BeginHandler.handle(sql, c);
            break;
        case ServerParse.SAVEPOINT:
            SavepointHandler.handle(sql, c);
            break;
        case ServerParse.KILL:
            KillHandler.handle(sql, rs >>> 8, c);
            break;
        case ServerParse.KILL_QUERY:
            c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
            break;
        case ServerParse.USE:
            UseHandler.handle(sql, c, rs >>> 8);
            break;
        case ServerParse.COMMIT:
            c.commit();
            break;
        case ServerParse.ROLLBACK:
            c.rollback();
            break;
        default:
            c.execute(sql, rs);
        }
    }

}
