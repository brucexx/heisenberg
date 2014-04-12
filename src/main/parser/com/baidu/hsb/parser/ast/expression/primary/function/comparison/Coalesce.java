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
 * @version $Id: Coalesce.java, v 0.1 2013年12月26日 下午8:10:28 HI:brucest0078 Exp $
 */
public class Coalesce extends FunctionExpression {
    public Coalesce(List<Expression> arguments) {
        super("COALESCE", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Coalesce(arguments);
    }

}
