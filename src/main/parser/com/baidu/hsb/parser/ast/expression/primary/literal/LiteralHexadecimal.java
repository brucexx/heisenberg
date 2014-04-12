/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.expression.primary.literal;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.baidu.hsb.parser.util.ParseString;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class LiteralHexadecimal extends Literal {
    private byte[] bytes;
    private final String introducer;
    private final String charset;
    private final char[] string;
    private final int offset;
    private final int size;

    /**
     * @param introducer e.g. "_latin1"
     * @param string e.g. "select x'89df'"
     * @param offset e.g. 9
     * @param size e.g. 4
     */
    public LiteralHexadecimal(String introducer, char[] string, int offset, int size, String charset) {
        super();
        if (string == null || offset + size > string.length) throw new IllegalArgumentException("hex text is invalid");
        if (charset == null) throw new IllegalArgumentException("charset is null");
        this.introducer = introducer;
        this.charset = charset;
        this.string = string;
        this.offset = offset;
        this.size = size;
    }

    public String getText() {
        return new String(string, offset, size);
    }

    public String getIntroducer() {
        return introducer;
    }

    public void appendTo(StringBuilder sb) {
        sb.append(string, offset, size);
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        try {
            this.bytes = ParseString.hexString2Bytes(string, offset, size);
            return new String(bytes, introducer == null ? charset : introducer.substring(1));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("", e);
        }
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
