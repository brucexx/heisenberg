/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.util;

/**
 * @author xiongzhao@baidu.com 
 */
public final class HexFormatUtil {

    public static byte[] fromHex(String src) {
        String[] hex = src.split(" ");
        byte[] b = new byte[hex.length];
        for (int i = 0; i < hex.length; i++) {
            b[i] = (byte) (Integer.parseInt(hex[i], 16) & 0xff);
        }
        return b;
    }

    public static String fromHex(String hexString, String charset) {
        try {
            byte[] b = fromHex(hexString);
            if (charset == null) {
                return new String(b);
            }
            return new String(b, charset);
        } catch (Exception e) {
            return null;
        }
    }

    public static int fromHex2B(String src) {
        byte[] b = fromHex(src);
        int position = 0;
        int i = (b[position++] & 0xff);
        i |= (b[position++] & 0xff) << 8;
        return i;
    }

    public static int fromHex4B(String src) {
        byte[] b = fromHex(src);
        int position = 0;
        int i = (b[position++] & 0xff);
        i |= (b[position++] & 0xff) << 8;
        i |= (b[position++] & 0xff) << 16;
        i |= (b[position++] & 0xff) << 24;
        return i;
    }

    public static long fromHex8B(String src) {
        byte[] b = fromHex(src);
        int position = 0;
        long l = (b[position++] & 0xff);
        l |= (long) (b[position++] & 0xff) << 8;
        l |= (long) (b[position++] & 0xff) << 16;
        l |= (long) (b[position++] & 0xff) << 24;
        l |= (long) (b[position++] & 0xff) << 32;
        l |= (long) (b[position++] & 0xff) << 40;
        l |= (long) (b[position++] & 0xff) << 48;
        l |= (long) (b[position++] & 0xff) << 56;
        return l;
    }

}
