/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route.hint;

import java.sql.SQLSyntaxErrorException;

import com.baidu.hsb.parser.util.Pair;
import com.baidu.hsb.route.RouteResultsetNode;

/**
 * @author xiongzhao@baidu.com
 */
public final class DataNodeHintParser extends HintParser {

    @Override
    public void process(CobarHint hint, String hintName, String sql) throws SQLSyntaxErrorException {
        if (currentChar(hint, sql) == '[') {
            for (;;) {
                nextChar(hint, sql);
                Pair<Integer, Integer> pair = parseDataNode(hint, sql);
                hint.addDataNode(pair.getKey(), pair.getValue());
                switch (currentChar(hint, sql)) {
                case ',':
                    continue;
                case ']':
                    nextChar(hint, sql);
                    return;
                default:
                    throw new SQLSyntaxErrorException("err for dataNodeId: " + sql);
                }
            }
        } else {
            Pair<Integer, Integer> pair = parseDataNode(hint, sql);
            hint.addDataNode(pair.getKey(), pair.getValue());
        }
    }

    /**
     * first char is not separator
     */
    private Pair<Integer, Integer> parseDataNode(CobarHint hint, String sql) {
        int start = hint.getCurrentIndex();
        int ci = start;
        for (; isDigit(sql.charAt(ci)); ++ci) {
        }
        Integer nodeIndex = Integer.parseInt(sql.substring(start, ci));
        Integer replica = RouteResultsetNode.DEFAULT_REPLICA_INDEX;
        hint.setCurrentIndex(ci);
        if (currentChar(hint, sql) == '.') {
            nextChar(hint, sql);
            start = hint.getCurrentIndex();
            ci = start;
            for (; isDigit(sql.charAt(ci)); ++ci) {
            }
            replica = Integer.parseInt(sql.substring(start, ci));
            hint.setCurrentIndex(ci);
        }
        return new Pair<Integer, Integer>(nodeIndex, replica);
    }
}
