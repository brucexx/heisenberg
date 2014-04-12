/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.comparison;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.ReplacableExpression;
import com.baidu.hsb.parser.ast.expression.TernaryOperatorExpression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: BetweenAndExpression.java, v 0.1 2013年12月26日 下午6:10:09 HI:brucest0078 Exp $
 */
public class BetweenAndExpression extends TernaryOperatorExpression implements ReplacableExpression {
    private final boolean not;

    public BetweenAndExpression(boolean not, Expression comparee, Expression notLessThan,
                                Expression notGreaterThan) {
        super(comparee, notLessThan, notGreaterThan);
        this.not = not;
    }

    public boolean isNot() {
        return not;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_BETWEEN_AND;
    }

    private Expression replaceExpr;

    @Override
    public void setReplaceExpr(Expression replaceExpr) {
        this.replaceExpr = replaceExpr;
    }

    @Override
    public void clearReplaceExpr() {
        this.replaceExpr = null;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        if (replaceExpr == null)
            visitor.visit(this);
        else
            replaceExpr.accept(visitor);
    }
}
