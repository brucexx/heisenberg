/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.type;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.UnaryOperatorExpression;

/**
 * <code>'BINARY' higherExpr</code>
 * 
 * @author xiongzhao@baidu.com
 */
public class CastBinaryExpression extends UnaryOperatorExpression {
    public CastBinaryExpression(Expression operand) {
        super(operand, PRECEDENCE_BINARY);
    }

    @Override
    public String getOperator() {
        return "BINARY";
    }

}
