/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route.util;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.baidu.hsb.config.model.RealTableCache;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.config.model.config.TableConfig;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.comparison.InExpression;
import com.baidu.hsb.parser.ast.expression.misc.InExpressionList;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.expression.primary.RowExpression;
import com.baidu.hsb.parser.ast.expression.primary.literal.Literal;
import com.baidu.hsb.parser.ast.fragment.tableref.TableReference;
import com.baidu.hsb.parser.ast.fragment.tableref.TableReferences;
import com.baidu.hsb.parser.ast.stmt.SQLStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLCondition;
import com.baidu.hsb.parser.ast.stmt.dml.DMLInsertReplaceStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLInsertStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLStatement;
import com.baidu.hsb.parser.recognizer.SQLParserDelegate;
import com.baidu.hsb.parser.visitor.MySQLOutputASTVisitor;
import com.baidu.hsb.route.visitor.PartitionKeyVisitor;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: SqlUtil.java, v 0.1 2013年12月23日 下午9:42:58 HI:brucest0078 Exp $
 */
public class SqlUtil {

    private static final Logger LOGGER = Logger.getLogger(SqlUtil.class);

    /**
     * 
     * 
     * @param oSql
     * @param tbName
     * @param dnIndex
     * @param tbMap
     * @return
     */
    public static String[] scan(SQLStatement ast, SchemaConfig schema, String oSql, Set<String> tbNameSet, int dnIndex,
            Map<Integer, List<String>> tbMap) {

        if (tbMap == null) {
            return new String[] { oSql };
        }
        List<String> sqlList = new ArrayList<String>();
        List<String> prefixSet = tbMap.get(dnIndex);
        if (prefixSet != null && !prefixSet.isEmpty() && StringUtil.isNotEmpty(oSql)) {

            if (ast instanceof DMLStatement) {
                try {
                    SQLStatement newAst = SQLParserDelegate.parse(oSql);

                    // 使用ast语法替换,所以这里复制一份出来
                    DMLStatement ast_t = (DMLStatement) newAst;
                    // 扫描出来需要替换的identifer
                    Map<String, List<Identifier>> result = new HashMap<String, List<Identifier>>();

                    if (ast instanceof DMLCondition) {
                        DMLCondition _ast = (DMLCondition) ast_t;
                        TableReferences tb = _ast.getTables();
                        // 扫描出来需要替换的identifer
                        scanIdentifer(result, tb);
                    } else if (ast instanceof DMLInsertStatement) {
                        DMLInsertStatement _ast = (DMLInsertStatement) ast_t;
                        result.put(_ast.getTable().getIdTextUpUnescape(), new ArrayList<Identifier>());
                        result.get(_ast.getTable().getIdTextUpUnescape()).add(_ast.getTable());
                    }

                    for (int i = 0; i < prefixSet.size(); i++) {
                        String tbPrefix = prefixSet.get(i);
                        String tmpSql = oSql;
                        for (String tbName : tbNameSet) {
                            String rTb = RealTableCache.getRealByUp(tbName);
                            for (Identifier iTb : result.get(tbName)) {
                                iTb.setIdText(rTb + tbPrefix.toUpperCase());
                            }

                            TableConfig tc = schema.getTables().get(tbName);
                            if (tc != null && tc.getRule().isTbShard()) {
                                ast_t.accept(new MySQLOutputASTVisitor(new StringBuilder()));
                                tmpSql = ast_t.toString();
                            }
                        }
                        sqlList.add(tmpSql);
                    }
                } catch (Exception e) {
                    LOGGER.error("oSql parse error:", e);
                }

            } else {
                for (int i = 0; i < prefixSet.size(); i++) {
                    String tbPrefix = prefixSet.get(i);
                    String tmpSql = oSql;
                    for (String tbName : tbNameSet) {
                        String rTb = RealTableCache.getRealByUp(tbName);
                        TableConfig tc = schema.getTables().get(tbName);
                        if (tc != null && tc.getRule().isTbShard()) {
                            tmpSql = replaceSqlTb(tmpSql, rTb, rTb + tbPrefix);
                        }
                    }
                    sqlList.add(tmpSql);
                }
            }

            return sqlList.toArray(new String[sqlList.size()]);
        } else {
            return new String[] { oSql };
        }
    }

    /**
     * 
     * @param ast
     * @param schema
     * @param oSql
     * @param columnValues
     * @param tc
     * @param tbSet
     * @param dnIdx
     * @return
     */
    public static String[] renderTBNew(SQLStatement ast, SchemaConfig schema, String oSql,
            Map<String, List<Object>> columnValues, TableConfig tc, Set<String> tbSet, int dnIdx) {
        List<String> sqlStrs = new ArrayList<String>();
        try {
            if (ast instanceof DMLStatement) {
                SQLStatement newAst = SQLParserDelegate.parse(oSql);

                if (ast instanceof DMLCondition) {
                    renderCondition(sqlStrs, ast, newAst, schema, oSql, columnValues, tc, tbSet, dnIdx);
                }

                if (ast instanceof DMLInsertReplaceStatement) {
                    renderInsert(sqlStrs, ast, newAst, schema, oSql, columnValues, tc, tbSet, dnIdx);
                }
            }

            if (sqlStrs.size() > 0)
                return sqlStrs.toArray(new String[sqlStrs.size()]);

        } catch (Exception e) {
            LOGGER.error("sql:" + oSql + "执行异常", e);
        }

        // 降级用普通的正则替换
        return renderTB(schema, oSql, columnValues, tc, tbSet, dnIdx);

    }

    private static void renderInsert(List<String> sqlStrs, SQLStatement ast, SQLStatement newAst, SchemaConfig schema,
            String oSql, Map<String, List<Object>> columnValues, TableConfig tc, Set<String> tbSet, int dnIdx) {
        Map<String, Set<Object>> tbPrefixSet = VelocityUtil.evalTbRuleArrayWithResult(tc.getRule(), columnValues);

        DMLInsertReplaceStatement old = (DMLInsertReplaceStatement) ast;
        // 使用ast语法替换,所以这里复制一份出来
        DMLInsertReplaceStatement _ast = (DMLInsertReplaceStatement) newAst;

        // 先计算出对应列的index
        String hitCol = "";
        int i = -1;
        outer: for (Identifier colIdentifer : old.getColumnNameList()) {
            String col = colIdentifer.getIdTextUpUnescape();
            i++;
            for (String t : tc.getRule().getColumns()) {
                if (StringUtil.equalsIgnoreCase(col, t)) {
                    hitCol = StringUtil.upperCase(t);
                    break outer;
                }
            }
        }
        if (StringUtil.isEmpty(hitCol)) {
            // 没有符合列
            return;
        }

        for (Map.Entry<String, Set<Object>> entry : tbPrefixSet.entrySet()) {
            String tbPrefix = entry.getKey();
            // 这个是tbPrefix对应col的值
            Set<Object> set = entry.getValue();

            String tmp = oSql;
            for (String tbName : tbSet) {
                String rTb = RealTableCache.getRealByUp(tbName);
                TableConfig scanTc = schema.getTables().get(tbName);
                if (scanTc != null && scanTc.getRule().isTbShard()) {
                    if (scanTc.getRule().getTbIndexMap().get(tbPrefix) == dnIdx) {
                        // 替换table
                        _ast.getTable().setIdText(rTb + tbPrefix.toUpperCase());
                        // 替换 values
                        List<RowExpression> replaceRow = new ArrayList<RowExpression>();
                        // 直接拆分
                        for (RowExpression rExpr : old.getRowList()) {
                            Expression expr = rExpr.getRowExprList().get(i);
                            if (expr instanceof Literal) {
                                Literal liter = (Literal) expr;
                                // 看这个值是否
                                if (set.contains(liter.getValue())) {
                                    replaceRow.add(rExpr);
                                }
                            }
                        }
                        _ast.setReplaceRowList(replaceRow);
                        SQLStatement _ssmt = (SQLStatement) _ast;
                        _ssmt.accept(new MySQLOutputASTVisitor(new StringBuilder()));
                        tmp = _ssmt.toString();
                    }
                }
            }
            // 如果没一个替换的
            if (!StringUtil.equals(tmp, oSql)) {
                sqlStrs.add(tmp);
            }
        }

    }

    private static void renderCondition(List<String> sqlStrs, SQLStatement ast, SQLStatement newAst,
            SchemaConfig schema, String oSql, Map<String, List<Object>> columnValues, TableConfig tc, Set<String> tbSet,
            int dnIdx) {
        Map<String, Set<Object>> tbPrefixSet = VelocityUtil.evalTbRuleArrayWithResult(tc.getRule(), columnValues);
        // 只需要从visitor里面取出对应的statement
        DMLCondition old = (DMLCondition) ast;

        // 使用ast语法替换,所以这里复制一份出来
        DMLCondition _ast = (DMLCondition) newAst;

        TableReferences tb = _ast.getTables();
        // 扫描出来需要替换的identifer
        Map<String, List<Identifier>> result = new HashMap<String, List<Identifier>>();
        scanIdentifer(result, tb);

        for (Map.Entry<String, Set<Object>> entry : tbPrefixSet.entrySet()) {
            String tbPrefix = entry.getKey();
            Set<Object> set = entry.getValue();

            String tmp = oSql;
            for (String tbName : tbSet) {
                String rTb = RealTableCache.getRealByUp(tbName);
                TableConfig scanTc = schema.getTables().get(tbName);
                if (scanTc != null && scanTc.getRule().isTbShard()) {
                    if (scanTc.getRule().getTbIndexMap().get(tbPrefix) == dnIdx && result.containsKey(tbName)) {
                        // 替换table
                        for (Identifier iTb : result.get(tbName)) {
                            iTb.setIdText(rTb + tbPrefix.toUpperCase());
                        }

                        for (int i = 0; i < old.getWhereCondition().size(); i++) {
                            Expression e = old.getWhereCondition().get(i);
                            if (e instanceof InExpression) {
                                InExpression is = (InExpression) e;
                                InExpressionList iList = is.getInExpressionList();
                                List<Expression> rr = new ArrayList<Expression>();

                                for (Expression expr : iList.getList()) {
                                    if (expr instanceof Literal) {
                                        Literal liter = (Literal) expr;
                                        // 需要比较值
                                        if (set.contains(liter.getValue())) {
                                            rr.add(expr);
                                        }
                                    }
                                }
                                ((InExpression) _ast.getWhereCondition().get(i))
                                        .setRightOprand(new InExpressionList(rr));
                            }
                        }

                        SQLStatement _ssmt = (SQLStatement) _ast;
                        _ssmt.accept(new MySQLOutputASTVisitor(new StringBuilder()));
                        tmp = _ssmt.toString();
                    }
                }
            }
            // 如果没一个替换的
            if (!StringUtil.equals(tmp, oSql)) {
                sqlStrs.add(tmp);
            }
        }

    }

    private static void scanIdentifer(Map<String, List<Identifier>> result, TableReferences ref) {
        List<TableReference> tList = ref.getTableReferenceList();
        for (TableReference tr : tList) {
            for (Identifier table : tr.getTables()) {
                String k = table.getIdTextUpUnescape();
                // 可能重名
                if (result.get(k) == null) {
                    result.put(k, new ArrayList<Identifier>());
                }
                result.get(k).add(table);
            }
        }
    }

    /**
     * 
     * 
     * @param oSql
     * @param columnValues
     * @param tc 主表
     * @param tbSet
     * @return
     */
    public static String[] renderTB(SchemaConfig schema, String oSql, Map<String, List<Object>> columnValues,
            TableConfig tc, Set<String> tbSet, int dnIdx) {

        Set<String> tbPrefixSet = VelocityUtil.evalTbRuleArray(tc.getRule(), columnValues);
        List<String> sqlStrs = new ArrayList<String>();
        for (String tbPrefix : tbPrefixSet) {
            String tmp = oSql;
            for (String tbName : tbSet) {
                String rTb = RealTableCache.getRealByUp(tbName);
                TableConfig scanTc = schema.getTables().get(tbName);
                if (scanTc != null && scanTc.getRule().isTbShard()) {
                    if (scanTc.getRule().getTbIndexMap().get(tbPrefix) == dnIdx) {
                        tmp = replaceSqlTb(tmp, rTb, rTb + tbPrefix.toUpperCase());
                    }
                }
            }
            // 如果没一个替换的
            if (!StringUtil.equals(tmp, oSql)) {
                sqlStrs.add(tmp);
            }
        }
        if (sqlStrs.size() > 0)
            return sqlStrs.toArray(new String[sqlStrs.size()]);
        else
            return new String[] { oSql };
    }

    /**
     * 
     * 
     * @param schema
     * @param oSql
     * @param tbNameSet
     * @param dnIndex
     * @param tbMap
     * @return
     */
    public static String replaceSql(SchemaConfig schema, String oSql, Set<String> tbNameSet, int dnIndex,
            Map<String, List<String>> tbMap) {
        List<String> tTable = tbMap.get(dnIndex);
        String sqlTmp = oSql;
        if (tTable != null && !tTable.isEmpty()) {
            for (int i = 0; i < tTable.size(); i++) {
                String tbPrefix = tTable.get(i);
                for (String tbName : tbNameSet) {
                    String rTb = RealTableCache.getRealByUp(tbName);
                    TableConfig tc = schema.getTables().get(tbName);
                    if (tc != null && tc.getRule().isTbShard()) {
                        sqlTmp = replaceSqlTb(sqlTmp, rTb, rTb + tbPrefix);
                    }
                }
            }
            return sqlTmp;
        } else {
            return oSql;
        }

    }

    private static String replaceSqlTb(String sql, String tb, String logic) {
        Matcher m = Pattern.compile(tb).matcher(sql);
        StringBuffer sb = new StringBuffer();
        int len = sql.length();
        while (m.find()) {
            int sIdx = m.start();
            int eIdx = m.end();
            if (eIdx >= len || sIdx <= 0) {
                m.appendReplacement(sb, logic);
            } else {
                String s = sql.substring(eIdx, eIdx + 1);
                String s1 = sql.substring(sIdx - 1, sIdx);
                if ((s.equals(",") || s.equals(".") || s.equals(" ") || s.equals("\t") || s.equals("\r")
                        || s.equals("\n") || s.equals("(") || s.equals("`"))
                        && (s1.equals(",") || s1.equals(".") || s1.equals(" ") || s1.equals("\t") || s1.equals("\r")
                                || s1.equals("\n"))
                        || s1.equals("`")) {
                    m.appendReplacement(sb, logic);
                }
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static void main(String args[]) throws SQLSyntaxErrorException {
        SQLStatement ss = SQLParserDelegate.parse(
                "select t.name from (select * from test ) as t left join test on t.id=test.id where t.id in ('1',2,3,4,5,6)");
        PartitionKeyVisitor visitor = new PartitionKeyVisitor(null);
        ss.accept(visitor);

    }
}
