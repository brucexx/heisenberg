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
public class ShowCreate extends DALShowStatement {
    /** enum name must equals to real sql string */
    public static enum Type {
        DATABASE,
        EVENT,
        FUNCTION,
        PROCEDURE,
        TABLE,
        TRIGGER,
        VIEW
    }

    private final Type type;
    private final Identifier id;

    public ShowCreate(Type type, Identifier id) {
        this.type = type;
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public Identifier getId() {
        return id;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
