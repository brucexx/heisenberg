/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.fragment.ddl.index;

import java.util.Collections;
import java.util.List;

import com.baidu.hsb.parser.ast.ASTNode;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class IndexDefinition implements ASTNode {
    public static enum IndexType {
        BTREE,
        HASH
    }

    private final IndexType indexType;
    private final List<IndexColumnName> columns;
    private final List<IndexOption> options;

    @SuppressWarnings("unchecked")
    public IndexDefinition(IndexType indexType, List<IndexColumnName> columns, List<IndexOption> options) {
        this.indexType = indexType;
        if (columns == null || columns.isEmpty()) throw new IllegalArgumentException("columns is null or empty");
        this.columns = columns;
        this.options = (List<IndexOption>) (options == null || options.isEmpty() ? Collections.emptyList() : options);
    }

    public IndexType getIndexType() {
        return indexType;
    }

    /**
     * @return never null
     */
    public List<IndexColumnName> getColumns() {
        return columns;
    }

    /**
     * @return never null
     */
    public List<IndexOption> getOptions() {
        return options;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        // QS_TODO

    }

}
