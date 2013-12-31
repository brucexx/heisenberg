/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.string;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.TernaryOperatorExpression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * <code>higherPreExpr 'NOT'? 'LIKE' higherPreExpr ('ESCAPE' higherPreExpr)?</code>
 * 
 * @author xiongzhao@baidu.com
 */
public class LikeExpression extends TernaryOperatorExpression {
    private final boolean not;

    /**
     * @param escape null is no ESCAPE
     */
    public LikeExpression(boolean not, Expression comparee, Expression pattern, Expression escape) {
        super(comparee, pattern, escape);
        this.not = not;
    }

    public boolean isNot() {
        return not;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_COMPARISION;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
