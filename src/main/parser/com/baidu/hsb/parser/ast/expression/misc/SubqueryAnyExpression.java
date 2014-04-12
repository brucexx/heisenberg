/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.misc;

import com.baidu.hsb.parser.ast.expression.UnaryOperatorExpression;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: SubqueryAnyExpression.java, v 0.1 2013年12月26日 下午6:19:18 HI:brucest0078 Exp $
 */
public class SubqueryAnyExpression extends UnaryOperatorExpression {
    public SubqueryAnyExpression(QueryExpression subquery) {
        super(subquery, PRECEDENCE_PRIMARY);
    }

    @Override
    public String getOperator() {
        return "ANY";
    }

}
