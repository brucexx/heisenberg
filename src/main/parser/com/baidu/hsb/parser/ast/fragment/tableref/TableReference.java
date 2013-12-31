/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.fragment.tableref;

import com.baidu.hsb.parser.ast.ASTNode;

/**
 * @author xiongzhao@baidu.com
 */
public interface TableReference extends ASTNode {
    int PRECEDENCE_REFS = 0;
    int PRECEDENCE_JOIN = 1;
    int PRECEDENCE_FACTOR = 2;

    /**
     * remove last condition element is success
     * 
     * @return {@link java.util.List List&lt;String&gt;} or
     *         {@link com.baidu.hsb.parser.ast.expression.Expression
     *         Expression}. null if last condition element cannot be removed.
     */
    Object removeLastConditionElement();

    /**
     * @return true if and only if there is one table (not subquery) in table
     *         reference
     */
    public boolean isSingleTable();

    /**
     * @return precedences are defined in {@link TableReference}
     */
    int getPrecedence();
}
