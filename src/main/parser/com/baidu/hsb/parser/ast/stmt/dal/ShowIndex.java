/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dal;

import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class ShowIndex extends DALShowStatement {
    public static enum Type {
        INDEX,
        INDEXES,
        KEYS
    }

    private final Type type;
    private final Identifier table;

    public ShowIndex(Type type, Identifier table, Identifier database) {
        this.table = table;
        this.table.setParent(database);
        this.type = type;
    }

    public ShowIndex(Type type, Identifier table) {
        this.table = table;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Identifier getTable() {
        return table;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
