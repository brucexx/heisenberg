/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression;

import java.util.Map;

import com.baidu.hsb.parser.ast.ASTNode;

/**
 * @author xiongzhao@baidu.com
 */
public interface Expression extends ASTNode {
    int PRECEDENCE_QUERY = 0;
    int PRECEDENCE_ASSIGNMENT = 1;
    int PRECEDENCE_LOGICAL_OR = 2;
    int PRECEDENCE_LOGICAL_XOR = 3;
    int PRECEDENCE_LOGICAL_AND = 4;
    int PRECEDENCE_LOGICAL_NOT = 5;
    int PRECEDENCE_BETWEEN_AND = 6;
    int PRECEDENCE_COMPARISION = 7;
    int PRECEDENCE_ANY_ALL_SUBQUERY = 8;
    int PRECEDENCE_BIT_OR = 8;
    int PRECEDENCE_BIT_AND = 10;
    int PRECEDENCE_BIT_SHIFT = 11;
    int PRECEDENCE_ARITHMETIC_TERM_OP = 12;
    int PRECEDENCE_ARITHMETIC_FACTOR_OP = 13;
    int PRECEDENCE_BIT_XOR = 14;
    int PRECEDENCE_UNARY_OP = 15;
    int PRECEDENCE_BINARY = 16;
    int PRECEDENCE_COLLATE = 17;
    int PRECEDENCE_PRIMARY = 19;

    /**
     * @return precedences are defined in {@link Expression}
     */
    int getPrecedence();

    /**
     * @return this
     */
    Expression setCacheEvalRst(boolean cacheEvalRst);

    Object UNEVALUATABLE = new Object();

    Object evaluation(Map<? extends Object, ? extends Object> parameters);
}
