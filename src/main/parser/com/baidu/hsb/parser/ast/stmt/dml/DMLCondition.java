/**
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dml;

import java.io.Serializable;
import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.fragment.tableref.TableReferences;

/**
 * @author brucexx
 * @since 2018年8月1日 上午10:40:11
 */
public interface DMLCondition extends Serializable {

    /**
     * 获取一个语句的所有条件集合，select union ,left join 等
     * 
     * @return
     */
    List<Expression> getWhereCondition();

    /**
     * 获取一个语句的所有tables,嵌套式
     * 
     * @return
     */
    TableReferences getTables();
}
