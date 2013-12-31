/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.mts;

import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.stmt.SQLStatement;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class MTSRollbackStatement implements SQLStatement {
    public static enum CompleteType {
        /** not specified, then use default */
        UN_DEF,
        CHAIN,
        /** MySQL's default */
        NO_CHAIN,
        RELEASE,
        NO_RELEASE
    }

    private final CompleteType completeType;
    private final Identifier savepoint;

    public MTSRollbackStatement(CompleteType completeType) {
        if (completeType == null) throw new IllegalArgumentException("complete type is null!");
        this.completeType = completeType;
        this.savepoint = null;
    }

    public MTSRollbackStatement(Identifier savepoint) {
        this.completeType = null;
        if (savepoint == null) throw new IllegalArgumentException("savepoint is null!");
        this.savepoint = savepoint;
    }

    /**
     * @return null if roll back to SAVEPOINT
     */
    public CompleteType getCompleteType() {
        return completeType;
    }

    /**
     * @return null for roll back the whole transaction
     */
    public Identifier getSavepoint() {
        return savepoint;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
