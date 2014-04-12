/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.stmt;

import com.baidu.hsb.parser.ast.ASTNode;

/**
 * @author xiongzhao@baidu.com
 */
public interface SQLStatement extends ASTNode {
    public static enum StmtType {
        DML_SELECT,
        DML_DELETE,
        DML_INSERT,
        DML_REPLACE,
        DML_UPDATE,
        DML_CALL,
        DAL_SET,
        DAL_SHOW,
        MTL_START,
        /** COMMIT or ROLLBACK */
        MTL_TERMINATE,
        MTL_ISOLATION
    }
}
