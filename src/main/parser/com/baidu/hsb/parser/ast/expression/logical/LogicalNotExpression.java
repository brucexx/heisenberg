/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.logical;

import java.util.Map;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.UnaryOperatorExpression;
import com.baidu.hsb.parser.ast.expression.primary.literal.LiteralBoolean;
import com.baidu.hsb.parser.util.ExprEvalUtils;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: LogicalNotExpression.java, v 0.1 2013年12月26日 下午6:18:06 HI:brucest0078 Exp $
 */
public class LogicalNotExpression extends UnaryOperatorExpression {
    public LogicalNotExpression(Expression operand) {
        super(operand, PRECEDENCE_LOGICAL_NOT);
    }

    @Override
    public String getOperator() {
        return "NOT";
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        Object operand = getOperand().evaluation(parameters);
        if (operand == null) return null;
        if (operand == UNEVALUATABLE) return UNEVALUATABLE;
        boolean bool = ExprEvalUtils.obj2bool(operand);
        return bool ? LiteralBoolean.FALSE : LiteralBoolean.TRUE;
    }
}
