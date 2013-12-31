/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.recognizer.mysql.lexer;

import java.util.HashMap;
import java.util.Map;

import com.baidu.hsb.parser.recognizer.mysql.MySQLToken;

/**
 * @author xiongzhao@baidu.com
 */
class MySQLKeywords {
    public static final MySQLKeywords DEFAULT_KEYWORDS = new MySQLKeywords();

    private final Map<String, MySQLToken> keywords = new HashMap<String, MySQLToken>(230);

    private MySQLKeywords() {
        for (MySQLToken type : MySQLToken.class.getEnumConstants()) {
            String name = type.name();
            if (name.startsWith("KW_")) {
                String kw = name.substring("KW_".length());
                keywords.put(kw, type);
            }
        }
        keywords.put("NULL", MySQLToken.LITERAL_NULL);
        keywords.put("FALSE", MySQLToken.LITERAL_BOOL_FALSE);
        keywords.put("TRUE", MySQLToken.LITERAL_BOOL_TRUE);
    }

    /**
     * @param keyUpperCase must be uppercase
     * @return <code>KeyWord</code> or {@link MySQLToken#LITERAL_NULL NULL} or
     *         {@link MySQLToken#LITERAL_BOOL_FALSE FALSE} or
     *         {@link MySQLToken#LITERAL_BOOL_TRUE TRUE}
     */
    public MySQLToken getKeyword(String keyUpperCase) {
        return keywords.get(keyUpperCase);
    }

}
