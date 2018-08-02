/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.fragment.tableref;

import java.util.ArrayList;
import java.util.List;

import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class Dual implements TableReference {

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Object removeLastConditionElement() {
        return null;
    }

    @Override
    public boolean isSingleTable() {
        return true;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_FACTOR;
    }

    /* (non-Javadoc)
     * @see com.baidu.hsb.parser.ast.fragment.tableref.TableReference#getTables()
     */
    @Override
    public List<Identifier> getTables() {
         return new ArrayList<Identifier>();
    }

   

}
