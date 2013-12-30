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
 * @version $Id: Sqrt.java, v 0.1 2013年12月26日 下午8:09:01 HI:brucest0078 Exp $
 */
public class Sqrt extends FunctionExpression {
    public Sqrt(List<Expression> arguments) {
        super("SQRT", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Sqrt(arguments);
    }

}
