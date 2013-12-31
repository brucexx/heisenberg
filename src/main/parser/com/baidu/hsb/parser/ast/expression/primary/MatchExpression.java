/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class MatchExpression extends PrimaryExpression {
    public static enum Modifier {
        /** no modifier */
        _DEFAULT,
        IN_BOOLEAN_MODE,
        IN_NATURAL_LANGUAGE_MODE,
        IN_NATURAL_LANGUAGE_MODE_WITH_QUERY_EXPANSION,
        WITH_QUERY_EXPANSION
    }

    private final List<Expression> columns;
    private final Expression pattern;
    private final Modifier modifier;

    /**
     * @param modifier never null
     */
    public MatchExpression(List<Expression> columns, Expression pattern, Modifier modifier) {
        if (columns == null || columns.isEmpty()) {
            this.columns = Collections.emptyList();
        } else if (columns instanceof ArrayList) {
            this.columns = columns;
        } else {
            this.columns = new ArrayList<Expression>(columns);
        }
        this.pattern = pattern;
        this.modifier = modifier;
    }

    public List<Expression> getColumns() {
        return columns;
    }

    public Expression getPattern() {
        return pattern;
    }

    public Modifier getModifier() {
        return modifier;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
