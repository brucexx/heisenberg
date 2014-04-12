/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary;

import com.baidu.hsb.parser.ast.fragment.VariableScope;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class SysVarPrimary extends VariableExpression {
    private final VariableScope scope;
    /** excluding starting "@@", '`' might be included */
    private final String varText;
    private final String varTextUp;

    public SysVarPrimary(VariableScope scope, String varText, String varTextUp) {
        this.scope = scope;
        this.varText = varText;
        this.varTextUp = varTextUp;
    }

    /**
     * @return never null
     */
    public VariableScope getScope() {
        return scope;
    }

    public String getVarTextUp() {
        return varTextUp;
    }

    public String getVarText() {
        return varText;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
