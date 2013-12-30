/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.handler;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.mysql.SecurityUtil;
import com.baidu.hsb.net.FrontendConnection;
import com.baidu.hsb.net.NIOHandler;
import com.baidu.hsb.net.mysql.AuthPacket;
import com.baidu.hsb.net.mysql.MySQLPacket;
import com.baidu.hsb.net.mysql.QuitPacket;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: FrontendAuthenticator.java, v 0.1 2013年12月26日 下午6:02:07 HI:brucest0078 Exp $
 */
public class FrontendAuthenticator implements NIOHandler {
    private static final Logger LOGGER = Logger.getLogger(FrontendAuthenticator.class);
    private static final byte[] AUTH_OK = new byte[] { 7, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0 };

    protected final FrontendConnection source;

    public FrontendAuthenticator(FrontendConnection source) {
        this.source = source;
    }

    @Override
    public void handle(byte[] data) {
        // check quit packet
        if (data.length == QuitPacket.QUIT.length && data[4] == MySQLPacket.COM_QUIT) {
            source.close();
            return;
        }

        AuthPacket auth = new AuthPacket();
        auth.read(data);

        // check user
        if (!checkUser(auth.user, source.getHost())) {
            failure(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + auth.user + "'");
            return;
        }

        // check password
        if (!checkPassword(auth.password, auth.user)) {
            failure(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + auth.user + "'");
            return;
        }

        // check schema
        switch (checkSchema(auth.database, auth.user)) {
        case ErrorCode.ER_BAD_DB_ERROR:
            failure(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + auth.database + "'");
            break;
        case ErrorCode.ER_DBACCESS_DENIED_ERROR:
            String s = "Access denied for user '" + auth.user + "' to database '" + auth.database + "'";
            failure(ErrorCode.ER_DBACCESS_DENIED_ERROR, s);
            break;
        default:
            success(auth);
        }
    }

    protected boolean checkUser(String user, String host) {
        return source.getPrivileges().userExists(user, host);
    }

    protected boolean checkPassword(byte[] password, String user) {
        String pass = source.getPrivileges().getPassword(user);

        // check null
        if (pass == null || pass.length() == 0) {
            if (password == null || password.length == 0) {
                return true;
            } else {
                return false;
            }
        }
        if (password == null || password.length == 0) {
            return false;
        }

        // encrypt
        byte[] encryptPass = null;
        try {
            encryptPass = SecurityUtil.scramble411(pass.getBytes(), source.getSeed());
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn(source.toString(), e);
            return false;
        }
        if (encryptPass != null && (encryptPass.length == password.length)) {
            int i = encryptPass.length;
            while (i-- != 0) {
                if (encryptPass[i] != password[i]) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    protected int checkSchema(String schema, String user) {
        if (schema == null) {
            return 0;
        }
        FrontendPrivileges privileges = source.getPrivileges();
        if (!privileges.schemaExists(schema)) {
            return ErrorCode.ER_BAD_DB_ERROR;
        }
        Set<String> schemas = privileges.getUserSchemas(user);
        if (schemas == null || schemas.size() == 0 || schemas.contains(schema)) {
            return 0;
        } else {
            return ErrorCode.ER_DBACCESS_DENIED_ERROR;
        }
    }

    protected void success(AuthPacket auth) {
        source.setAuthenticated(true);
        source.setUser(auth.user);
        source.setSchema(auth.database);
        source.setCharsetIndex(auth.charsetIndex);
        source.setHandler(new FrontendCommandHandler(source));
        if (LOGGER.isInfoEnabled()) {
            StringBuilder s = new StringBuilder();
            s.append(source).append('\'').append(auth.user).append("' login success");
            byte[] extra = auth.extra;
            if (extra != null && extra.length > 0) {
                s.append(",extra:").append(new String(extra));
            }
            LOGGER.info(s.toString());
        }
        ByteBuffer buffer = source.allocate();
        source.write(source.writeToBuffer(AUTH_OK, buffer));
    }

    protected void failure(int errno, String info) {
        LOGGER.error(source.toString() + info);
        source.writeErrMessage((byte) 2, errno, info);
    }

}
