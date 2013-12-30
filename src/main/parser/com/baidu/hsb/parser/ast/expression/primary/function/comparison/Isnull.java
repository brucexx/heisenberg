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
 * @version $Id: Isnull.java, v 0.1 2013年12月26日 下午8:11:58 HI:brucest0078 Exp $
 */
public class Isnull extends FunctionExpression {
    public Isnull(List<Expression> arguments) {
        super("ISNULL", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Isnull(arguments);
    }

}
