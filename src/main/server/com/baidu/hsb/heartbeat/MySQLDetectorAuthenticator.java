/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.heartbeat;

import com.baidu.hsb.mysql.CharsetUtil;
import com.baidu.hsb.mysql.SecurityUtil;
import com.baidu.hsb.net.NIOHandler;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.HandshakePacket;
import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.net.mysql.Reply323Packet;

/**
 * @author xiongzhao@baidu.com
 */
public class MySQLDetectorAuthenticator implements NIOHandler {

    private final MySQLDetector source;

    public MySQLDetectorAuthenticator(MySQLDetector source) {
        this.source = source;
    }

    @Override
    public void handle(byte[] data) {
        MySQLDetector source = this.source;
        HandshakePacket hsp = source.getHandshake();
        if (hsp == null) {
            // 设置握手数据包
            hsp = new HandshakePacket();
            hsp.read(data);
            source.setHandshake(hsp);

            // 设置字符集编码
            int charsetIndex = (hsp.serverCharsetIndex & 0xff);
            String charset = CharsetUtil.getCharset(charsetIndex);
            if (charset != null) {
                source.setCharsetIndex(charsetIndex);
            } else {
                throw new RuntimeException("Unknown charsetIndex:" + charsetIndex);
            }

            // 发送认证数据包
            source.authenticate();
        } else {
            switch (data[4]) {
            case OkPacket.FIELD_COUNT:
                source.setHandler(new MySQLDetectorHandler(source));
                source.setAuthenticated(true);
                source.heartbeat();// 成功后发起心跳。
                break;
            case ErrorPacket.FIELD_COUNT:
                ErrorPacket err = new ErrorPacket();
                err.read(data);
                throw new RuntimeException(new String(err.message));
            case EOFPacket.FIELD_COUNT:
                auth323(data[3], hsp.seed);
                break;
            default:
                throw new RuntimeException("Unknown packet");
            }
        }
    }

    /**
     * 发送323响应认证数据包
     */
    private void auth323(byte packetId, byte[] seed) {
        Reply323Packet r323 = new Reply323Packet();
        r323.packetId = ++packetId;
        String pass = source.getPassword();
        if (pass != null && pass.length() > 0) {
            r323.seed = SecurityUtil.scramble323(pass, new String(seed)).getBytes();
        }
        r323.write(source);
    }

}
