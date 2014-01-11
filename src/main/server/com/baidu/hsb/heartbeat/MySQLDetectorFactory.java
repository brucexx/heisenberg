/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.heartbeat;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.model.config.DataNodeConfig;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.net.factory.BackendConnectionFactory;

/**
 * @author xiongzhao@baidu.com
 */
public class MySQLDetectorFactory extends BackendConnectionFactory {

    public MySQLDetectorFactory() {
        this.idleTimeout = 300 * 1000L;
    }

    public MySQLDetector make(MySQLHeartbeat heartbeat) throws IOException {
        SocketChannel channel = openSocketChannel();
        DataSourceConfig dsc = heartbeat.getSource().getConfig();
        DataNodeConfig dnc = heartbeat.getSource().getNode().getConfig();
        MySQLDetector detector = new MySQLDetector(channel);
        detector.setHost(dsc.getHost());
        detector.setPort(dsc.getPort());
        detector.setUser(dsc.getUser());
        detector.setPassword(dsc.getPassword());
        detector.setSchema(dsc.getDatabase());
        detector.setHeartbeatTimeout(dnc.getHeartbeatTimeout());
        detector.setHeartbeat(heartbeat);
        postConnect(detector, HeisenbergServer.getInstance().getConnector());
        return detector;
    }

}
