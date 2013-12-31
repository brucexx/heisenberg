/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dal;

import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class ShowEngine extends DALShowStatement {
    public static enum Type {
        INNODB_STATUS,
        INNODB_MUTEX,
        PERFORMANCE_SCHEMA_STATUS
    }

    private final Type type;

    public ShowEngine(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
