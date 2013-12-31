/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.model.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: QuarantineConfig.java, v 0.1 2013年12月31日 上午10:48:09 HI:brucest0078 Exp $
 */
public final class QuarantineConfig {

    private final Map<String, Set<String>> hosts;

    public QuarantineConfig() {
        hosts = new HashMap<String, Set<String>>();
    }

    public Map<String, Set<String>> getHosts() {
        return hosts;
    }

}
