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
 * @version $Id: Log.java, v 0.1 2013年12月26日 下午8:07:09 HI:brucest0078 Exp $
 */
public class Log extends FunctionExpression {
    public Log(List<Expression> arguments) {
        super("LOG", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Log(arguments);
    }

}
