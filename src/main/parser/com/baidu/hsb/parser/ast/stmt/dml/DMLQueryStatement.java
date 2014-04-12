/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dml;

import java.util.Map;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.misc.QueryExpression;

/**
 * @author xiongzhao@baidu.com
 */
public abstract class DMLQueryStatement extends DMLStatement implements QueryExpression {
    @Override
    public int getPrecedence() {
        return PRECEDENCE_QUERY;
    }

    @Override
    public Expression setCacheEvalRst(boolean cacheEvalRst) {
        return this;
    }

    @Override
    public Object evaluation(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }
}
