/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.baidu.hsb.config.loader.ConfigLoader;
import com.baidu.hsb.config.loader.SchemaLoader;
import com.baidu.hsb.config.loader.xml.XMLConfigLoader;
import com.baidu.hsb.config.loader.xml.XMLSchemaLoader;
import com.baidu.hsb.config.model.config.DataNodeConfig;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.config.model.config.QuarantineConfig;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.config.model.config.SystemConfig;
import com.baidu.hsb.config.model.config.UserConfig;
import com.baidu.hsb.config.util.ConfigException;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.mysql.MySQLDataSource;
import com.baidu.hsb.util.SplitUtil;

/**
 * @author xiongzhao@baidu.com
 */
public class ConfigInitializer {
    private volatile SystemConfig                  system;
    private volatile HeisenbergCluster             cluster;
    private volatile QuarantineConfig              quarantine;
    private volatile Map<String, UserConfig>       users;
    private volatile Map<String, SchemaConfig>     schemas;
    private volatile Map<String, MySQLDataNode>    dataNodes;
    private volatile Map<String, DataSourceConfig> dataSources;

    public ConfigInitializer() {
        boolean selfCp = HeisenbergStartup.hasSelfConfigPath();

        SchemaLoader schemaLoader = null;
        if (selfCp) {
            schemaLoader = new XMLSchemaLoader(HeisenbergStartup.getConfigPath());
        } else {
            schemaLoader = new XMLSchemaLoader();
        }

        XMLConfigLoader configLoader = new XMLConfigLoader(schemaLoader,
            HeisenbergStartup.getConfigPath());
        //        try {
        //           //RouteRuleInitializer.initRouteRule(schemaLoader);
        //            schemaLoader = null;
        //        } catch (SQLSyntaxErrorException e) {
        //            throw new ConfigException(e);
        //        }
        this.system = configLoader.getSystemConfig();
        this.users = configLoader.getUserConfigs();
        this.schemas = configLoader.getSchemaConfigs();
        this.dataSources = configLoader.getDataSources();
        this.dataNodes = initDataNodes(configLoader);
        this.quarantine = configLoader.getQuarantineConfig();
        this.cluster = initCobarCluster(configLoader);

        this.checkConfig();
    }

    private void checkConfig() throws ConfigException {
        if (users == null || users.isEmpty())
            return;
        for (UserConfig uc : users.values()) {
            if (uc == null) {
                continue;
            }
            Set<String> authSchemas = uc.getSchemas();
            if (authSchemas == null) {
                continue;
            }
            for (String schema : authSchemas) {
                if (!schemas.containsKey(schema)) {
                    String errMsg = "schema " + schema + " refered by user " + uc.getName()
                                    + " is not exist!";
                    throw new ConfigException(errMsg);
                }
            }
        }

        for (SchemaConfig sc : schemas.values()) {
            if (null == sc) {
                continue;
            }
            String g = sc.getGroup();
            if (!cluster.getGroups().containsKey(g)) {
                throw new ConfigException("[group:" + g + "] refered by [schema:" + sc.getName()
                                          + "] is not exist!");
            }
        }
    }

    public SystemConfig getSystem() {
        return system;
    }

    public HeisenbergCluster getCluster() {
        return cluster;
    }

    public QuarantineConfig getQuarantine() {
        return quarantine;
    }

    public Map<String, UserConfig> getUsers() {
        return users;
    }

    public Map<String, SchemaConfig> getSchemas() {
        return schemas;
    }

    public Map<String, MySQLDataNode> getDataNodes() {
        return dataNodes;
    }

    public Map<String, DataSourceConfig> getDataSources() {
        return dataSources;
    }

    private HeisenbergCluster initCobarCluster(ConfigLoader configLoader) {
        return new HeisenbergCluster(configLoader.getClusterConfig());
    }

    private Map<String, MySQLDataNode> initDataNodes(ConfigLoader configLoader) {
        Map<String, DataNodeConfig> nodeConfs = configLoader.getDataNodes();
        Map<String, MySQLDataNode> nodes = new HashMap<String, MySQLDataNode>(nodeConfs.size());
        for (DataNodeConfig conf : nodeConfs.values()) {
            MySQLDataNode dataNode = getDataNode(conf, configLoader);
            if (nodes.containsKey(dataNode.getName())) {
                throw new ConfigException("dataNode " + dataNode.getName() + " duplicated!");
            }
            nodes.put(dataNode.getName(), dataNode);
        }
        return nodes;
    }

    private MySQLDataNode getDataNode(DataNodeConfig dnc, ConfigLoader configLoader) {
        String[] dsNames = SplitUtil.split(dnc.getDataSource(), ',');
        checkDataSourceExists(dsNames);
        MySQLDataNode node = new MySQLDataNode(dnc);
        MySQLDataSource[] dsList = new MySQLDataSource[dsNames.length];
        int size = dnc.getPoolSize();
        for (int i = 0; i < dsList.length; i++) {
            DataSourceConfig dsc = dataSources.get(dsNames[i]);
            dsList[i] = new MySQLDataSource(node, i, dsc, size);
        }
        node.setSources(dsList);
        return node;
    }

    private void checkDataSourceExists(String... nodes) {
        if (nodes == null || nodes.length < 1) {
            return;
        }
        for (String node : nodes) {
            if (!dataSources.containsKey(node)) {
                throw new ConfigException("dataSource '" + node + "' is not found!");
            }
        }
    }
}
