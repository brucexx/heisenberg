/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.statistic;

/**
 * @author xiongzhao@baidu.com 2010-9-30 上午10:43:53
 */
public final class ParserCount {

    private long parseCount;
    private long timeCount;
    private long maxParseTime;
    private long maxParseSQL;
    private long cachedCount;
    private int cacheSizeCount;

    public void doParse(long sqlId, long time) {
        parseCount++;
        timeCount += time;
        if (time > maxParseTime) {
            maxParseTime = time;
            maxParseSQL = sqlId;
        }
    }

    public long getParseCount() {
        return parseCount;
    }

    public long getTimeCount() {
        return timeCount;
    }

    public long getMaxParseTime() {
        return maxParseTime;
    }

    public long getMaxParseSQL() {
        return maxParseSQL;
    }

    public void doCached() {
        cachedCount++;
    }

    public long getCachedCount() {
        return cachedCount;
    }

    public void setCacheSizeCount(int cacheSizeCount) {
        this.cacheSizeCount = cacheSizeCount;
    }

    public int getCacheSizeCount() {
        return cacheSizeCount;
    }

}
