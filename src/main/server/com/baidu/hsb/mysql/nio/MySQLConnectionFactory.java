/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.mysql.nio.handler.ResponseHandler;
import com.baidu.hsb.net.factory.BackendConnectionFactory;

/**
 * @author xiongzhao@baidu.com 2012-4-12
 */
public class MySQLConnectionFactory extends BackendConnectionFactory {

    public MySQLConnection make(MySQLConnectionPool pool, ResponseHandler handler) throws IOException {
        SocketChannel channel = openSocketChannel();
        DataSourceConfig dsc = pool.getConfig();
        MySQLConnection c = new MySQLConnection(channel);
        c.setHost(dsc.getHost());
        c.setPort(dsc.getPort());
        c.setUser(dsc.getUser());
        c.setPassword(dsc.getPassword());
        c.setSchema(dsc.getDatabase());
        c.setHandler(new MySQLConnectionAuthenticator(c, handler));
        c.setPool(pool);
        postConnect(c, HeisenbergServer.getInstance().getConnector());
        return c;
    }

}
