/**
 * Baifubao.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: ByteUtil.java, v 0.1 2013年12月25日 下午10:34:36 HI:brucest0078 Exp $
 */
public final class ByteUtil {

    private static final Logger LOGGER = Logger.getLogger(ByteUtil.class);

    /**
     * 把一个字节数组的串格式化成十六进制形式, 格式化后的样式如下:
     * 
     * <pre> 
     *  00000H  61 62 63 64 D6 D0 B9 FA 73 73 73 73 73 73 73 73 ; abcd中国ssssssss 
     *  00016H  73 73 73 73 73 73 73 73 73 B1 B1 BE A9 64 64 64 ; sssssssss北京ddd 
     *  00032H  64 64 64 64 64 64 64 64 64 64 64 64 64 64 64 64 ; dddddddddddddddd  
     * </pre> 
     * 
     * @param by 需要格式化的字节数组
     * @return 格式化后的字符串
     * @throws UnsupportedEncodingException 
     */
    public static String formatByte(byte[] by, String charset) throws UnsupportedEncodingException {
        if (by == null || by.length == 0) {
            return StringUtil.EMPTY;
        }
        StringBuilder result = new StringBuilder(20);

        // 只保存十六进制串后面的字符串 (" : " 就占了三个字节，后面为16个字节)
        byte[] chdata = new byte[ByteConstant.HEX + 3];
        for (int i = 0; i < by.length; i++) {
            String hexStr = Integer.toHexString(by[i]).toUpperCase();

            if (i % ByteConstant.HEX == 0) {
                result.append(new String(chdata)).append("\n ");
                Arrays.fill(chdata, (byte) 0x00);
                System.arraycopy(" ; ".getBytes(), 0, chdata, 0, 3);
                for (int j = 0; j < 5 - String.valueOf(i).length(); j++) {
                    result.append("0");
                }

                result.append(i).append("H ");
            }

            if (hexStr.length() >= 2) {
                result.append(" ").append(hexStr.substring(hexStr.length() - 2));
            } else {
                result.append(" 0").append(hexStr.substring(hexStr.length() - 1));
            }

            System.arraycopy(by, i, chdata, 3 + (i % ByteConstant.HEX), 1);
        }

        for (int j = 0; j < (ByteConstant.HEX - (by.length % ByteConstant.HEX)) % ByteConstant.HEX; j++) {
            result.append("   ");
        }

        result.append(new String(chdata, charset));

        return result.toString();
    }

    /**
     * 
     * 
     * @param by
     * @return
     */
    public static String formatByte(byte[] by) {
        try {
            return formatByte(by, "GBK");
        } catch (Exception e) {
            LOGGER.error("format error:", e);
            return StringUtil.EMPTY;
        }
    }

    /**
     * HEX转换成BYTE数组
     * 
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (StringUtil.isEmpty(hexString)) {
            return null;
        }
        String temp = hexString.toUpperCase(Locale.CHINA);
        int length = temp.length() / 2;
        char[] hexChars = temp.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * char类型转换
     * 
     * @param c
     * @return
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * Encode the value as BCD and put it in the buffer. The buffer must be big
     * enough to store the digits in the original value (half the length of the
     * string).
     */
    public static void string2BCD(String pValue, byte[] buf) {
        int charpos = 0; // char where we start
        int bufpos = 0;

        //奇数判断尽量不要使用 x % 2 == 1 的方式，因为  (-5) % 2 == -1，负数也是有奇数和偶数之分的
        if (pValue.length() % 2 != 0) {

            // for odd lengths we encode just the first digit in the first byte
            buf[0] = (byte) (pValue.charAt(0) - 48);
            charpos = 1;
            bufpos = 1;
        }

        // encode the rest of the string
        while (charpos < pValue.length()) {
            buf[bufpos] = (byte) (((pValue.charAt(charpos) - 48) << 4) | (pValue
                .charAt(charpos + 1) - 48));
            charpos += 2;
            bufpos++;
        }
    }

    /**
     * <pre>
     * 进行数字ASCII码到BCD码的转换
     * </pre>
     *
     * @param val 压缩后的ASCII码
     * @param len 数据长度
     * @return ASCII码 => BCD码 
     */
    public static byte[] ascii2BCD(byte[] val, int len) {
        byte[] valByte = new byte[(len + 1) / 2];
        if (len % 2 == 0) {
            for (int i = 0; i < len; i++) {
                byte b = val[i];
                if (b > '9') {
                    b = (byte) (b % 0x10 + 9);
                } else {
                    b = (byte) (b % 0x10);
                }
                if (i % 2 == 0) {
                    valByte[i / 2] = (byte) (b * 0x10);
                } else {
                    valByte[i / 2] += b;
                }
            }
        } else {
            valByte[0] = (byte) (val[0] % 0x10);
            for (int i = 1; i < len; i++) {
                byte b = val[i];
                if (b > '9') {
                    b = (byte) (b % 0x10 + 9);
                } else {
                    b = (byte) (b % 0x10);
                }

                if (i % 2 != 0) {
                    valByte[(1 + i) / 2] = (byte) (b * 0x10);
                } else {
                    valByte[(1 + i) / 2] += b;
                }
            }
        }
        return valByte;
    }

    /**
     * 将对象转换为字节数组
     * 
     * @param obj
     * @return
     * @throws IOException
     */
    public static byte[] objectToStream(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(obj);
        } finally {
            IOUtils.closeQuietly(oos);
            IOUtils.closeQuietly(bos);
        }

        return bos.toByteArray();
    }

    /**
     * byte数组反序列化成对象
     * 
     * @param bytes
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object bytesToObject(final byte[] bytes) throws IOException,
                                                          ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }

    /**
     * 流转化为字符数组
     * 
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] streamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[1024];
            int offset = -1;
            while ((offset = inputStream.read(buf)) != -1) {
                baos.write(buf, 0, offset);
                baos.flush();
            }
            return baos.toByteArray();
        } finally {
            baos.close();
        }
    }

    /**
     *  银行大端系统，高位，C写的，和java int构造不一致
     * @param i
     * @return
     */
    public static byte[] int2BigEndianStr(int i) {
        byte[] result = new byte[4];
        result[3] = (byte) ((i >> 24) & 0xFF);
        result[2] = (byte) ((i >> 16) & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * 将最后n个重复字节去掉
     * 比如  0xff 0xee 0x00 0x00 ,  0x00  =  0xff 0xee
     * 
     * @param data
     * @param ch
     * @return
     */
    public static byte[] removeLastRepeat(byte[] data, byte ch) {
        for (int i = data.length - 1; i >= 0; i--) {
            if (data[i] != ch) {
                byte[] copyData = new byte[i + 1];
                System.arraycopy(data, 0, copyData, 0, i + 1);
                return copyData;
            }
        }
        return data;
    }

}
