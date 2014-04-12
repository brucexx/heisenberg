/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary.literal;

import java.util.Map;

import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * literal date is also possible
 * 
 * @author xiongzhao@baidu.com
 */
public class LiteralString extends Literal {
    private final String introducer;
    private final String string;
    private final boolean nchars;

    /**
     * @param string content of string, excluded of head and tail "'". e.g. for
     *            string token of "'don\\'t'", argument of string is "don\\'t"
     */
    public LiteralString(String introducer, String string, boolean nchars) {
        super();
        this.introducer = introducer;
        if (string == null) throw new IllegalArgumentException("argument string is null!");
        this.string = string;
        this.nchars = nchars;
    }

    public String getIntroducer() {
        return introducer;
    }

    public String getString() {
        return string;
    }

    public boolean isNchars() {
        return nchars;
    }

    public String getUnescapedString() {
        return getUnescapedString(string, false);
    }

    public String getUnescapedString(boolean toUppercase) {
        return getUnescapedString(string, toUppercase);
    }

    public static String getUnescapedString(String string) {
        return getUnescapedString(string, false);
    }

    public static String getUnescapedString(String string, boolean toUppercase) {
        StringBuilder sb = new StringBuilder();
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (c == '\\') {
                switch (c = chars[++i]) {
                case '0':
                    sb.append('\0');
                    break;
                case 'b':
                    sb.append('\b');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'Z':
                    sb.append((char) 26);
                    break;
                default:
                    sb.append(c);
                }
            } else if (c == '\'') {
                ++i;
                sb.append('\'');
            } else {
                if (toUppercase && c >= 'a' && c <= 'z') c -= 32;
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        if (string == null) return null;
        return getUnescapedString();
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
