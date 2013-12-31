/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.model.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.baidu.hsb.util.SplitUtil;

/**
 * @author xiongzhao@baidu.com
 */
public class TableConfig {
    private final String          name;
    private final String          nameUp;
    private final String[]        dataNodes;
    private final TableRuleConfig rule;
    private final Set<String>     columnIndex;
    private final boolean         ruleRequired;

    public TableConfig(String name, String dataNode, TableRuleConfig rule, boolean ruleRequired) {
        if (name == null) {
            throw new IllegalArgumentException("table name is null");
        }
        this.name = name;
        this.nameUp = name.toUpperCase();
        this.dataNodes = SplitUtil.split(dataNode, ',', '$', '-', '[', ']');
        if (this.dataNodes == null || this.dataNodes.length <= 0) {
            throw new IllegalArgumentException("invalid table dataNodes: " + dataNode);
        }
        this.rule = rule;
        this.columnIndex = buildColumnIndex(rule);
        this.ruleRequired = ruleRequired;
    }

    public boolean existsColumn(String columnNameUp) {
        return columnIndex.contains(columnNameUp);
    }

    /**
     * @return upper-case
     */
    public String getRealName() {
        return name;
    }

    public String getNameUp() {
        return nameUp;
    }

    public String[] getDataNodes() {
        return dataNodes;
    }

    public boolean isRuleRequired() {
        return ruleRequired;
    }

    public TableRuleConfig getRule() {
        return rule;
    }

    private static Set<String> buildColumnIndex(TableRuleConfig rule) {
        if (rule == null) {
            return Collections.emptySet();
        }

        Set<String> columnIndex = new HashSet<String>();
        for (String column : rule.getColumns()) {
            columnIndex.add(column.toUpperCase());
        }

        return columnIndex;
    }

}
