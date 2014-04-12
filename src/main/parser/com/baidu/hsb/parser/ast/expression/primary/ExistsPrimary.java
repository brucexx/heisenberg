/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary;

import com.baidu.hsb.parser.ast.expression.misc.QueryExpression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * <code>'EXISTS' '(' subquery ')'</code>
 * 
 * @author xiongzhao@baidu.com
 */
public class ExistsPrimary extends PrimaryExpression {
    private final QueryExpression subquery;

    public ExistsPrimary(QueryExpression subquery) {
        if (subquery == null) throw new IllegalArgumentException("subquery is null for EXISTS expression");
        this.subquery = subquery;
    }

    /**
     * @return never null
     */
    public QueryExpression getSubquery() {
        return subquery;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
