/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dml;

import java.util.List;

import com.baidu.hsb.parser.ast.expression.misc.QueryExpression;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.expression.primary.RowExpression;

/**
 * @author xiongzhao@baidu.com
 */
public abstract class DMLInsertReplaceStatement extends DMLStatement {
    protected final Identifier table;
    protected final List<Identifier> columnNameList;
    protected List<RowExpression> rowList;
    protected final QueryExpression select;

    @SuppressWarnings("unchecked")
    public DMLInsertReplaceStatement(Identifier table, List<Identifier> columnNameList, List<RowExpression> rowList) {
        this.table = table;
        this.columnNameList = ensureListType(columnNameList);
        this.rowList = ensureListType(rowList);
        this.select = null;
    }

    @SuppressWarnings("unchecked")
    public DMLInsertReplaceStatement(Identifier table, List<Identifier> columnNameList, QueryExpression select) {
        if (select == null) throw new IllegalArgumentException("argument 'select' is empty");
        this.select = select;
        this.table = table;
        this.columnNameList = ensureListType(columnNameList);
        this.rowList = null;
    }

    public Identifier getTable() {
        return table;
    }

    /**
     * @return {@link java.util.ArrayList ArrayList}
     */
    public List<Identifier> getColumnNameList() {
        return columnNameList;
    }

    /**
     * @return {@link java.util.ArrayList ArrayList} or
     *         {@link java.util.Collections#emptyList() EMPTY_LIST}
     */
    public List<RowExpression> getRowList() {
        return rowList;
    }

    public QueryExpression getSelect() {
        return select;
    }

    private List<RowExpression> rowListBak;

    public void setReplaceRowList(List<RowExpression> list) {
        rowListBak = rowList;
        rowList = list;
    }

    public void clearReplaceRowList() {
        if (rowListBak != null) {
            rowList = rowListBak;
            rowListBak = null;
        }
    }

}
