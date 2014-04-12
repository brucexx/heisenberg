/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.config.model.config.TableConfig;
import com.baidu.hsb.config.model.config.TableRuleConfig;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.stmt.SQLStatement;
import com.baidu.hsb.parser.ast.stmt.dal.DALShowStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLQueryStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLSelectStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLSelectUnionStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLUpdateStatement;
import com.baidu.hsb.parser.recognizer.SQLParserDelegate;
import com.baidu.hsb.parser.recognizer.mysql.syntax.MySQLParser;
import com.baidu.hsb.parser.util.Pair;
import com.baidu.hsb.parser.visitor.MySQLOutputASTVisitor;
import com.baidu.hsb.route.context.HeisenbergContext;
import com.baidu.hsb.route.router.MetaRouter;
import com.baidu.hsb.route.util.SqlUtil;
import com.baidu.hsb.route.util.StringUtil;
import com.baidu.hsb.route.util.VelocityUtil;
import com.baidu.hsb.route.util.WeightHelper;
import com.baidu.hsb.route.visitor.PartitionKeyVisitor;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: MyServerRouter.java, v 0.1 2013年12月22日 下午1:01:32 HI:brucest0078 Exp $
 */
public class HServerRouter {

    private static int getReadIdx(boolean isRead, String dataNode) {

        if (!isRead) {
            return -1;
        }
        MySQLDataNode dn = HeisenbergContext.getMysqlDataNode().get(dataNode);
        if (StringUtil.isEmpty(dataNode) || dn == null) {
            throw new IllegalArgumentException(
                "schema don't have dataNode attribute or dataNode is null");
        }
        if (!dn.getConfig().isNeedWR()) {
            return -1;
        }

        return WeightHelper.getReadIndex(dn);

    }

    public static RouteResultset route(SchemaConfig schema, String stmt, String charset, Object info)
                                                                                                     throws SQLNonTransientException {
        boolean isRead = false;

        RouteResultset rrs = new RouteResultset(stmt);
        //无分库表
        if (schema.isNoSharding()) {
            if (schema.isKeepSqlSchema()) {
                SQLStatement ast = SQLParserDelegate.parse(stmt,
                    charset == null ? MySQLParser.DEFAULT_CHARSET : charset);
                PartitionKeyVisitor visitor = new PartitionKeyVisitor(schema.getTables());
                visitor.setTrimSchema(schema.getName());
                if (ast instanceof DMLQueryStatement) {
                    isRead = true;
                }
                ast.accept(visitor);
                if (visitor.isSchemaTrimmed()) {
                    stmt = genSQL(ast, stmt);
                }
            }

            RouteResultsetNode[] nodes = new RouteResultsetNode[1];
            nodes[0] = new RouteResultsetNode(schema.getDataNode(), getReadIdx(isRead,
                schema.getDataNode()), new String[] { stmt });
            rrs.setNodes(nodes);
            return rrs;
        }
        SQLStatement ast = SQLParserDelegate.parse(stmt,
            charset == null ? MySQLParser.DEFAULT_CHARSET : charset);
        PartitionKeyVisitor visitor = new PartitionKeyVisitor(schema.getTables());
        visitor.setTrimSchema(schema.isKeepSqlSchema() ? schema.getName() : null);
        ast.accept(visitor);
        if (ast instanceof DMLQueryStatement) {
            isRead = true;
        }

        // 如果sql包含用户自定义的schema，则路由到default节点
        if (schema.isKeepSqlSchema() && visitor.isCustomedSchema()) {
            if (visitor.isSchemaTrimmed()) {
                stmt = genSQL(ast, stmt);
            }
            RouteResultsetNode[] nodes = new RouteResultsetNode[1];
            nodes[0] = new RouteResultsetNode(schema.getDataNode(), getReadIdx(isRead,
                schema.getDataNode()), new String[] { stmt });
            rrs.setNodes(nodes);
            return rrs;
        }

        // 元数据语句路由
        if (visitor.isTableMetaRead()) {
            MetaRouter.routeForTableMeta(rrs, schema, ast, visitor, stmt);
            if (visitor.isNeedRewriteField()) {
                rrs.setFlag(RouteResultset.REWRITE_FIELD);
            }
            return rrs;
        }
        // 匹配规则
        TableConfig matchedTable = null;
        // RuleConfig rule = null;
        Map<String, List<Object>> columnValues = null;
        boolean colShard = false;
        Map<String, Map<String, List<Object>>> astExt = visitor.getColumnValue();
        Map<String, TableConfig> tables = schema.getTables();
        boolean isForceHit = false;
        ft: for (Entry<String, Map<String, List<Object>>> e : astExt.entrySet()) {
            Map<String, List<Object>> col2Val = e.getValue();
            TableConfig tc = tables.get(e.getKey());
            if (tc == null) {
                continue;
            }
            if (matchedTable == null) {
                matchedTable = tc;
            }
            if (col2Val == null || col2Val.isEmpty()) {
                continue;
            }
            TableRuleConfig tr = tc.getRule();
            if (tr != null) {
                isForceHit = tr.isForceHit();
                boolean match = true;
                for (String ruleColumn : tr.getColumns()) {
                    match &= col2Val.containsKey(ruleColumn);
                }
                if (match) {
                    colShard = true;
                    columnValues = col2Val;
                    matchedTable = tc;
                    break ft;
                } else if (isForceHit) {
                    columnValues = col2Val;
                }
            }
        }

        // 规则匹配处理，表级别和列级别。
        if (matchedTable == null) {
            return matchTable(rrs, schema, visitor, ast, stmt);
        }

        //无命中，全扫描 
        if (!colShard) {
            //强制命中
            if (isForceHit) {
                //防止变更分区列
                validateAST(ast, matchedTable, visitor);

                shard(isRead, schema, rrs, matchedTable, visitor, columnValues, stmt,
                    astExt.keySet());
                return rrs;
            }
            //
            return colsShared(isRead, schema, astExt.keySet(), rrs, matchedTable, visitor, ast,
                stmt);
        }
        //防止变更分区列
        validateAST(ast, matchedTable, visitor);

        shard(isRead, schema, rrs, matchedTable, visitor, columnValues, stmt, astExt.keySet());

        return rrs;
    }

    /**
     * 
     * 
     * @param rrs
     * @param matchedTable
     * @param columnValues
     */
    private static void shard(boolean isRead, SchemaConfig schema, RouteResultset rrs,
                              TableConfig tc, PartitionKeyVisitor visitor,
                              Map<String, List<Object>> columnValues, String stmt, Set<String> tbSet) {
        Integer[] dnIndexs = cacDataNodes(tc, columnValues);
        if (dnIndexs.length == 0) {
            throw new IllegalArgumentException("error!no partion value!");
        }
        RouteResultsetNode[] rrn = new RouteResultsetNode[dnIndexs.length];
        for (int i = 0; i < dnIndexs.length; i++) {
            int idx = dnIndexs[i];
            String dataNode = tc.getDataNodes()[idx];
            rrn[i] = new RouteResultsetNode(dataNode, getReadIdx(isRead, dataNode),
                SqlUtil.renderTB(schema, stmt, columnValues, tc, tbSet, idx));
        }
        rrs.setNodes(rrn);
        if (dnIndexs.length > 1) {
            setGroupFlagAndLimit(rrs, visitor);
        }
    }

    /**
     * 
     * 
     * @param tc
     * @param parameter
     * @return
     */
    private static Integer[] cacDataNodes(TableConfig tc, Map<String, List<Object>> columnValues) {
        Set<Integer> renderNode = VelocityUtil.evalDBRuleArray(tc.getRule(), columnValues);
        if (renderNode.size() == 0) {
            return new Integer[0];
        } else {
            return renderNode.toArray(new Integer[renderNode.size()]);
        }
    }

    /**
     * 
     * 
     * @param rrs
     * @param schema
     * @param visitor
     * @param ast
     * @param stmt
     * @return
     */
    private static RouteResultset matchTable(RouteResultset rrs, SchemaConfig schema,
                                             PartitionKeyVisitor visitor, SQLStatement ast,
                                             String stmt) {
        String sql = visitor.isSchemaTrimmed() ? genSQL(ast, stmt) : stmt;
        RouteResultsetNode[] rn = new RouteResultsetNode[1];
        if ("".equals(schema.getDataNode()) && isSystemReadSQL(ast)) {
            String rnNode = schema.getRandomDataNode();
            rn[0] = new RouteResultsetNode(rnNode, new String[] { sql });
        } else {
            rn[0] = new RouteResultsetNode(schema.getDataNode(), new String[] { sql });
        }
        rrs.setNodes(rn);
        return rrs;
    }

    /**
     * 
     * 
     * @param rrs
     * @param tc
     * @param visitor
     * @param ast
     * @param stmt
     * @return
     */
    private static RouteResultset colsShared(boolean isRead, SchemaConfig schema,
                                             Set<String> tbSet, RouteResultset rrs, TableConfig tc,
                                             PartitionKeyVisitor visitor, SQLStatement ast,
                                             String stmt) {

        String[] dataNodes = tc.getDataNodes();
        String sql = visitor.isSchemaTrimmed() ? genSQL(ast, stmt) : stmt;
        RouteResultsetNode[] rn = new RouteResultsetNode[dataNodes.length];
        for (int i = 0; i < rn.length; i++) {
            rn[i] = new RouteResultsetNode(dataNodes[i], getReadIdx(isRead, dataNodes[i]),
                SqlUtil.scan(schema, sql, tbSet, i, tc.getRule().getTbMap()));
        }
        rrs.setNodes(rn);
        setGroupFlagAndLimit(rrs, visitor);
        return rrs;
    }

    private static void setGroupFlagAndLimit(RouteResultset rrs, PartitionKeyVisitor visitor) {
        rrs.setLimitSize(visitor.getLimitSize());
        switch (visitor.getGroupFuncType()) {
            case PartitionKeyVisitor.GROUP_SUM:
                rrs.setFlag(RouteResultset.SUM_FLAG);
                break;
            case PartitionKeyVisitor.GROUP_MAX:
                rrs.setFlag(RouteResultset.MAX_FLAG);
                break;
            case PartitionKeyVisitor.GROUP_MIN:
                rrs.setFlag(RouteResultset.MIN_FLAG);
                break;
        }
    }

    private static void validateAST(SQLStatement ast, TableConfig tc, PartitionKeyVisitor visitor)
                                                                                                  throws SQLNonTransientException {
        if (ast instanceof DMLUpdateStatement) {
            List<Identifier> columns = null;
            List<String> ruleCols = tc.getRule().getColumns();
            DMLUpdateStatement update = (DMLUpdateStatement) ast;
            for (Pair<Identifier, Expression> pair : update.getValues()) {
                for (String ruleCol : ruleCols) {
                    if (StringUtil.equals(pair.getKey().getIdTextUpUnescape(), ruleCol)) {
                        if (columns == null) {
                            columns = new ArrayList<Identifier>(ruleCols.size());
                        }
                        columns.add(pair.getKey());
                    }
                }
            }
            if (columns == null) {
                return;
            }
            Map<String, String> alias = visitor.getTableAlias();
            for (Identifier column : columns) {
                String table = column.getLevelUnescapeUpName(2);
                table = alias.get(table);
                if (table != null && table.equals(tc.getNameUp())) {
                    throw new SQLFeatureNotSupportedException("partition key cannot be changed");
                }
            }
        }
    }

    private static boolean isSystemReadSQL(SQLStatement ast) {
        if (ast instanceof DALShowStatement) {
            return true;
        }
        DMLSelectStatement select = null;
        if (ast instanceof DMLSelectStatement) {
            select = (DMLSelectStatement) ast;
        } else if (ast instanceof DMLSelectUnionStatement) {
            DMLSelectUnionStatement union = (DMLSelectUnionStatement) ast;
            if (union.getSelectStmtList().size() == 1) {
                select = union.getSelectStmtList().get(0);
            } else {
                return false;
            }
        } else {
            return false;
        }
        return select.getTables() == null;
    }

    private static String genSQL(SQLStatement ast, String orginalSql) {
        StringBuilder s = new StringBuilder();
        ast.accept(new MySQLOutputASTVisitor(s));
        return s.toString();
    }

}
