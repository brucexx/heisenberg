/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary;

import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * used as right oprand for assignment of INSERT and REPLACE
 * 
 * @author xiongzhao@baidu.com
 */
public class DefaultValue extends PrimaryExpression {

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
