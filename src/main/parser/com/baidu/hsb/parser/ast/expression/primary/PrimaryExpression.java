/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary;

import java.util.Map;

import com.baidu.hsb.parser.ast.expression.AbstractExpression;

/**
 * @author xiongzhao@baidu.com
 */
public abstract class PrimaryExpression extends AbstractExpression {
    @Override
    public int getPrecedence() {
        return PRECEDENCE_PRIMARY;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }
}
