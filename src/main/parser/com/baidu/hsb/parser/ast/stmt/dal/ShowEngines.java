/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dal;

import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class ShowEngines extends DALShowStatement {
    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
