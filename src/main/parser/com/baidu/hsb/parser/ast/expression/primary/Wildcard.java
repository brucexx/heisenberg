/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary;

import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * stand for <code>*</code>
 * 
 * @author xiongzhao@baidu.com
 */
public class Wildcard extends Identifier {
    public Wildcard(Identifier parent) {
        super(parent, "*", "*");
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
