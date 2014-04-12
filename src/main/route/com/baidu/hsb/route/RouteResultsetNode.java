/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: RouteResultsetNode.java, v 0.1 2013年12月31日 下午1:14:25 HI:brucest0078 Exp $
 */
public final class RouteResultsetNode {
    public final static Integer DEFAULT_REPLICA_INDEX = -1;

    private final String        name;                      // 数据节点名称
    private final int           replicaIndex;              // 数据源编号
    private final String[]      statement;                 // 执行的语句

    public RouteResultsetNode(String name, String[] statement) {
        this(name, DEFAULT_REPLICA_INDEX, statement);
    }

    public RouteResultsetNode(String name, int index, String[] statement) {
        this.name = name;
        this.replicaIndex = index;
        this.statement = statement;
    }

    public String getName() {
        return name;
    }

    public int getReplicaIndex() {
        return replicaIndex;
    }

    public String[] getStatement() {
        return statement;
    }

    public int getSqlCount() {
        return statement != null ? statement.length : 0;
    }

    public String getLogger() {
        StringBuilder sb = new StringBuilder();
        sb.append(isMultiStatement() ? "multi:" : "");
        for (String stmt : statement) {
            sb.append(stmt).append(",");
        }
        return sb.toString();
    }

    public boolean isMultiStatement() {
        return statement != null && statement.length > 1;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof RouteResultsetNode) {
            RouteResultsetNode rrn = (RouteResultsetNode) obj;
            if (replicaIndex == rrn.getReplicaIndex() && equals(name, rrn.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(name).append('.');
        if (replicaIndex < 0) {
            s.append("default");
        } else {
            s.append(replicaIndex);
        }
        s.append('{').append(statement).append('}');
        return s.toString();
    }

    private static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

}
