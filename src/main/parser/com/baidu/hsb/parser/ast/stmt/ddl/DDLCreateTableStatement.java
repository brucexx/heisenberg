/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt.ddl;

import java.util.ArrayList;
import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.fragment.ddl.ColumnDefinition;
import com.baidu.hsb.parser.ast.fragment.ddl.TableOptions;
import com.baidu.hsb.parser.ast.fragment.ddl.index.IndexDefinition;
import com.baidu.hsb.parser.ast.stmt.dml.DMLSelectStatement;
import com.baidu.hsb.parser.util.Pair;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * NOT FULL AST: foreign key, ... not supported
 * 
 * @author xiongzhao@baidu.com
 */
public class DDLCreateTableStatement implements DDLStatement {
    public static enum SelectOption {
        IGNORED,
        REPLACE
    }

    private final boolean temporary;
    private final boolean ifNotExists;
    private final Identifier table;
    private final List<Pair<Identifier, ColumnDefinition>> colDefs;
    private IndexDefinition primaryKey;
    private final List<Pair<Identifier, IndexDefinition>> uniqueKeys;
    private final List<Pair<Identifier, IndexDefinition>> keys;
    private final List<Pair<Identifier, IndexDefinition>> fullTextKeys;
    private final List<Pair<Identifier, IndexDefinition>> spatialKeys;
    private final List<Expression> checks;
    private TableOptions tableOptions;
    private Pair<SelectOption, DMLSelectStatement> select;

    public DDLCreateTableStatement(boolean temporary, boolean ifNotExists, Identifier table) {
        this.table = table;
        this.temporary = temporary;
        this.ifNotExists = ifNotExists;
        this.colDefs = new ArrayList<Pair<Identifier, ColumnDefinition>>(4);
        this.uniqueKeys = new ArrayList<Pair<Identifier, IndexDefinition>>(1);
        this.keys = new ArrayList<Pair<Identifier, IndexDefinition>>(2);
        this.fullTextKeys = new ArrayList<Pair<Identifier, IndexDefinition>>(1);
        this.spatialKeys = new ArrayList<Pair<Identifier, IndexDefinition>>(1);
        this.checks = new ArrayList<Expression>(1);
    }

    public DDLCreateTableStatement setTableOptions(TableOptions tableOptions) {
        this.tableOptions = tableOptions;
        return this;
    }

    public DDLCreateTableStatement addColumnDefinition(Identifier colname, ColumnDefinition def) {
        colDefs.add(new Pair<Identifier, ColumnDefinition>(colname, def));
        return this;
    }

    public DDLCreateTableStatement setPrimaryKey(IndexDefinition def) {
        primaryKey = def;
        return this;
    }

    public DDLCreateTableStatement addUniqueIndex(Identifier colname, IndexDefinition def) {
        uniqueKeys.add(new Pair<Identifier, IndexDefinition>(colname, def));
        return this;
    }

    public DDLCreateTableStatement addIndex(Identifier colname, IndexDefinition def) {
        keys.add(new Pair<Identifier, IndexDefinition>(colname, def));
        return this;
    }

    public DDLCreateTableStatement addFullTextIndex(Identifier colname, IndexDefinition def) {
        fullTextKeys.add(new Pair<Identifier, IndexDefinition>(colname, def));
        return this;
    }

    public DDLCreateTableStatement addSpatialIndex(Identifier colname, IndexDefinition def) {
        spatialKeys.add(new Pair<Identifier, IndexDefinition>(colname, def));
        return this;
    }

    public DDLCreateTableStatement addCheck(Expression check) {
        checks.add(check);
        return this;
    }

    public TableOptions getTableOptions() {
        return tableOptions;
    }

    public Pair<SelectOption, DMLSelectStatement> getSelect() {
        return select;
    }

    public void setSelect(SelectOption option, DMLSelectStatement select) {
        this.select = new Pair<DDLCreateTableStatement.SelectOption, DMLSelectStatement>(option, select);
    }

    public boolean isTemporary() {
        return temporary;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public Identifier getTable() {
        return table;
    }

    /**
     * @return key := columnName
     */
    public List<Pair<Identifier, ColumnDefinition>> getColDefs() {
        return colDefs;
    }

    public IndexDefinition getPrimaryKey() {
        return primaryKey;
    }

    public List<Pair<Identifier, IndexDefinition>> getUniqueKeys() {
        return uniqueKeys;
    }

    public List<Pair<Identifier, IndexDefinition>> getKeys() {
        return keys;
    }

    public List<Pair<Identifier, IndexDefinition>> getFullTextKeys() {
        return fullTextKeys;
    }

    public List<Pair<Identifier, IndexDefinition>> getSpatialKeys() {
        return spatialKeys;
    }

    public List<Expression> getChecks() {
        return checks;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
