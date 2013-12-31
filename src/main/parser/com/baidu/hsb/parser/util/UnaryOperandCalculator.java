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
public interface UnaryOperandCalculator {
    Number calculate(Integer num);

    Number calculate(Long num);

    Number calculate(BigInteger num);

    Number calculate(BigDecimal num);
}
