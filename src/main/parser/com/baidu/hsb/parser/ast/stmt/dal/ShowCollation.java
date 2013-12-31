/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dal;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class ShowCollation extends DALShowStatement {
    private final String pattern;
    private final Expression where;

    public ShowCollation(Expression where) {
        this.pattern = null;
        this.where = where;
    }

    public ShowCollation(String pattern) {
        this.pattern = pattern;
        this.where = null;
    }

    public ShowCollation() {
        this.pattern = null;
        this.where = null;
    }

    public String getPattern() {
        return pattern;
    }

    public Expression getWhere() {
        return where;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
