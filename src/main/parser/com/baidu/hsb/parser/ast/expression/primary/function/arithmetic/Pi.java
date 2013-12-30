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
 * @version $Id: Pi.java, v 0.1 2013年12月26日 下午8:07:48 HI:brucest0078 Exp $
 */
public class Pi extends FunctionExpression {
    public Pi(List<Expression> arguments) {
        super("PI", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Pi(arguments);
    }

}
