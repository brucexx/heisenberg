/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression;

import java.util.Map;

/**
 * @author xiongzhao@baidu.com
 */
public abstract class AbstractExpression implements Expression {
    private boolean cacheEvalRst = true;
    private boolean evaluated;
    private Object evaluationCache;

    @Override
    public Expression setCacheEvalRst(boolean cacheEvalRst) {
        this.cacheEvalRst = cacheEvalRst;
        return this;
    }

    @Override
    public final Object evaluation(Map<? extends Object, ? extends Object> parameters) {
        if (cacheEvalRst) {
            if (evaluated) {
                return evaluationCache;
            }
            evaluationCache = evaluationInternal(parameters);
            evaluated = true;
            return evaluationCache;
        }
        return evaluationInternal(parameters);
    }

    protected abstract Object evaluationInternal(Map<? extends Object, ? extends Object> parameters);

}
