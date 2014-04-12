/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary.function.arithmetic;

import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.function.FunctionExpression;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: Degrees.java, v 0.1 2013年12月26日 下午8:06:34 HI:brucest0078 Exp $
 */
public class Degrees extends FunctionExpression {
    public Degrees(List<Expression> arguments) {
        super("DEGREES", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Degrees(arguments);
    }

}
