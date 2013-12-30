/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary.function.bit;

import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.function.FunctionExpression;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: BitCount.java, v 0.1 2013年12月26日 下午8:09:51 HI:brucest0078 Exp $
 */
public class BitCount extends FunctionExpression {
    public BitCount(List<Expression> arguments) {
        super("BIT_COUNT", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new BitCount(arguments);
    }

}
