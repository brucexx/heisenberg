/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.fragment;

import com.baidu.hsb.parser.ast.ASTNode;
import com.baidu.hsb.parser.ast.expression.primary.ParamMarker;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * @author xiongzhao@baidu.com
 */
public class Limit implements ASTNode {
    /** when it is null, to sql generated must ignore this number */
    private final Number offset;
    private final Number size;
    private final ParamMarker offsetP;
    private final ParamMarker sizeP;

    public Limit(Number offset, Number size) {
        if (offset == null) throw new IllegalArgumentException();
        if (size == null) throw new IllegalArgumentException();
        this.offset = offset;
        this.size = size;
        this.offsetP = null;
        this.sizeP = null;
    }

    public Limit(Number offset, ParamMarker sizeP) {
        if (offset == null) throw new IllegalArgumentException();
        if (sizeP == null) throw new IllegalArgumentException();
        this.offset = offset;
        this.size = null;
        this.offsetP = null;
        this.sizeP = sizeP;
    }

    public Limit(ParamMarker offsetP, Number size) {
        if (offsetP == null) throw new IllegalArgumentException();
        if (size == null) throw new IllegalArgumentException();
        this.offset = null;
        this.size = size;
        this.offsetP = offsetP;
        this.sizeP = null;
    }

    public Limit(ParamMarker offsetP, ParamMarker sizeP) {
        if (offsetP == null) throw new IllegalArgumentException();
        if (sizeP == null) throw new IllegalArgumentException();
        this.offset = null;
        this.size = null;
        this.offsetP = offsetP;
        this.sizeP = sizeP;
    }

    /**
     * @return {@link ParamMarker} or {@link Number}
     */
    public Object getOffset() {
        return offset == null ? offsetP : offset;
    }

    /**
     * @return {@link ParamMarker} or {@link Number}
     */
    public Object getSize() {
        return size == null ? sizeP : size;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
