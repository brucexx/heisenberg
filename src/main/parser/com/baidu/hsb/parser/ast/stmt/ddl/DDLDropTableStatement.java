/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.ddl;

import java.util.Collections;
import java.util.List;

import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class DDLDropTableStatement implements DDLStatement {
    public static enum Mode {
        UNDEF,
        RESTRICT,
        CASCADE
    }

    private final List<Identifier> tableNames;
    private final boolean temp;
    private final boolean ifExists;
    private final Mode mode;

    public DDLDropTableStatement(List<Identifier> tableNames, boolean temp, boolean ifExists) {
        this(tableNames, temp, ifExists, Mode.UNDEF);
    }

    public DDLDropTableStatement(List<Identifier> tableNames, boolean temp, boolean ifExists, Mode mode) {
        if (tableNames == null || tableNames.isEmpty()) {
            this.tableNames = Collections.emptyList();
        } else {
            this.tableNames = tableNames;
        }
        this.temp = temp;
        this.ifExists = ifExists;
        this.mode = mode;
    }

    public List<Identifier> getTableNames() {
        return tableNames;
    }

    public boolean isTemp() {
        return temp;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
