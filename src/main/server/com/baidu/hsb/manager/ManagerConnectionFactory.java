/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager;

import java.nio.channels.SocketChannel;

import com.baidu.hsb.HeisenbergPrivileges;
import com.baidu.hsb.net.FrontendConnection;
import com.baidu.hsb.net.factory.FrontendConnectionFactory;

/**
 * @author xiongzhao@baidu.com
 */
public class ManagerConnectionFactory extends FrontendConnectionFactory {

    @Override
    protected FrontendConnection getConnection(SocketChannel channel) {
        ManagerConnection c = new ManagerConnection(channel);
        c.setPrivileges(new HeisenbergPrivileges());
        c.setQueryHandler(new ManagerQueryHandler(c));
        return c;
    }

}
