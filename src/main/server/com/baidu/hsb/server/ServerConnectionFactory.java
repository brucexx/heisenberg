/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server;

import java.nio.channels.SocketChannel;

import com.baidu.hsb.HeisenbergPrivileges;
import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.model.config.SystemConfig;
import com.baidu.hsb.net.FrontendConnection;
import com.baidu.hsb.net.factory.FrontendConnectionFactory;
import com.baidu.hsb.server.session.BlockingSession;
import com.baidu.hsb.server.session.NonBlockingSession;

/**
 * @author xiongzhao@baidu.com
 */
public class ServerConnectionFactory extends FrontendConnectionFactory {

    @Override
    protected FrontendConnection getConnection(SocketChannel channel) {
        SystemConfig sys = HeisenbergServer.getInstance().getConfig().getSystem();
        ServerConnection c = new ServerConnection(channel);
        c.setPrivileges(new HeisenbergPrivileges());
        c.setQueryHandler(new ServerQueryHandler(c));
        // c.setPrepareHandler(new ServerPrepareHandler(c)); TODO prepare
        c.setTxIsolation(sys.getTxIsolation());
        c.setSession(new BlockingSession(c));
        c.setSession2(new NonBlockingSession(c));
        return c;
    }

}
