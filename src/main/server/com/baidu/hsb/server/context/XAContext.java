/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.server.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.baidu.hsb.server.session.XASession;

/**
 * @author brucexx
 *
 */
public class XAContext {

    private final static Map<String, XASession> sessionMap = new ConcurrentHashMap<String, XASession>();

    private XAContext() {

    }

    public static void removeSession(String xid) {
        sessionMap.remove(xid);

    }

    public static void addSession(String xid, XASession ss) {
        sessionMap.put(xid, ss);
    }

    public static XASession getSession(String xid) {
        return sessionMap.get(xid);
    }

}
