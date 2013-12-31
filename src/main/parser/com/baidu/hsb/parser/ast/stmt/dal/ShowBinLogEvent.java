/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dal;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.fragment.Limit;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class ShowBinLogEvent extends DALShowStatement {
    private final String logName;
    private final Expression pos;
    private final Limit limit;

    public ShowBinLogEvent(String logName, Expression pos, Limit limit) {
        this.logName = logName;
        this.pos = pos;
        this.limit = limit;
    }

    public String getLogName() {
        return logName;
    }

    public Expression getPos() {
        return pos;
    }

    public Limit getLimit() {
        return limit;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
