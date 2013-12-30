/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.model;

import java.util.HashMap;
import java.util.Map;

import com.baidu.hsb.route.util.StringUtil;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: RealTableCache.java, v 0.1 2013年12月25日 上午10:29:24 HI:brucest0078 Exp $
 */
public class RealTableCache {

    //k up value realTab
    public static final Map<String, String> map = new HashMap<String, String>();

    /**
     * 
     * 
     * @param rTableName
     */
    public static void put(String rTableName) {
        if (StringUtil.isNotEmpty(rTableName)) {
            map.put(rTableName.toUpperCase(), rTableName);
        }

    }

    /**
     * 
     * @param tbUp
     * @return
     */
    public static String getRealByUp(String tbUp) {
        String s = map.get(tbUp);
        if (StringUtil.isEmpty(s)) {
            return tbUp;
        } else {
            return s;
        }
    }

}
