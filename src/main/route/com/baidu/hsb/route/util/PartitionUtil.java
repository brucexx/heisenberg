/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route.util;

import com.baidu.hsb.util.StringUtil;

/**
 * 数据分区工具
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: PartitionUtil.java, v 0.1 2013年12月31日 下午1:13:18 HI:brucest0078 Exp $
 */
public final class PartitionUtil {

    // 分区长度:数据段分布定义，其中取模的数一定要是2^n， 因为这里使用x % 2^n == x & (2^n - 1)等式，来优化性能。
    private static final int PARTITION_LENGTH = 1024;

    // %转换为&操作的换算数值
    private static final long AND_VALUE = PARTITION_LENGTH - 1;

    // 分区线段
    private final int[] segment = new int[PARTITION_LENGTH];

    /**
     * <pre>
     * @param count 表示定义的分区数
     * @param length 表示对应每个分区的取值长度
     * 注意：其中count,length两个数组的长度必须是一致的。
     * 约束：1024 = sum((count[i]*length[i])). count和length两个向量的点积恒等于1024
     * </pre>
     */
    public PartitionUtil(int[] count, int[] length) {
        if (count == null || length == null || (count.length != length.length)) {
            throw new RuntimeException("error,check your scope & scopeLength definition.");
        }
        int segmentLength = 0;
        for (int i = 0; i < count.length; i++) {
            segmentLength += count[i];
        }
        int[] ai = new int[segmentLength + 1];

        int index = 0;
        for (int i = 0; i < count.length; i++) {
            for (int j = 0; j < count[i]; j++) {
                ai[++index] = ai[index - 1] + length[i];
            }
        }
        if (ai[ai.length - 1] != PARTITION_LENGTH) {
            throw new RuntimeException("error,check your partitionScope definition.");
        }

        // 数据映射操作
        for (int i = 1; i < ai.length; i++) {
            for (int j = ai[i - 1]; j < ai[i]; j++) {
                segment[j] = (i - 1);
            }
        }
    }

    public int partition(long hash) {
        return segment[(int) (hash & AND_VALUE)];
    }

    public int partition(String key, int start, int end) {
        return partition(StringUtil.hash(key, start, end));
    }

}
