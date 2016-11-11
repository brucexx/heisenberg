/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.config.util;

import org.apache.commons.lang.StringUtils;

import com.baidu.hsb.route.RouteResultset;

/**
 * 字节相关辅助类
 * 
 * @author xiongzhao@baidu.com
 */
public class ByteUtil {

    private static final byte NULL = (byte) 0xfb;

    /**
     * 将字节数组转换为十六进制字符串
     */
    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException();
        }

        StringBuffer result = new StringBuffer();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex);
        }
        return result.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组
     * 
     * @param hex 如果长度为奇数，在前面补0
     */
    public static byte[] fromHexString(String hex) {
        if (StringUtils.isBlank(hex)) {
            throw new IllegalArgumentException();
        }
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }

        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = StringUtils.upperCase(hex).toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    /**
     * 将代表十六进制数字的字符变为字节
     */
    private static byte toByte(char c) {
        int digit = Character.digit(c, 16);
        if (digit == -1) {
            throw new IllegalArgumentException(c + " is not a valid Hex digit!");
        }
        return (byte) digit;
    }

    /**
     * MySQL数据包字节数组计算
     */
    public static byte[] calc(byte[] s1, byte[] s2, int flag) {
        if (s1 == null || s1.length == 0) {
            return s2;
        }
        if (s2 == null || s2.length == 0) {
            return s1;
        }

        String[] data1 = convertToStrings(s1);
        String[] data2 = convertToStrings(s2);
        if (data1.length != data2.length) {
            throw new RuntimeException("data error! s1=" + toHexString(s1) + ", s2=" + toHexString(s2));
        }

        String[] result = new String[data1.length];
        for (int i = 0; i < data1.length; i++) {
            if (StringUtils.isEmpty(data1[i])) {
                result[i] = data2[i];
            } else if (StringUtils.isEmpty(data2[i])) {
                result[i] = data1[i];
            } else {
                switch (flag) {
                    case RouteResultset.SUM_FLAG:
                        long dataSum = Long.valueOf(data1[i]) + Long.valueOf(data2[i]);
                        result[i] = String.valueOf(dataSum);
                        break;
                    case RouteResultset.MAX_FLAG:
                        if (compare(data1[i], data2[i]) >= 0) {
                            result[i] = data1[i];
                        } else {
                            result[i] = data2[i];
                        }
                        break;
                    case RouteResultset.MIN_FLAG:
                        if (compare(data1[i], data2[i]) <= 0) {
                            result[i] = data1[i];
                        } else {
                            result[i] = data2[i];
                        }
                        break;
                    default:
                        throw new RuntimeException("flag error! flag=" + flag);
                }
            }
        }
        return convertToBytes(result);
    }

    /**
     * 比较s1是否大于s2，大于返回正数，等于返回0，小于返回负数；比较规则：长度（长>短），ASCII码（大>小）
     */
    private static int compare(String s1, String s2) {
        int len1 = StringUtils.length(s1);
        int len2 = StringUtils.length(s2);
        return len1 == len2 ? s1.compareTo(s2) : len1 - len2;
    }

    private static String[] convertToStrings(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new String[0];
        }

        int size = 0;
        for (int i = 0; i < bytes.length; i++) {
            size++;
            int len = bytes[i] < 0 ? 0 : bytes[i];
            i += len;
        }
        String[] result = new String[size];

        for (int i = 0, pos = 0; i < bytes.length; i++) {
            StringBuilder element = new StringBuilder();
            int len = bytes[i] < 0 ? 0 : bytes[i];
            for (int j = i + 1; j <= i + len; j++) {
                element.append((char) bytes[j]);
            }
            result[pos] = element.toString();
            pos++;
            i += len;
        }
        return result;
    }

    private static byte[] convertToBytes(String[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        int size = 0;
        for (int i = 0; i < data.length; i++) {
            size += (data[i].length() + 1);
        }
        byte[] result = new byte[size];

        for (int i = 0, pos = 0; i < data.length; i++) {
            if (data[i].length() == 0) {
                result[pos] = NULL;
                pos++;
            } else {
                result[pos] = (byte) data[i].length();
                pos++;
                byte[] bytes = data[i].getBytes();
                for (int j = 0; j < bytes.length; j++) {
                    result[pos] = bytes[j];
                    pos++;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(NULL);
    }

}
