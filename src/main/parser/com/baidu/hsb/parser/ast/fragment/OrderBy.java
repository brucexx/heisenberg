/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.fragment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.baidu.hsb.parser.ast.ASTNode;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.util.Pair;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class OrderBy implements ASTNode {
    /** might be {@link LinkedList} */
    private final List<Pair<Expression, SortOrder>> orderByList;

    public List<Pair<Expression, SortOrder>> getOrderByList() {
        return orderByList;
    }

    /**
     * performance tip: linked list is used
     */
    public OrderBy() {
        this.orderByList = new LinkedList<Pair<Expression, SortOrder>>();
    }

    /**
     * performance tip: expect to have only 1 order item
     */
    public OrderBy(Expression expr, SortOrder order) {
        this.orderByList = new ArrayList<Pair<Expression, SortOrder>>(1);
        this.orderByList.add(new Pair<Expression, SortOrder>(expr, order));
    }

    public OrderBy(List<Pair<Expression, SortOrder>> orderByList) {
        if (orderByList == null) throw new IllegalArgumentException("order list is null");
        this.orderByList = orderByList;
    }

    public OrderBy addOrderByItem(Expression expr, SortOrder order) {
        orderByList.add(new Pair<Expression, SortOrder>(expr, order));
        return this;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
