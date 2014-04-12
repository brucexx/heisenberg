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
public class DALSetCharacterSetStatement implements SQLStatement {
    private final String charset;

    public DALSetCharacterSetStatement() {
        this.charset = null;
    }

    public DALSetCharacterSetStatement(String charset) {
        if (charset == null) throw new IllegalArgumentException("charsetName is null");
        this.charset = charset;
    }

    public boolean isDefault() {
        return charset == null;
    }

    public String getCharset() {
        return charset;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
