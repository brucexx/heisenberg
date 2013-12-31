/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.dal;

import com.baidu.hsb.parser.ast.stmt.SQLStatement;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class DALSetNamesStatement implements SQLStatement {
    private final String charsetName;
    private final String collationName;

    public DALSetNamesStatement() {
        this.charsetName = null;
        this.collationName = null;
    }

    public DALSetNamesStatement(String charsetName, String collationName) {
        if (charsetName == null) throw new IllegalArgumentException("charsetName is null");
        this.charsetName = charsetName;
        this.collationName = collationName;
    }

    public boolean isDefault() {
        return charsetName == null;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public String getCollationName() {
        return collationName;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
