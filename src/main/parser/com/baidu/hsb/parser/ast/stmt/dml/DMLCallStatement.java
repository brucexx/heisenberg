/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class DMLCallStatement extends DMLStatement {
    private final Identifier procedure;
    private final List<Expression> arguments;

    public DMLCallStatement(Identifier procedure, List<Expression> arguments) {
        this.procedure = procedure;
        if (arguments == null || arguments.isEmpty()) {
            this.arguments = Collections.emptyList();
        } else if (arguments instanceof ArrayList) {
            this.arguments = arguments;
        } else {
            this.arguments = new ArrayList<Expression>(arguments);
        }
    }

    public DMLCallStatement(Identifier procedure) {
        this.procedure = procedure;
        this.arguments = Collections.emptyList();
    }

    public Identifier getProcedure() {
        return procedure;
    }

    /**
     * @return never null
     */
    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
