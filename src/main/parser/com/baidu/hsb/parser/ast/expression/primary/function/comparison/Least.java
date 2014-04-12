/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary.function.comparison;

import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.function.FunctionExpression;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: Least.java, v 0.1 2013年12月26日 下午8:12:05 HI:brucest0078 Exp $
 */
public class Least extends FunctionExpression {
    public Least(List<Expression> arguments) {
        super("LEAST", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Least(arguments);
    }

}
