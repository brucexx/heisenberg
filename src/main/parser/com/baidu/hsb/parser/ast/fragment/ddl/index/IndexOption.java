/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.fragment.ddl.index;

import com.baidu.hsb.parser.ast.ASTNode;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.expression.primary.literal.LiteralString;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class IndexOption implements ASTNode {
    public static enum IndexType {
        BTREE,
        HASH
    }

    private final Expression keyBlockSize;
    private final IndexType indexType;
    private final Identifier parserName;
    private final LiteralString comment;

    public IndexOption(Expression keyBlockSize) {
        this.keyBlockSize = keyBlockSize;
        this.indexType = null;
        this.parserName = null;
        this.comment = null;
    }

    public IndexOption(IndexType indexType) {
        this.keyBlockSize = null;
        this.indexType = indexType;
        this.parserName = null;
        this.comment = null;
    }

    public IndexOption(Identifier parserName) {
        this.keyBlockSize = null;
        this.indexType = null;
        this.parserName = parserName;
        this.comment = null;
    }

    public IndexOption(LiteralString comment) {
        this.keyBlockSize = null;
        this.indexType = null;
        this.parserName = null;
        this.comment = comment;
    }

    public Expression getKeyBlockSize() {
        return keyBlockSize;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public Identifier getParserName() {
        return parserName;
    }

    public LiteralString getComment() {
        return comment;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
