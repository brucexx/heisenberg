/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.bit;

import com.baidu.hsb.parser.ast.expression.BinaryOperatorExpression;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: BitOrExpression.java, v 0.1 2013年12月26日 下午6:09:42 HI:brucest0078 Exp $
 */
public class BitOrExpression extends BinaryOperatorExpression {
    public BitOrExpression(Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_BIT_OR);
    }

    @Override
    public String getOperator() {
        return "|";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
