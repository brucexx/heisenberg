/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.heartbeat;

import com.baidu.hsb.mysql.CharsetUtil;
import com.baidu.hsb.net.NIOHandler;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.HandshakePacket;
import com.baidu.hsb.net.mysql.OkPacket;

/**
 * @author xiongzhao@baidu.com
 */
public class CobarDetectorAuthenticator implements NIOHandler {

    private final CobarDetector source;

    public CobarDetectorAuthenticator(CobarDetector source) {
        this.source = source;
    }

    @Override
    public void handle(byte[] data) {
        CobarDetector source = this.source;
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
        } else { // 处理认证结果
            switch (data[4]) {
            case OkPacket.FIELD_COUNT:
                source.setHandler(new CobarDetectorHandler(source));
                source.setAuthenticated(true);
                source.heartbeat();// 认证成功后，发起心跳。
                break;
            case ErrorPacket.FIELD_COUNT:
                ErrorPacket err = new ErrorPacket();
                err.read(data);
                throw new RuntimeException(new String(err.message));
            default:
                throw new RuntimeException("Unknown packet");
            }
        }
    }

}
