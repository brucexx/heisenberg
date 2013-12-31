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
public class ShowColumns extends DALShowStatement {
    private final boolean full;
    private final Identifier table;
    private final String pattern;
    private final Expression where;

    public ShowColumns(boolean full, Identifier table, Identifier database, Expression where) {
        this.full = full;
        this.table = table;
        if (database != null) {
            this.table.setParent(database);
        }
        this.pattern = null;
        this.where = where;
    }

    public ShowColumns(boolean full, Identifier table, Identifier database, String pattern) {
        this.full = full;
        this.table = table;
        if (database != null) {
            this.table.setParent(database);
        }
        this.pattern = pattern;
        this.where = null;
    }

    public ShowColumns(boolean full, Identifier table, Identifier database) {
        this.full = full;
        this.table = table;
        if (database != null) {
            this.table.setParent(database);
        }
        this.pattern = null;
        this.where = null;
    }

    public boolean isFull() {
        return full;
    }

    public Identifier getTable() {
        return table;
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
