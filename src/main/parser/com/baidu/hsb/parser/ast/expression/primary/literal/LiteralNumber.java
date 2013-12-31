/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary.literal;

import java.util.Map;

import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * literal date is also possible
 * 
 * @author xiongzhao@baidu.com
 */
public class LiteralNumber extends Literal {
    private final Number number;

    public LiteralNumber(Number number) {
        super();
        if (number == null) throw new IllegalArgumentException("number is null!");
        this.number = number;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return number;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public Number getNumber() {
        return number;
    }

}
