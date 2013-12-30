/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.comparison;

import com.baidu.hsb.parser.ast.expression.BinaryOperatorExpression;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.ReplacableExpression;
import com.baidu.hsb.parser.ast.expression.misc.InExpressionList;
import com.baidu.hsb.parser.ast.expression.misc.QueryExpression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: InExpression.java, v 0.1 2013年12月26日 下午6:17:42 HI:brucest0078 Exp $
 */
public class InExpression extends BinaryOperatorExpression implements ReplacableExpression {
    private final boolean not;

    /**
     * @param rightOprand {@link QueryExpression} or {@link InExpressionList}
     */
    public InExpression(boolean not, Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_COMPARISION);
        this.not = not;
    }

    public boolean isNot() {
        return not;
    }

    public InExpressionList getInExpressionList() {
        if (rightOprand instanceof InExpressionList) {
            return (InExpressionList) rightOprand;
        }
        return null;
    }

    public QueryExpression getQueryExpression() {
        if (rightOprand instanceof QueryExpression) {
            return (QueryExpression) rightOprand;
        }
        return null;
    }

    @Override
    public String getOperator() {
        return not ? "NOT IN" : "IN";
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
        if (replaceExpr == null) visitor.visit(this);
        else replaceExpr.accept(visitor);
    }
}
