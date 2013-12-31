/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.heartbeat;

import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.baidu.hsb.config.Capabilities;
import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.mysql.SecurityUtil;
import com.baidu.hsb.net.BackendConnection;
import com.baidu.hsb.net.mysql.AuthPacket;
import com.baidu.hsb.net.mysql.HandshakePacket;
import com.baidu.hsb.net.mysql.HeartbeatPacket;
import com.baidu.hsb.net.mysql.MySQLPacket;
import com.baidu.hsb.util.TimeUtil;

/**
 * @author xiongzhao@baidu.com
 */
public class CobarDetector extends BackendConnection {
    private static final Logger LOGGER = Logger.getLogger(CobarDetector.class);
    private static final long CLIENT_FLAGS = initClientFlags();
    private static final Logger HEARTBEAT = Logger.getLogger("heartbeat");

    private CobarHeartbeat heartbeat;
    private final long clientFlags;
    private HandshakePacket handshake;
    private int charsetIndex;
    private boolean isAuthenticated;
    private String user;
    private String password;
    private long heartbeatTimeout;
    private final AtomicBoolean isQuit;

    public CobarDetector(SocketChannel channel) {
        super(channel);
        this.clientFlags = CLIENT_FLAGS;
        this.handler = new CobarDetectorAuthenticator(this);
        this.isQuit = new AtomicBoolean(false);
    }

    public CobarHeartbeat getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(CobarHeartbeat heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(long heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public boolean isHeartbeatTimeout() {
        return TimeUtil.currentTimeMillis() > Math.max(lastWriteTime, lastReadTime) + heartbeatTimeout;
    }

    public long lastReadTime() {
        return lastReadTime;
    }

    public long lastWriteTime() {
        return lastWriteTime;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public HandshakePacket getHandshake() {
        return handshake;
    }

    public void setHandshake(HandshakePacket handshake) {
        this.handshake = handshake;
    }

    public void setCharsetIndex(int charsetIndex) {
        this.charsetIndex = charsetIndex;
    }

    public void authenticate() {
        AuthPacket packet = new AuthPacket();
        packet.packetId = 1;
        packet.clientFlags = clientFlags;
        packet.maxPacketSize = maxPacketSize;
        packet.charsetIndex = charsetIndex;
        packet.user = user;
        try {
            packet.password = getPass(password, handshake);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
        packet.write(this);
    }

    public void heartbeat() {
        if (isAuthenticated) {
            HeartbeatPacket hp = new HeartbeatPacket();
            hp.packetId = 0;
            hp.command = MySQLPacket.COM_HEARTBEAT;
            hp.id = heartbeat.detectCount.incrementAndGet();
            hp.write(this);
            if (HEARTBEAT.isInfoEnabled()) {
                HEARTBEAT.info(heartbeat.requestMessage(CobarHeartbeat.SEND, String.valueOf(hp.id).getBytes()));
            }
        } else {
            authenticate();
        }
    }

    public void quit() {
        if (isQuit.compareAndSet(false, true)) {
            close();
        }
    }

    public boolean isQuit() {
        return isQuit.get();
    }

    @Override
    public void error(int errCode, Throwable t) {
        LOGGER.warn(toString(), t);
        switch (errCode) {
        case ErrorCode.ERR_HANDLE_DATA:
            heartbeat.setResult(CobarHeartbeat.ERROR_STATUS, this, false, null);
            break;
        default:
            heartbeat.setResult(CobarHeartbeat.ERROR_STATUS, this, true, null);
        }
    }

    @Override
    protected void idleCheck() {
        if (isIdleTimeout()) {
            LOGGER.warn(toString() + " idle timeout");
            quit();
        }
    }

    private static final long initClientFlags() {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        // flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        // flag |= Capabilities.CLIENT_COMPRESS;
        flag |= Capabilities.CLIENT_ODBC;
        // flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        // flag |= Capabilities.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        // client extension
        // flag |= Capabilities.CLIENT_MULTI_STATEMENTS;
        // flag |= Capabilities.CLIENT_MULTI_RESULTS;
        return flag;
    }

    private static final byte[] getPass(String src, HandshakePacket hsp) throws NoSuchAlgorithmException {
        if (src == null || src.length() == 0) {
            return null;
        }
        byte[] passwd = src.getBytes();
        int sl1 = hsp.seed.length;
        int sl2 = hsp.restOfScrambleBuff.length;
        byte[] seed = new byte[sl1 + sl2];
        System.arraycopy(hsp.seed, 0, seed, 0, sl1);
        System.arraycopy(hsp.restOfScrambleBuff, 0, seed, sl1, sl2);
        return SecurityUtil.scramble411(passwd, seed);
    }

}
