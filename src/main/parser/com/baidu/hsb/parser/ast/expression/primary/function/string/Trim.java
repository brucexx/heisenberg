/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary.function.string;

import java.util.ArrayList;
import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.function.FunctionExpression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class Trim extends FunctionExpression {
    public static enum Direction {
        /** no tag for direction */
        DEFAULT,
        BOTH,
        LEADING,
        TRAILING
    }

    private final Direction direction;

    private static List<Expression> wrapList(Expression str, Expression remstr) {
        if (str == null) throw new IllegalArgumentException("str is null");
        List<Expression> list = remstr != null ? new ArrayList<Expression>(2) : new ArrayList<Expression>(1);
        list.add(str);
        if (remstr != null) list.add(remstr);
        return list;
    }

    public Trim(Direction direction, Expression remstr, Expression str) {
        super("TRIM", wrapList(str, remstr));
        this.direction = direction;
    }

    /**
     * @return never null
     */
    public Expression getString() {
        return getArguments().get(0);
    }

    public Expression getRemainString() {
        List<Expression> args = getArguments();
        if (args.size() < 2) return null;
        return getArguments().get(1);
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        throw new UnsupportedOperationException("function of trim has special arguments");
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
