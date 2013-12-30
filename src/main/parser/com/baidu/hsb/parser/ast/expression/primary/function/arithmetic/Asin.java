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
 * @version $Id: Asin.java, v 0.1 2013年12月26日 下午8:05:07 HI:brucest0078 Exp $
 */
public class Asin extends FunctionExpression {
    public Asin(List<Expression> arguments) {
        super("ASIN", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Asin(arguments);
    }

}
