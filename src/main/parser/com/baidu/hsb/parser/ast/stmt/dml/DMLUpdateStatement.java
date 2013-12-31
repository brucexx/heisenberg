/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.fragment.Limit;
import com.baidu.hsb.parser.ast.fragment.OrderBy;
import com.baidu.hsb.parser.ast.fragment.tableref.TableReferences;
import com.baidu.hsb.parser.util.Pair;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class DMLUpdateStatement extends DMLStatement {
    private final boolean lowPriority;
    private final boolean ignore;
    private final TableReferences tableRefs;
    private final List<Pair<Identifier, Expression>> values;
    private final Expression where;
    private final OrderBy orderBy;
    private final Limit limit;

    public DMLUpdateStatement(boolean lowPriority, boolean ignore, TableReferences tableRefs,
                              List<Pair<Identifier, Expression>> values, Expression where, OrderBy orderBy, Limit limit) {
        this.lowPriority = lowPriority;
        this.ignore = ignore;
        if (tableRefs == null) throw new IllegalArgumentException("argument tableRefs is null for update stmt");
        this.tableRefs = tableRefs;
        if (values == null || values.size() <= 0) {
            this.values = Collections.emptyList();
        } else if (!(values instanceof ArrayList)) {
            this.values = new ArrayList<Pair<Identifier, Expression>>(values);
        } else {
            this.values = values;
        }
        this.where = where;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    public boolean isLowPriority() {
        return lowPriority;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public TableReferences getTableRefs() {
        return tableRefs;
    }

    public List<Pair<Identifier, Expression>> getValues() {
        return values;
    }

    public Expression getWhere() {
        return where;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public Limit getLimit() {
        return limit;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
