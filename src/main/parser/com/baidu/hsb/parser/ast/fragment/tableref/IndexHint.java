/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.fragment.tableref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.baidu.hsb.parser.ast.ASTNode;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class IndexHint implements ASTNode {
    public static enum IndexAction {
        USE,
        IGNORE,
        FORCE
    }

    public static enum IndexType {
        INDEX,
        KEY
    }

    public static enum IndexScope {
        /** not specified */
        ALL,
        JOIN,
        GROUP_BY,
        ORDER_BY
    }

    private final IndexAction action;
    private final IndexType type;
    private final IndexScope scope;
    private final List<String> indexList;

    public IndexHint(IndexAction action, IndexType type, IndexScope scope, List<String> indexList) {
        super();
        if (action == null) throw new IllegalArgumentException("index hint action is null");
        if (type == null) throw new IllegalArgumentException("index hint type is null");
        if (scope == null) throw new IllegalArgumentException("index hint scope is null");
        this.action = action;
        this.type = type;
        this.scope = scope;
        if (indexList == null || indexList.isEmpty()) {
            this.indexList = Collections.emptyList();
        } else if (indexList instanceof ArrayList) {
            this.indexList = indexList;
        } else {
            this.indexList = new ArrayList<String>(indexList);
        }
    }

    public IndexAction getAction() {
        return action;
    }

    public IndexType getType() {
        return type;
    }

    public IndexScope getScope() {
        return scope;
    }

    public List<String> getIndexList() {
        return indexList;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
