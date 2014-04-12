/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route.hint;

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Map;

import com.baidu.hsb.config.util.ParameterMapping;

/**
 * @author xiongzhao@baidu.com
 */
public final class SimpleHintParser extends HintParser {

    @Override
    public void process(CobarHint hint, String hintName, String sql) throws SQLSyntaxErrorException {
        Object value = parsePrimary(hint, sql);
        if (value instanceof Long)
            value = ((Long) value).intValue();
        Map<String, Object> properties = new HashMap<String, Object>(1, 1);
        properties.put(hintName, value);
        try {
            ParameterMapping.mapping(hint, properties);
        } catch (Throwable t) {
            throw new SQLSyntaxErrorException(t);
        }
    }

}
