/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary.function.datetime;

import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.function.FunctionExpression;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: Curtime.java, v 0.1 2013年12月30日 下午5:27:44 HI:brucest0078 Exp $
 */
public class Curtime extends FunctionExpression {
    public Curtime() {
        super("CURTIME", null);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Curtime();
    }

}
