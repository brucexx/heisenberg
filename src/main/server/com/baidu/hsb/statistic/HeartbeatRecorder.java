/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.statistic;

import java.util.LinkedList;
import java.util.List;

import com.baidu.hsb.util.TimeUtil;

/**
 * 记录最近3个时段的平均响应时间，默认1，10，30分钟。
 * 
 * @author xiongzhao@baidu.com 2012-4-25
 */
public class HeartbeatRecorder {

    private static final int MAX_RECORD_SIZE = 256;
    private static final long AVG1_TIME = 60 * 1000L;
    private static final long AVG2_TIME = 10 * 60 * 1000L;
    private static final long AVG3_TIME = 30 * 60 * 1000L;

    private long avg1;
    private long avg2;
    private long avg3;
    private final List<Record> records;

    public HeartbeatRecorder() {
        this.records = new LinkedList<Record>();
    }

    public String get() {
        return new StringBuilder().append(avg1).append(',').append(avg2).append(',').append(avg3).toString();
    }

    public void set(long value) {
        if (value < 0) {
            return;
        }
        long time = TimeUtil.currentTimeMillis();
        remove(time);
        int size = records.size();
        if (size == 0) {
            records.add(new Record(value, time));
            avg1 = avg2 = avg3 = value;
            return;
        }
        if (size >= MAX_RECORD_SIZE) {
            records.remove(0);
        }
        records.add(new Record(value, time));
        calculate(time);
    }

    /**
     * 删除超过统计时间段的数据
     */
    private void remove(long time) {
        final List<Record> records = this.records;
        while (records.size() > 0) {
            Record record = records.get(0);
            if (time >= record.time + AVG3_TIME) {
                records.remove(0);
            } else {
                break;
            }
        }
    }

    /**
     * 计算记录的统计数据
     */
    private void calculate(long time) {
        long v1 = 0L, v2 = 0L, v3 = 0L;
        int c1 = 0, c2 = 0, c3 = 0;
        for (Record record : records) {
            long t = time - record.time;
            if (t <= AVG1_TIME) {
                v1 += record.value;
                ++c1;
            }
            if (t <= AVG2_TIME) {
                v2 += record.value;
                ++c2;
            }
            if (t <= AVG3_TIME) {
                v3 += record.value;
                ++c3;
            }
        }
        avg1 = (v1 / c1);
        avg2 = (v2 / c2);
        avg3 = (v3 / c3);
    }

    /**
     * @author xiongzhao@baidu.com 2012-4-25
     */
    private static class Record {
        private long value;
        private long time;

        Record(long value, long time) {
            this.value = value;
            this.time = time;
        }
    }

}
