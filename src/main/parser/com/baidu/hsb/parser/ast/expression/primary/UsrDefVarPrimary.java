/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary;

import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class UsrDefVarPrimary extends VariableExpression {
    /** include starting '@', e.g. "@'mary''s'" */
    private final String varText;

    public UsrDefVarPrimary(String varText) {
        this.varText = varText;
    }

    public String getVarText() {
        return varText;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
