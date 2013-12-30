/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.arithmeic;

import java.util.Map;

import com.baidu.hsb.parser.ast.expression.BinaryOperatorExpression;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.util.BinaryOperandCalculator;
import com.baidu.hsb.parser.util.ExprEvalUtils;
import com.baidu.hsb.parser.util.Pair;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: ArithmeticBinaryOperatorExpression.java, v 0.1 2013年12月26日 下午6:07:50 HI:brucest0078 Exp $
 */
public abstract class ArithmeticBinaryOperatorExpression extends BinaryOperatorExpression implements
                                                                                         BinaryOperandCalculator {
    protected ArithmeticBinaryOperatorExpression(Expression leftOprand, Expression rightOprand,
                                                 int precedence) {
        super(leftOprand, rightOprand, precedence, true);
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        Object left = leftOprand.evaluation(parameters);
        Object right = rightOprand.evaluation(parameters);
        if (left == null || right == null)
            return null;
        if (left == UNEVALUATABLE || right == UNEVALUATABLE)
            return UNEVALUATABLE;
        Pair<Number, Number> pair = ExprEvalUtils.convertNum2SameLevel(left, right);
        return ExprEvalUtils.calculate(this, pair.getKey(), pair.getValue());
    }

}
