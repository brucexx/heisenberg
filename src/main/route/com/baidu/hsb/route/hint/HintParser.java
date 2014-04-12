/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route.hint;

import java.sql.SQLSyntaxErrorException;

/**
 * Stateless
 * 
 * @author xiongzhao@baidu.com
 */
public abstract class HintParser {
    protected static boolean isDigit(char c) {
        switch (c) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return true;
        default:
            return false;
        }
    }

    /**
     * hint's {@link CobarHint#getCurrentIndex()} will be changed to index of
     * next char after process
     */
    public abstract void process(CobarHint hint, String hintName, String sql) throws SQLSyntaxErrorException;

    private void skipSpace(CobarHint hint, String sql) {
        int ci = hint.getCurrentIndex();
        skip: for (;;) {
            switch (sql.charAt(ci)) {
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                hint.increaseCurrentIndex();
                ++ci;
                break;
            default:
                break skip;
            }
        }
    }

    protected char currentChar(CobarHint hint, String sql) {
        skipSpace(hint, sql);
        return sql.charAt(hint.getCurrentIndex());
    }

    /**
     * current char is not separator
     */
    protected char nextChar(CobarHint hint, String sql) {
        skipSpace(hint, sql);
        skipSpace(hint.increaseCurrentIndex(), sql);
        return sql.charAt(hint.getCurrentIndex());
    }

    protected Object parsePrimary(CobarHint hint, String sql) throws SQLSyntaxErrorException {
        char c = currentChar(hint, sql);
        int ci = hint.getCurrentIndex();
        switch (c) {
        case '\'':
            StringBuilder sb = new StringBuilder();
            for (++ci;; ++ci) {
                c = sql.charAt(ci);
                switch (c) {
                case '\'':
                    hint.setCurrentIndex(ci + 1);
                    return sb.toString();
                case '\\':
                    c = sql.charAt(++ci);
                default:
                    sb.append(c);
                    break;
                }
            }
        case 'n':
        case 'N':
            hint.setCurrentIndex(ci + "null".length());
            return null;
        default:
            if (isDigit(c)) {
                int start = ci++;
                for (; isDigit(sql.charAt(ci)); ++ci) {
                }
                hint.setCurrentIndex(ci);
                return Long.parseLong(sql.substring(start, ci));
            }
            throw new SQLSyntaxErrorException("unknown primary in hint: " + sql);
        }
    }

}
