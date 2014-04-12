/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dal;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class ShowTables extends DALShowStatement {
    private final boolean full;
    private Identifier schema;
    private final String pattern;
    private final Expression where;

    public ShowTables(boolean full, Identifier schema, String pattern) {
        this.full = full;
        this.schema = schema;
        this.pattern = pattern;
        this.where = null;
    }

    public ShowTables(boolean full, Identifier schema, Expression where) {
        this.full = full;
        this.schema = schema;
        this.pattern = null;
        this.where = where;
    }

    public ShowTables(boolean full, Identifier schema) {
        this.full = full;
        this.schema = schema;
        this.pattern = null;
        this.where = null;
    }

    public boolean isFull() {
        return full;
    }

    public void setSchema(Identifier schema) {
        this.schema = schema;
    }

    public Identifier getSchema() {
        return schema;
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
