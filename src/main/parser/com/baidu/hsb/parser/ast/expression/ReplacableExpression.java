/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression;

import com.baidu.hsb.parser.ast.expression.primary.literal.LiteralBoolean;

/**
 * @author xiongzhao@baidu.com
 */
public interface ReplacableExpression extends Expression {
    LiteralBoolean BOOL_FALSE = new LiteralBoolean(false);

    void setReplaceExpr(Expression replaceExpr);

    void clearReplaceExpr();
}
