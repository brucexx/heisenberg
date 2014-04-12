/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.baidu.hsb.parser.ast.expression.AbstractExpression;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: InExpressionList.java, v 0.1 2013年12月26日 下午6:18:54 HI:brucest0078 Exp $
 */
public class InExpressionList extends AbstractExpression {
    private List<Expression> list;

    public InExpressionList(List<Expression> list) {
        if (list == null || list.size() == 0) {
            this.list = Collections.emptyList();
        } else if (list instanceof ArrayList) {
            this.list = list;
        } else {
            this.list = new ArrayList<Expression>(list);
        }
    }

    /**
     * @return never null
     */
    public List<Expression> getList() {
        return list;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_PRIMARY;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }

    private List<Expression> replaceList;

    public void setReplaceExpr(List<Expression> replaceList) {
        this.replaceList = replaceList;
    }

    public void clearReplaceExpr() {
        this.replaceList = null;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        if (replaceList == null) {
            visitor.visit(this);
        } else {
            List<Expression> temp = list;
            list = replaceList;
            visitor.visit(this);
            list = temp;
        }
    }
}
