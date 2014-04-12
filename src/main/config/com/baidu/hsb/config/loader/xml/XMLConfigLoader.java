/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.loader.xml;

import java.util.Map;

import com.baidu.hsb.config.loader.ConfigLoader;
import com.baidu.hsb.config.loader.SchemaLoader;
import com.baidu.hsb.config.model.config.ClusterConfig;
import com.baidu.hsb.config.model.config.DataNodeConfig;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.config.model.config.QuarantineConfig;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.config.model.config.SystemConfig;
import com.baidu.hsb.config.model.config.UserConfig;

/**
 * @author xiongzhao@baidu.com
 */
public class XMLConfigLoader implements ConfigLoader {
    /** unmodifiable */
    // private final Set<RuleConfig> rules;
    /** unmodifiable */
    //private final Map<String, RuleAlgorithm> functions;
    /** unmodifiable */
    private final Map<String, DataSourceConfig> dataSources;
    /** unmodifiable */
    private final Map<String, DataNodeConfig>   dataNodes;
    /** unmodifiable */
    private final Map<String, SchemaConfig>     schemas;
    private final SystemConfig                  system;
    /** unmodifiable */
    private final Map<String, UserConfig>       users;
    private final QuarantineConfig              quarantine;
    private final ClusterConfig                 cluster;

    public XMLConfigLoader(SchemaLoader schemaLoader, String configFolder) {
        //this.functions = Collections.unmodifiableMap(schemaLoader.getFunctions());
        this.dataSources = schemaLoader.getDataSources();
        this.dataNodes = schemaLoader.getDataNodes();
        this.schemas = schemaLoader.getSchemas();
        // this.rules = schemaLoader.listRuleConfig();
        schemaLoader = null;
        XMLServerLoader serverLoader = new XMLServerLoader(configFolder);
        this.system = serverLoader.getSystem();
        this.users = serverLoader.getUsers();
        this.quarantine = serverLoader.getQuarantine();
        this.cluster = serverLoader.getCluster();
    }

    @Override
    public ClusterConfig getClusterConfig() {
        return cluster;
    }

    @Override
    public QuarantineConfig getQuarantineConfig() {
        return quarantine;
    }

    @Override
    public UserConfig getUserConfig(String user) {
        return users.get(user);
    }

    @Override
    public Map<String, UserConfig> getUserConfigs() {
        return users;
    }

    @Override
    public SystemConfig getSystemConfig() {
        return system;
    }

    //    @Override
    //    public Map<String, RuleAlgorithm> getRuleFunction() {
    //        return functions;

    //    @Override
    //    public Set<RuleConfig> listRuleConfig() {
    //        return rules;
    //    }

    @Override
    public Map<String, SchemaConfig> getSchemaConfigs() {
        return schemas;
    }

    @Override
    public Map<String, DataNodeConfig> getDataNodes() {
        return dataNodes;
    }

    @Override
    public Map<String, DataSourceConfig> getDataSources() {
        return dataSources;
    }

    @Override
    public SchemaConfig getSchemaConfig(String schema) {
        return schemas.get(schema);
    }

}
