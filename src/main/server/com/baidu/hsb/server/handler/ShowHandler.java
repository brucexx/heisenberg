/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server.handler;

import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.server.parser.ServerParse;
import com.baidu.hsb.server.parser.ServerParseShow;
import com.baidu.hsb.server.response.ShowCobarCluster;
import com.baidu.hsb.server.response.ShowCobarStatus;
import com.baidu.hsb.server.response.ShowDataSources;
import com.baidu.hsb.server.response.ShowDatabases;

/**
 * @author xiongzhao@baidu.com
 */
public final class ShowHandler {

    public static void handle(String stmt, ServerConnection c, int offset) {
        switch (ServerParseShow.parse(stmt, offset)) {
        case ServerParseShow.DATABASES:
            ShowDatabases.response(c);
            break;
        case ServerParseShow.DATASOURCES:
            ShowDataSources.response(c);
            break;
        case ServerParseShow.COBAR_STATUS:
            ShowCobarStatus.response(c);
            break;
        case ServerParseShow.COBAR_CLUSTER:
            ShowCobarCluster.response(c);
            break;
        default:
            c.execute(stmt, ServerParse.SHOW);
        }
    }

}
