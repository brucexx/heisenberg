/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.bit;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.UnaryOperatorExpression;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: BitInvertExpression.java, v 0.1 2013年12月26日 下午6:09:34 HI:brucest0078 Exp $
 */
public class BitInvertExpression extends UnaryOperatorExpression {
    public BitInvertExpression(Expression operand) {
        super(operand, PRECEDENCE_UNARY_OP);
    }

    @Override
    public String getOperator() {
        return "~";
    }

}
