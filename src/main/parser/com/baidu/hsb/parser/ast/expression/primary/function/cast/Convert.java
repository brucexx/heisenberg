/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary.function.cast;

import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.function.FunctionExpression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: Convert.java, v 0.1 2013年12月26日 下午8:10:09 HI:brucest0078 Exp $
 */
public class Convert extends FunctionExpression {
    /**
     * Either {@link transcodeName} or {@link typeName} is null
     */
    private final String transcodeName;

    public Convert(Expression arg, String transcodeName) {
        super("CONVERT", wrapList(arg));
        if (null == transcodeName) {
            throw new IllegalArgumentException("transcodeName is null");
        }
        this.transcodeName = transcodeName;
    }

    public String getTranscodeName() {
        return transcodeName;
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        throw new UnsupportedOperationException("function of char has special arguments");
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
