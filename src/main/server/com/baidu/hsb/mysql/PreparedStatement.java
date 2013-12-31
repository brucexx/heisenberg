/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql;

/**
 * @author xiongzhao@baidu.com 2012-8-28
 */
public class PreparedStatement {

    private long id;
    private String statement;
    private int columnsNumber;
    private int parametersNumber;
    private int[] parametersType;

    public PreparedStatement(long id, String statement, int columnsNumber, int parametersNumber) {
        this.id = id;
        this.statement = statement;
        this.columnsNumber = columnsNumber;
        this.parametersNumber = parametersNumber;
        this.parametersType = new int[parametersNumber];
    }

    public long getId() {
        return id;
    }

    public String getStatement() {
        return statement;
    }

    public int getColumnsNumber() {
        return columnsNumber;
    }

    public int getParametersNumber() {
        return parametersNumber;
    }

    public int[] getParametersType() {
        return parametersType;
    }

}
