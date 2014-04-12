/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.fragment.ddl.index;

import com.baidu.hsb.parser.ast.ASTNode;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class IndexColumnName implements ASTNode {
    private final Identifier columnName;
    /** null is possible */
    private final Expression length;
    private final boolean asc;

    public IndexColumnName(Identifier columnName, Expression length, boolean asc) {
        this.columnName = columnName;
        this.length = length;
        this.asc = asc;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public Identifier getColumnName() {
        return columnName;
    }

    public Expression getLength() {
        return length;
    }

    public boolean isAsc() {
        return asc;
    }

}
