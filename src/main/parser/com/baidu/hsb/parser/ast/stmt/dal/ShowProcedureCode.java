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
public class ShowProcedureCode extends DALShowStatement {
    private final Identifier procedureName;

    public ShowProcedureCode(Identifier procedureName) {
        this.procedureName = procedureName;
    }

    public Identifier getProcedureName() {
        return procedureName;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
