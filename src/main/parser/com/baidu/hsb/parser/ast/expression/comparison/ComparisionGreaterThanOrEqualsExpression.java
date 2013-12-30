/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.comparison;

import com.baidu.hsb.parser.ast.expression.BinaryOperatorExpression;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: ComparisionGreaterThanOrEqualsExpression.java, v 0.1 2013年12月26日 下午6:11:46 HI:brucest0078 Exp $
 */
public class ComparisionGreaterThanOrEqualsExpression extends BinaryOperatorExpression {
    public ComparisionGreaterThanOrEqualsExpression(Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_COMPARISION);
    }

    @Override
    public String getOperator() {
        return ">=";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
