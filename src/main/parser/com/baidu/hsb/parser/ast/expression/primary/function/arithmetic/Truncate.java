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
 * @version $Id: Truncate.java, v 0.1 2013年12月26日 下午8:09:18 HI:brucest0078 Exp $
 */
public class Truncate extends FunctionExpression {
    public Truncate(List<Expression> arguments) {
        super("TRUNCATE", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Truncate(arguments);
    }

}
