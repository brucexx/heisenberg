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
 * @version $Id: ArithmeticAddExpression.java, v 0.1 2013年12月26日 下午6:01:32 HI:brucest0078 Exp $
 */
public class ArithmeticAddExpression extends ArithmeticBinaryOperatorExpression {
    public ArithmeticAddExpression(Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_ARITHMETIC_TERM_OP);
    }

    @Override
    public String getOperator() {
        return "+";
    }

    @Override
    public Number calculate(Integer integer1, Integer integer2) {
        if (integer1 == null || integer2 == null)
            return null;
        int i1 = integer1.intValue();
        int i2 = integer2.intValue();
        if (i1 == 0)
            return integer2;
        if (i2 == 0)
            return integer1;
        if (i1 >= 0 && i2 <= 0 || i1 <= 0 && i2 >= 0) {
            return new Integer(i1 + i2);
        }
        int rst = i1 + i2;
        if (i1 > 0 && rst < i1 || i1 < 0 && rst > i1) {
            return new Long((long) i1 + (long) i2);
        }
        return new Integer(rst);
    }

    @Override
    public Number calculate(Long long1, Long long2) {
        if (long1 == null || long2 == null)
            return null;
        long l1 = long1.longValue();
        long l2 = long2.longValue();
        if (l1 == 0L)
            return long2;
        if (l2 == 0L)
            return long1;
        if (l1 >= 0L && l2 <= 0L || l1 <= 0L && l2 >= 0L) {
            return new Long(l1 + l2);
        }
        long rst = l1 + l2;
        if (l1 > 0L && rst < l1 || l1 < 0L && rst > l1) {
            BigInteger bi1 = BigInteger.valueOf(l1);
            BigInteger bi2 = BigInteger.valueOf(l2);
            return bi1.add(bi2);
        }
        return new Long(rst);
    }

    @Override
    public BigInteger calculate(BigInteger bigint1, BigInteger bigint2) {
        if (bigint1 == null || bigint2 == null)
            return null;
        return bigint1.add(bigint2);
    }

    @Override
    public BigDecimal calculate(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        if (bigDecimal1 == null || bigDecimal2 == null)
            return null;
        return bigDecimal1.add(bigDecimal2);
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
