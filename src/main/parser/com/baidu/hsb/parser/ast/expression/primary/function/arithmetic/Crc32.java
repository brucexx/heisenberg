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
 * @version $Id: Crc32.java, v 0.1 2013年12月26日 下午8:06:23 HI:brucest0078 Exp $
 */
public class Crc32 extends FunctionExpression {
    public Crc32(List<Expression> arguments) {
        super("CRC32", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Crc32(arguments);
    }

}
