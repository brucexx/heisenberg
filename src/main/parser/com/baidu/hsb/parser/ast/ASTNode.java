/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast;

import java.io.Serializable;

import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public interface ASTNode extends Cloneable,Serializable{
    void accept(SQLASTVisitor visitor);
}
