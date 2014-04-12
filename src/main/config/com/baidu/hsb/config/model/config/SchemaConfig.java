/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.model.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.math.RandomUtils;

/**
 * @author xiongzhao@baidu.com
 */
public class SchemaConfig {

    private final String                   name;
    private final String                   dataNode;
    private final String                   group;
    private final Map<String, TableConfig> tables;
    private final boolean                  noSharding;
    private final String[]                 metaDataNodes;
    private final boolean                  keepSqlSchema;
    private final Set<String>              allDataNodes;
    private final List<String>             allDataNodesArr;

    public SchemaConfig(String name, String dataNode, String group, boolean keepSqlSchema,
                        Map<String, TableConfig> tables) {
        this.name = name;
        this.dataNode = dataNode;
        this.group = group;
        this.tables = tables;
        this.noSharding = (tables == null || tables.isEmpty()) ? true : false;
        this.metaDataNodes = buildMetaDataNodes();
        this.allDataNodes = buildAllDataNodes();
        allDataNodesArr = new ArrayList<String>(allDataNodes);
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
        int k = RandomUtils.nextInt(allDataNodesArr.size());
        if (k < 0 || k >= allDataNodesArr.size() || allDataNodesArr.size() == 0) {
            return null;
        }
        return allDataNodesArr.get(k);
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
        Set<String> set = new TreeSet<String>();
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
