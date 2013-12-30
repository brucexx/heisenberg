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
 * @version $Id: Log2.java, v 0.1 2013年12月26日 下午8:07:29 HI:brucest0078 Exp $
 */
public class Log2 extends FunctionExpression {
    public Log2(List<Expression> arguments) {
        super("LOG2", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Log2(arguments);
    }

}
