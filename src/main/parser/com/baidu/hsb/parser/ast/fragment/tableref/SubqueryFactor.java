/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.fragment.tableref;

import com.baidu.hsb.parser.ast.expression.misc.QueryExpression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class SubqueryFactor extends AliasableTableReference {
    private final QueryExpression subquery;

    public SubqueryFactor(QueryExpression subquery, String alias) {
        super(alias);
        if (alias == null) throw new IllegalArgumentException("alias is required for subquery factor");
        if (subquery == null) throw new IllegalArgumentException("subquery is null");
        this.subquery = subquery;
    }

    public QueryExpression getSubquery() {
        return subquery;
    }

    @Override
    public Object removeLastConditionElement() {
        return null;
    }

    @Override
    public boolean isSingleTable() {
        return false;
    }

    @Override
    public int getPrecedence() {
        return TableReference.PRECEDENCE_FACTOR;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
