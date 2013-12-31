/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.recognizer.mysql.syntax;

import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.KW_IGNORE;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.KW_LOW_PRIORITY;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.KW_SET;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.KW_UPDATE;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.KW_WHERE;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.OP_ASSIGN;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.OP_EQUALS;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.PUNC_COMMA;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.fragment.Limit;
import com.baidu.hsb.parser.ast.fragment.OrderBy;
import com.baidu.hsb.parser.ast.fragment.tableref.TableReferences;
import com.baidu.hsb.parser.ast.stmt.dml.DMLUpdateStatement;
import com.baidu.hsb.parser.recognizer.mysql.lexer.MySQLLexer;
import com.baidu.hsb.parser.util.Pair;

/**
 * @author xiongzhao@baidu.com
 */
public class MySQLDMLUpdateParser extends MySQLDMLParser {
    public MySQLDMLUpdateParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
    }

    /**
     * nothing has been pre-consumed <code><pre>
     * 'UPDATE' 'LOW_PRIORITY'? 'IGNORE'? table_reference
     *   'SET' colName ('='|':=') (expr|'DEFAULT') (',' colName ('='|':=') (expr|'DEFAULT'))*
     *     ('WHERE' cond)?
     *     {singleTable}? => ('ORDER' 'BY' orderBy)?  ('LIMIT' count)?
     * </pre></code>
     */
    public DMLUpdateStatement update() throws SQLSyntaxErrorException {
        match(KW_UPDATE);
        boolean lowPriority = false;
        boolean ignore = false;
        if (lexer.token() == KW_LOW_PRIORITY) {
            lexer.nextToken();
            lowPriority = true;
        }
        if (lexer.token() == KW_IGNORE) {
            lexer.nextToken();
            ignore = true;
        }
        TableReferences tableRefs = tableRefs();
        match(KW_SET);
        List<Pair<Identifier, Expression>> values;
        Identifier col = identifier();
        match(OP_EQUALS, OP_ASSIGN);
        Expression expr = exprParser.expression();
        if (lexer.token() == PUNC_COMMA) {
            values = new LinkedList<Pair<Identifier, Expression>>();
            values.add(new Pair<Identifier, Expression>(col, expr));
            for (; lexer.token() == PUNC_COMMA;) {
                lexer.nextToken();
                col = identifier();
                match(OP_EQUALS, OP_ASSIGN);
                expr = exprParser.expression();
                values.add(new Pair<Identifier, Expression>(col, expr));
            }
        } else {
            values = new ArrayList<Pair<Identifier, Expression>>(1);
            values.add(new Pair<Identifier, Expression>(col, expr));
        }
        Expression where = null;
        if (lexer.token() == KW_WHERE) {
            lexer.nextToken();
            where = exprParser.expression();
        }
        OrderBy orderBy = null;
        Limit limit = null;
        if (tableRefs.isSingleTable()) {
            orderBy = orderBy();
            limit = limit();
        }
        return new DMLUpdateStatement(lowPriority, ignore, tableRefs, values, where, orderBy, limit);
    }
}
