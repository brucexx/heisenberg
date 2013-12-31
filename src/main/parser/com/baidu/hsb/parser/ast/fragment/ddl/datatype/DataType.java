/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.fragment.ddl.datatype;

import java.util.List;

import com.baidu.hsb.parser.ast.ASTNode;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * <code>spatial data type</code> for MyISAM is not supported
 * 
 * @author xiongzhao@baidu.com
 */
public class DataType implements ASTNode {
    public static enum DataTypeName {
        BIT,
        TINYINT,
        SMALLINT,
        MEDIUMINT,
        INT,
        BIGINT,
        REAL,
        DOUBLE,
        FLOAT,
        DECIMAL,
        DATE,
        TIME,
        TIMESTAMP,
        DATETIME,
        YEAR,
        CHAR,
        VARCHAR,
        BINARY,
        VARBINARY,
        TINYBLOB,
        BLOB,
        MEDIUMBLOB,
        LONGBLOB,
        TINYTEXT,
        TEXT,
        MEDIUMTEXT,
        LONGTEXT,
        ENUM,
        SET
    }

    //              BIT[(length)]
    //            | TINYINT[(length)] [UNSIGNED] [ZEROFILL]
    //            | SMALLINT[(length)] [UNSIGNED] [ZEROFILL]
    //            | MEDIUMINT[(length)] [UNSIGNED] [ZEROFILL]
    //            | INT[(length)] [UNSIGNED] [ZEROFILL]
    //            | INTEGER[(length)] [UNSIGNED] [ZEROFILL]
    //            | BIGINT[(length)] [UNSIGNED] [ZEROFILL]
    //            | DOUBLE[(length,decimals)] [UNSIGNED] [ZEROFILL]
    //            | REAL[(length,decimals)] [UNSIGNED] [ZEROFILL]
    //            | FLOAT[(length,decimals)] [UNSIGNED] [ZEROFILL]
    //            | DECIMAL[(length[,decimals])] [UNSIGNED] [ZEROFILL]
    //            | NUMERIC[(length[,decimals])] [UNSIGNED] [ZEROFILL] 同上
    //            | DATE
    //            | TIME
    //            | TIMESTAMP
    //            | DATETIME
    //            | YEAR
    //            | CHAR[(length)][CHARACTER SET charset_name] [COLLATE collation_name]
    //            | VARCHAR(length)[CHARACTER SET charset_name] [COLLATE collation_name]
    //            | BINARY[(length)]
    //            | VARBINARY(length)
    //            | TINYBLOB
    //            | BLOB
    //            | MEDIUMBLOB
    //            | LONGBLOB
    //            | TINYTEXT [BINARY][CHARACTER SET charset_name] [COLLATE collation_name]
    //            | TEXT [BINARY][CHARACTER SET charset_name] [COLLATE collation_name]
    //            | MEDIUMTEXT [BINARY][CHARACTER SET charset_name] [COLLATE collation_name]
    //            | LONGTEXT [BINARY][CHARACTER SET charset_name] [COLLATE collation_name]
    //            | ENUM(value1,value2,value3,...)[CHARACTER SET charset_name] [COLLATE collation_name]
    //            | SET(value1,value2,value3,...)[CHARACTER SET charset_name] [COLLATE collation_name]
    //            | spatial_type 不支持

    private final DataTypeName typeName;
    private final boolean unsigned;
    private final boolean zerofill;
    /** for text only */
    private final boolean binary;
    private final Expression length;
    private final Expression decimals;
    private final Identifier charSet;
    private final Identifier collation;
    private final List<Expression> collectionVals;

    public DataType(DataTypeName typeName, boolean unsigned, boolean zerofill, boolean binary, Expression length,
                    Expression decimals, Identifier charSet, Identifier collation, List<Expression> collectionVals) {
        if (typeName == null) throw new IllegalArgumentException("typeName is null");
        this.typeName = typeName;
        this.unsigned = unsigned;
        this.zerofill = zerofill;
        this.binary = binary;
        this.length = length;
        this.decimals = decimals;
        this.charSet = charSet;
        this.collation = collation;
        this.collectionVals = collectionVals;
    }

    public DataTypeName getTypeName() {
        return typeName;
    }

    public boolean isUnsigned() {
        return unsigned;
    }

    public boolean isZerofill() {
        return zerofill;
    }

    public boolean isBinary() {
        return binary;
    }

    public Expression getLength() {
        return length;
    }

    public Expression getDecimals() {
        return decimals;
    }

    public Identifier getCharSet() {
        return charSet;
    }

    public Identifier getCollation() {
        return collation;
    }

    public List<Expression> getCollectionVals() {
        return collectionVals;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
