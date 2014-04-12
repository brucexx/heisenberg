/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.parser;

import com.baidu.hsb.parser.util.Pair;
import com.baidu.hsb.parser.util.ParseUtil;
import com.baidu.hsb.util.SplitUtil;

/**
 * @author xiongzhao@baidu.com
 */
public final class ManagerParseStop {

    public static final int OTHER = -1;
    public static final int HEARTBEAT = 1;

    public static int parse(String stmt, int offset) {
        int i = offset;
        for (; i < stmt.length(); i++) {
            switch (stmt.charAt(i)) {
            case ' ':
                continue;
            case '/':
            case '#':
                i = ParseUtil.comment(stmt, i);
                continue;
            case '@':
                return stop2Check(stmt, i);
            default:
                return OTHER;
            }
        }
        return OTHER;
    }

    public static Pair<String[], Integer> getPair(String stmt) {
        int offset = stmt.indexOf("@@");
        String s = stmt.substring(offset + 11).trim();
        int p1 = s.lastIndexOf(':');
        if (p1 == -1) {
            String[] src = SplitUtil.split(s, ',', '$', '-', '[', ']');
            return new Pair<String[], Integer>(src, null);
        } else {
            String[] src = SplitUtil.split(s, ':', true);
            String[] src1 = SplitUtil.split(src[0], ',', '$', '-', '[', ']');
            return new Pair<String[], Integer>(src1, Integer.valueOf(src[1]));
        }
    }

    // HEARTBEAT
    static int stop2Check(String stmt, int offset) {
        if (stmt.length() > ++offset && stmt.charAt(offset) == '@') {
            if (stmt.length() > offset + 9) {
                char c1 = stmt.charAt(++offset);
                char c2 = stmt.charAt(++offset);
                char c3 = stmt.charAt(++offset);
                char c4 = stmt.charAt(++offset);
                char c5 = stmt.charAt(++offset);
                char c6 = stmt.charAt(++offset);
                char c7 = stmt.charAt(++offset);
                char c8 = stmt.charAt(++offset);
                char c9 = stmt.charAt(++offset);
                if ((c1 == 'H' || c1 == 'h') && (c2 == 'E' || c2 == 'e') && (c3 == 'A' || c3 == 'a')
                        && (c4 == 'R' || c4 == 'r') && (c5 == 'T' || c5 == 't') && (c6 == 'B' || c6 == 'b')
                        && (c7 == 'E' || c7 == 'e') && (c8 == 'A' || c8 == 'a') && (c9 == 'T' || c9 == 't')) {
                    if (stmt.length() > ++offset && stmt.charAt(offset) != ' ') {
                        return OTHER;
                    }
                    return HEARTBEAT;
                }
            }
        }
        return OTHER;
    }

}
