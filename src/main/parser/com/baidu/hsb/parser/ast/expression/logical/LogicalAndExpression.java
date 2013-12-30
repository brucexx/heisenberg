/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.logical;

import java.util.Map;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.PolyadicOperatorExpression;
import com.baidu.hsb.parser.ast.expression.primary.literal.LiteralBoolean;
import com.baidu.hsb.parser.util.ExprEvalUtils;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: LogicalAndExpression.java, v 0.1 2013年12月26日 下午6:17:54 HI:brucest0078 Exp $
 */
public class LogicalAndExpression extends PolyadicOperatorExpression {
    public LogicalAndExpression() {
        super(PRECEDENCE_LOGICAL_AND);
    }

    @Override
    public String getOperator() {
        return "AND";
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        for (Expression operand : operands) {
            Object val = operand.evaluation(parameters);
            if (val == null) return null;
            if (val == UNEVALUATABLE) return UNEVALUATABLE;
            if (!ExprEvalUtils.obj2bool(val)) {
                return LiteralBoolean.FALSE;
            }
        }
        return LiteralBoolean.TRUE;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
