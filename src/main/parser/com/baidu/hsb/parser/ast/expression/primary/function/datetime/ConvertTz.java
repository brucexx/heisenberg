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
 * @version $Id: ConvertTz.java, v 0.1 2013年12月30日 下午5:27:27 HI:brucest0078 Exp $
 */
public class ConvertTz extends FunctionExpression {
    public ConvertTz(List<Expression> arguments) {
        super("CONVERT_TZ", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new ConvertTz(arguments);
    }

}
