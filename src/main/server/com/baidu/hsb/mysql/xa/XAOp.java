/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.mysql.xa;

/**
 * @author brucexx
 *
 */
public enum XAOp {

    START(1), END(2), PREPARE(3), COMMIT(4), ROLLBACK(5);

    private int code;

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    private XAOp(int code) {
        this.code = code;
    }

}
