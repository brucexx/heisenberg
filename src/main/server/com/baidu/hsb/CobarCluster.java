/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.hsb.config.model.config.ClusterConfig;
import com.baidu.hsb.config.model.config.CobarNodeConfig;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: CobarCluster.java, v 0.1 2013年12月31日 下午1:39:33 HI:brucest0078 Exp $
 */
public final class CobarCluster {

    private final Map<String, CobarNode> nodes;
    private final Map<String, List<String>> groups;

    public CobarCluster(ClusterConfig clusterConf) {
        this.nodes = new HashMap<String, CobarNode>(clusterConf.getNodes().size());
        this.groups = clusterConf.getGroups();
        for (CobarNodeConfig conf : clusterConf.getNodes().values()) {
            String name = conf.getName();
            CobarNode node = new CobarNode(conf);
            this.nodes.put(name, node);
        }
    }

    public Map<String, CobarNode> getNodes() {
        return nodes;
    }

    public Map<String, List<String>> getGroups() {
        return groups;
    }

}
