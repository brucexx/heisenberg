/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.arithmeic;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: ArithmeticIntegerDivideExpression.java, v 0.1 2013年12月26日 下午6:08:23 HI:brucest0078 Exp $
 */
public class ArithmeticIntegerDivideExpression extends ArithmeticBinaryOperatorExpression {
    public ArithmeticIntegerDivideExpression(Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_ARITHMETIC_FACTOR_OP);
    }

    @Override
    public String getOperator() {
        return "DIV";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Number calculate(Integer integer1, Integer integer2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Number calculate(Long long1, Long long2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Number calculate(BigInteger bigint1, BigInteger bigint2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Number calculate(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        throw new UnsupportedOperationException();
    }
}
