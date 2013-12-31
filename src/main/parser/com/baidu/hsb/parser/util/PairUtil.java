/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.util;

/**
 * @author xiongzhao@baidu.com
 */
public final class PairUtil {
    private static final int DEFAULT_INDEX = -1;

    /**
     * "2" -&gt; (0,2)<br/>
     * "1:2" -&gt; (1,2)<br/>
     * "1:" -&gt; (1,0)<br/>
     * "-1:" -&gt; (-1,0)<br/>
     * ":-1" -&gt; (0,-1)<br/>
     * ":" -&gt; (0,0)<br/>
     */
    public static Pair<Integer, Integer> sequenceSlicing(String slice) {
        int ind = slice.indexOf(':');
        if (ind < 0) {
            int i = Integer.parseInt(slice.trim());
            if (i >= 0) {
                return new Pair<Integer, Integer>(0, i);
            } else {
                return new Pair<Integer, Integer>(i, 0);
            }
        }
        String left = slice.substring(0, ind).trim();
        String right = slice.substring(1 + ind).trim();
        int start, end;
        if (left.length() <= 0) {
            start = 0;
        } else {
            start = Integer.parseInt(left);
        }
        if (right.length() <= 0) {
            end = 0;
        } else {
            end = Integer.parseInt(right);
        }
        return new Pair<Integer, Integer>(start, end);
    }

    /**
     * <pre>
     * 将名字和索引用进行分割 当src = "offer_group[4]", l='[', r=']'时，
     * 返回的Piar<String,Integer>("offer", 4);
     * 当src = "offer_group", l='[', r=']'时， 
     * 返回Pair<String, Integer>("offer",-1);
     * </pre>
     */
    public static Pair<String, Integer> splitIndex(String src, char l, char r) {
        if (src == null) {
            return null;
        }
        int length = src.length();
        if (length == 0) {
            return new Pair<String, Integer>("", DEFAULT_INDEX);
        }
        if (src.charAt(length - 1) != r) {
            return new Pair<String, Integer>(src, DEFAULT_INDEX);
        }
        int offset = src.lastIndexOf(l);
        if (offset == -1) {
            return new Pair<String, Integer>(src, DEFAULT_INDEX);
        }
        int index = DEFAULT_INDEX;
        try {
            index = Integer.parseInt(src.substring(offset + 1, length - 1));
        } catch (NumberFormatException e) {
            return new Pair<String, Integer>(src, DEFAULT_INDEX);
        }
        return new Pair<String, Integer>(src.substring(0, offset), index);
    }

}
