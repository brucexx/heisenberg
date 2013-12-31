/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.statistic;

/**
 * @author xiongzhao@baidu.com
 */
public final class SQLRecord implements Comparable<SQLRecord> {

    public String host;
    public String schema;
    public String statement;
    public long startTime;
    public long executeTime;
    public String dataNode;
    public int dataNodeIndex;

    @Override
    public int compareTo(SQLRecord o) {
        return (int) (executeTime - o.executeTime);
    }

}
