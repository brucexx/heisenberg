/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.recognizer.mysql.syntax;

import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.KW_DELETE;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.KW_FROM;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.KW_LIMIT;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.KW_ORDER;
import static com.baidu.hsb.parser.recognizer.mysql.MySQLToken.KW_WHERE;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.fragment.Limit;
import com.baidu.hsb.parser.ast.fragment.OrderBy;
import com.baidu.hsb.parser.ast.fragment.tableref.TableReferences;
import com.baidu.hsb.parser.ast.stmt.dml.DMLDeleteStatement;
import com.baidu.hsb.parser.recognizer.mysql.MySQLToken;
import com.baidu.hsb.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author xiongzhao@baidu.com
 */
public class MySQLDMLDeleteParser extends MySQLDMLParser {
    public MySQLDMLDeleteParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
    }

    private static enum SpecialIdentifier {
        QUICK
    }

    private static final Map<String, SpecialIdentifier> specialIdentifiers = new HashMap<String, SpecialIdentifier>();
    static {
        specialIdentifiers.put("QUICK", SpecialIdentifier.QUICK);
    }

    /**
     * first token is {@link MySQLToken#KW_DELETE} <code><pre>
     * 'DELETE' 'LOW_PRIORITY'? 'QUICK'? 'IGNORE'? (
     *     'FROM' tid ( (',' tid)* 'USING' table_refs ('WHERE' cond)?  
     *                | ('WHERE' cond)? ('ORDER' 'BY' ids)? ('LIMIT' count)?  )  // single table
     *    | tid (',' tid)* 'FROM' table_refs ('WHERE' cond)? )
     * </pre></code>
     */
    public DMLDeleteStatement delete() throws SQLSyntaxErrorException {
        match(KW_DELETE);
        boolean lowPriority = false;
        boolean quick = false;
        boolean ignore = false;
        loopOpt: for (;; lexer.nextToken()) {
            switch (lexer.token()) {
            case KW_LOW_PRIORITY:
                lowPriority = true;
                break;
            case KW_IGNORE:
                ignore = true;
                break;
            case IDENTIFIER:
                SpecialIdentifier si = specialIdentifiers.get(lexer.stringValueUppercase());
                if (SpecialIdentifier.QUICK == si) {
                    quick = true;
                    break;
                }
            default:
                break loopOpt;
            }
        }
        List<Identifier> tempList;
        TableReferences tempRefs;
        Expression tempWhere;
        if (lexer.token() == KW_FROM) {
            lexer.nextToken();
            Identifier id = identifier();
            tempList = new ArrayList<Identifier>(1);
            tempList.add(id);
            switch (lexer.token()) {
            case PUNC_COMMA:
                tempList = buildIdList(id);
            case KW_USING:
                lexer.nextToken();
                tempRefs = tableRefs();
                if (lexer.token() == KW_WHERE) {
                    lexer.nextToken();
                    tempWhere = exprParser.expression();
                    return new DMLDeleteStatement(lowPriority, quick, ignore, tempList, tempRefs, tempWhere);
                }
                return new DMLDeleteStatement(lowPriority, quick, ignore, tempList, tempRefs);
            case KW_WHERE:
            case KW_ORDER:
            case KW_LIMIT:
                break;
            default:
                return new DMLDeleteStatement(lowPriority, quick, ignore, id);
            }
            tempWhere = null;
            OrderBy orderBy = null;
            Limit limit = null;
            if (lexer.token() == KW_WHERE) {
                lexer.nextToken();
                tempWhere = exprParser.expression();
            }
            if (lexer.token() == KW_ORDER) {
                orderBy = orderBy();
            }
            if (lexer.token() == KW_LIMIT) {
                limit = limit();
            }
            return new DMLDeleteStatement(lowPriority, quick, ignore, id, tempWhere, orderBy, limit);
        }

        tempList = idList();
        match(KW_FROM);
        tempRefs = tableRefs();
        if (lexer.token() == KW_WHERE) {
            lexer.nextToken();
            tempWhere = exprParser.expression();
            return new DMLDeleteStatement(lowPriority, quick, ignore, tempList, tempRefs, tempWhere);
        }
        return new DMLDeleteStatement(lowPriority, quick, ignore, tempList, tempRefs);
    }

}
