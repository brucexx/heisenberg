/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.misc;

import com.baidu.hsb.parser.ast.expression.BinaryOperatorExpression;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: AssignmentExpression.java, v 0.1 2013年12月26日 下午6:18:47 HI:brucest0078 Exp $
 */
public class AssignmentExpression extends BinaryOperatorExpression {
    public AssignmentExpression(Expression left, Expression right) {
        super(left, right, Expression.PRECEDENCE_ASSIGNMENT, false);
    }

    public String getOperator() {
        return ":=";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
