/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.baidu.hsb.config.Alarms;
import com.baidu.hsb.config.model.config.UserConfig;
import com.baidu.hsb.net.handler.FrontendPrivileges;

/**
 * @author xiongzhao@baidu.com
 */
public class CobarPrivileges implements FrontendPrivileges {
    private static final Logger ALARM = Logger.getLogger("alarm");

    @Override
    public boolean schemaExists(String schema) {
        CobarConfig conf = CobarServer.getInstance().getConfig();
        return conf.getSchemas().containsKey(schema);
    }

    @Override
    public boolean userExists(String user, String host) {
        CobarConfig conf = CobarServer.getInstance().getConfig();
        Map<String, Set<String>> quarantineHosts = conf.getQuarantine().getHosts();
        if (quarantineHosts.containsKey(host)) {
            boolean rs = quarantineHosts.get(host).contains(user);
            if (!rs) {
                ALARM.error(new StringBuilder().append(Alarms.QUARANTINE_ATTACK).append("[host=").append(host)
                        .append(",user=").append(user).append(']').toString());
            }
            return rs;
        } else {
            if (user != null && user.equals(conf.getSystem().getClusterHeartbeatUser())) {
                return true;
            } else {
                return conf.getUsers().containsKey(user);
            }
        }
    }

    @Override
    public String getPassword(String user) {
        CobarConfig conf = CobarServer.getInstance().getConfig();
        if (user != null && user.equals(conf.getSystem().getClusterHeartbeatUser())) {
            return conf.getSystem().getClusterHeartbeatPass();
        } else {
            UserConfig uc = conf.getUsers().get(user);
            if (uc != null) {
                return uc.getPassword();
            } else {
                return null;
            }
        }
    }

    @Override
    public Set<String> getUserSchemas(String user) {
        CobarConfig conf = CobarServer.getInstance().getConfig();
        UserConfig uc = conf.getUsers().get(user);
        if (uc != null) {
            return uc.getSchemas();
        } else {
            return null;
        }
    }

}
