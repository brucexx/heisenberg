/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.hsb.heartbeat;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.baidu.hsb.CobarServer;
import com.baidu.hsb.config.model.config.CobarNodeConfig;
import com.baidu.hsb.config.model.config.SystemConfig;
import com.baidu.hsb.net.factory.BackendConnectionFactory;

/**
 * @author xianmao.hexm
 */
public class CobarDetectorFactory extends BackendConnectionFactory {

    public CobarDetectorFactory() {
        this.idleTimeout = 120 * 1000L;
    }

    public CobarDetector make(CobarHeartbeat heartbeat) throws IOException {
        SocketChannel channel = openSocketChannel();
        CobarNodeConfig cnc = heartbeat.getNode().getConfig();
        SystemConfig sys = CobarServer.getInstance().getConfig().getSystem();
        CobarDetector detector = new CobarDetector(channel);
        detector.setHost(cnc.getHost());
        detector.setPort(cnc.getPort());
        detector.setUser(sys.getClusterHeartbeatUser());
        detector.setPassword(sys.getClusterHeartbeatPass());
        detector.setHeartbeatTimeout(sys.getClusterHeartbeatTimeout());
        detector.setHeartbeat(heartbeat);
        postConnect(detector, CobarServer.getInstance().getConnector());
        return detector;
    }

}
