/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.mysql.xa;

/**
 * @author brucexx
 *
 */
public enum XAOp {

	START(0), END_PREPARE(1), COMMIT(2), ROLLBACK(3);

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
