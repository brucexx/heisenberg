/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author xiongzhao@baidu.com
 */
public interface BinaryOperandCalculator {
    Number calculate(Integer integer1, Integer integer2);

    Number calculate(Long long1, Long long2);

    Number calculate(BigInteger bigint1, BigInteger bigint2);

    Number calculate(BigDecimal bigDecimal1, BigDecimal bigDecimal2);
}
