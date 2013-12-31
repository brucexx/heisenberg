/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.model.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author xiongzhao@baidu.com
 */
public class SchemaConfig {

    private final String name;
    private final String dataNode;
    private final String group;
    private final Map<String, TableConfig> tables;
    private final boolean noSharding;
    private final String[] metaDataNodes;
    private final boolean keepSqlSchema;
    private final Set<String> allDataNodes;

    public SchemaConfig(String name, String dataNode, String group, boolean keepSqlSchema,
            Map<String, TableConfig> tables) {
        this.name = name;
        this.dataNode = dataNode;
        this.group = group;
        this.tables = tables;
        this.noSharding = (tables == null || tables.isEmpty()) ? true : false;
        this.metaDataNodes = buildMetaDataNodes();
        this.allDataNodes = buildAllDataNodes();
        this.keepSqlSchema = keepSqlSchema;
    }

    public boolean isKeepSqlSchema() {
        return keepSqlSchema;
    }

    public String getName() {
        return name;
    }

    public String getDataNode() {
        return dataNode;
    }

    public String getGroup() {
        return group;
    }

    public Map<String, TableConfig> getTables() {
        return tables;
    }

    public boolean isNoSharding() {
        return noSharding;
    }

    public String[] getMetaDataNodes() {
        return metaDataNodes;
    }

    public Set<String> getAllDataNodes() {
        return allDataNodes;
    }

    public String getRandomDataNode() {
        if (allDataNodes == null || allDataNodes.isEmpty()) {
            return null;
        }
        return allDataNodes.iterator().next();
    }

    /**
     * 取得含有不同Meta信息的数据节点,比如表和表结构。
     */
    private String[] buildMetaDataNodes() {
        Set<String> set = new HashSet<String>();
        if (!isEmpty(dataNode)) {
            set.add(dataNode);
        }
        if (!noSharding) {
            for (TableConfig tc : tables.values()) {
                set.add(tc.getDataNodes()[0]);
            }
        }
        return set.toArray(new String[set.size()]);
    }

    /**
     * 取得该schema的所有数据节点
     */
    private Set<String> buildAllDataNodes() {
        Set<String> set = new HashSet<String>();
        if (!isEmpty(dataNode)) {
            set.add(dataNode);
        }
        if (!noSharding) {
            for (TableConfig tc : tables.values()) {
                set.addAll(Arrays.asList(tc.getDataNodes()));
            }
        }
        return set;
    }

    private static boolean isEmpty(String str) {
        return ((str == null) || (str.length() == 0));
    }

}
