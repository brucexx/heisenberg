/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.extension;

import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.stmt.ddl.DDLStatement;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class ExtDDLDropPolicy implements DDLStatement {
    private final Identifier policyName;

    public ExtDDLDropPolicy(Identifier policyName) {
        this.policyName = policyName;
    }

    public Identifier getPolicyName() {
        return policyName;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
