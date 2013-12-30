/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.misc;

import com.baidu.hsb.parser.ast.expression.primary.PrimaryExpression;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: UserExpression.java, v 0.1 2013年12月26日 下午6:19:25 HI:brucest0078 Exp $
 */
public class UserExpression extends PrimaryExpression {
    private final String userAtHost;

    /**
     * @param userAtHost
     */
    public UserExpression(String userAtHost) {
        super();
        this.userAtHost = userAtHost;
    }

    public String getUserAtHost() {
        return userAtHost;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
