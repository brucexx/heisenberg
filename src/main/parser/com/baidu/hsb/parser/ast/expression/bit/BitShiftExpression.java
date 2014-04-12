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
 * @version $Id: BitShiftExpression.java, v 0.1 2013年12月26日 下午6:09:51 HI:brucest0078 Exp $
 */
public class BitShiftExpression extends BinaryOperatorExpression {
    private final boolean negative;

    /**
     * @param negative true if right shift
     */
    public BitShiftExpression(boolean negative, Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_BIT_SHIFT);
        this.negative = negative;
    }

    public boolean isRightShift() {
        return negative;
    }

    @Override
    public String getOperator() {
        return negative ? ">>" : "<<";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
