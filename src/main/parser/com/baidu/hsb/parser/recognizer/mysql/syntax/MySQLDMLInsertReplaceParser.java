/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.recognizer.mysql.syntax;

import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.PUNC_COMMA;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.PUNC_LEFT_PAREN;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.PUNC_RIGHT_PAREN;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.RowExpression;
import com.baidu.hsb.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author xiongzhao@baidu.com
 */
public abstract class MySQLDMLInsertReplaceParser extends MySQLDMLParser {
    public MySQLDMLInsertReplaceParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
    }

    protected List<RowExpression> rowList() throws SQLSyntaxErrorException {
        List<RowExpression> valuesList;
        List<Expression> tempRowValue = rowValue();
        if (lexer.token() == PUNC_COMMA) {
            valuesList = new LinkedList<RowExpression>();
            valuesList.add(new RowExpression(tempRowValue));
            for (; lexer.token() == PUNC_COMMA;) {
                lexer.nextToken();
                tempRowValue = rowValue();
                valuesList.add(new RowExpression(tempRowValue));
            }
        } else {
            valuesList = new ArrayList<RowExpression>(1);
            valuesList.add(new RowExpression(tempRowValue));
        }
        return valuesList;
    }

    /**
     * first token is <code>(</code>
     */
    private List<Expression> rowValue() throws SQLSyntaxErrorException {
        match(PUNC_LEFT_PAREN);
        if (lexer.token() == PUNC_RIGHT_PAREN) {
            return Collections.emptyList();
        }
        List<Expression> row;
        Expression expr = exprParser.expression();
        if (lexer.token() == PUNC_COMMA) {
            row = new LinkedList<Expression>();
            row.add(expr);
            for (; lexer.token() == PUNC_COMMA;) {
                lexer.nextToken();
                expr = exprParser.expression();
                row.add(expr);
            }
        } else {
            row = new ArrayList<Expression>(1);
            row.add(expr);
        }
        match(PUNC_RIGHT_PAREN);
        return row;
    }
}
