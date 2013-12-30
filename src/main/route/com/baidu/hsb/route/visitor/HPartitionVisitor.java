/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route.visitor;

import java.util.HashSet;
import java.util.Set;

import com.baidu.hsb.parser.ast.expression.BinaryOperatorExpression;
import com.baidu.hsb.parser.ast.expression.Expression;
import com.baidu.hsb.parser.ast.expression.PolyadicOperatorExpression;
import com.baidu.hsb.parser.ast.expression.UnaryOperatorExpression;
import com.baidu.hsb.parser.ast.expression.comparison.BetweenAndExpression;
import com.baidu.hsb.parser.ast.expression.comparison.ComparisionEqualsExpression;
import com.baidu.hsb.parser.ast.expression.comparison.ComparisionIsExpression;
import com.baidu.hsb.parser.ast.expression.comparison.ComparisionNullSafeEqualsExpression;
import com.baidu.hsb.parser.ast.expression.comparison.InExpression;
import com.baidu.hsb.parser.ast.expression.logical.LogicalAndExpression;
import com.baidu.hsb.parser.ast.expression.logical.LogicalOrExpression;
import com.baidu.hsb.parser.ast.expression.misc.InExpressionList;
import com.baidu.hsb.parser.ast.expression.misc.UserExpression;
import com.baidu.hsb.parser.ast.expression.primary.CaseWhenOperatorExpression;
import com.baidu.hsb.parser.ast.expression.primary.DefaultValue;
import com.baidu.hsb.parser.ast.expression.primary.ExistsPrimary;
import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.expression.primary.MatchExpression;
import com.baidu.hsb.parser.ast.expression.primary.ParamMarker;
import com.baidu.hsb.parser.ast.expression.primary.PlaceHolder;
import com.baidu.hsb.parser.ast.expression.primary.RowExpression;
import com.baidu.hsb.parser.ast.expression.primary.SysVarPrimary;
import com.baidu.hsb.parser.ast.expression.primary.UsrDefVarPrimary;
import com.baidu.hsb.parser.ast.expression.primary.Wildcard;
import com.baidu.hsb.parser.ast.expression.primary.function.FunctionExpression;
import com.baidu.hsb.parser.ast.expression.primary.function.cast.Cast;
import com.baidu.hsb.parser.ast.expression.primary.function.cast.Convert;
import com.baidu.hsb.parser.ast.expression.primary.function.datetime.Extract;
import com.baidu.hsb.parser.ast.expression.primary.function.datetime.GetFormat;
import com.baidu.hsb.parser.ast.expression.primary.function.datetime.Timestampadd;
import com.baidu.hsb.parser.ast.expression.primary.function.datetime.Timestampdiff;
import com.baidu.hsb.parser.ast.expression.primary.function.groupby.Avg;
import com.baidu.hsb.parser.ast.expression.primary.function.groupby.Count;
import com.baidu.hsb.parser.ast.expression.primary.function.groupby.GroupConcat;
import com.baidu.hsb.parser.ast.expression.primary.function.groupby.Max;
import com.baidu.hsb.parser.ast.expression.primary.function.groupby.Min;
import com.baidu.hsb.parser.ast.expression.primary.function.groupby.Sum;
import com.baidu.hsb.parser.ast.expression.primary.function.string.Char;
import com.baidu.hsb.parser.ast.expression.primary.function.string.Trim;
import com.baidu.hsb.parser.ast.expression.primary.literal.IntervalPrimary;
import com.baidu.hsb.parser.ast.expression.primary.literal.LiteralBitField;
import com.baidu.hsb.parser.ast.expression.primary.literal.LiteralBoolean;
import com.baidu.hsb.parser.ast.expression.primary.literal.LiteralHexadecimal;
import com.baidu.hsb.parser.ast.expression.primary.literal.LiteralNull;
import com.baidu.hsb.parser.ast.expression.primary.literal.LiteralNumber;
import com.baidu.hsb.parser.ast.expression.primary.literal.LiteralString;
import com.baidu.hsb.parser.ast.expression.string.LikeExpression;
import com.baidu.hsb.parser.ast.expression.type.CollateExpression;
import com.baidu.hsb.parser.ast.fragment.GroupBy;
import com.baidu.hsb.parser.ast.fragment.Limit;
import com.baidu.hsb.parser.ast.fragment.OrderBy;
import com.baidu.hsb.parser.ast.fragment.ddl.ColumnDefinition;
import com.baidu.hsb.parser.ast.fragment.ddl.TableOptions;
import com.baidu.hsb.parser.ast.fragment.ddl.datatype.DataType;
import com.baidu.hsb.parser.ast.fragment.ddl.index.IndexColumnName;
import com.baidu.hsb.parser.ast.fragment.ddl.index.IndexOption;
import com.baidu.hsb.parser.ast.fragment.tableref.Dual;
import com.baidu.hsb.parser.ast.fragment.tableref.IndexHint;
import com.baidu.hsb.parser.ast.fragment.tableref.InnerJoin;
import com.baidu.hsb.parser.ast.fragment.tableref.NaturalJoin;
import com.baidu.hsb.parser.ast.fragment.tableref.OuterJoin;
import com.baidu.hsb.parser.ast.fragment.tableref.StraightJoin;
import com.baidu.hsb.parser.ast.fragment.tableref.SubqueryFactor;
import com.baidu.hsb.parser.ast.fragment.tableref.TableRefFactor;
import com.baidu.hsb.parser.ast.fragment.tableref.TableReferences;
import com.baidu.hsb.parser.ast.stmt.dal.DALSetCharacterSetStatement;
import com.baidu.hsb.parser.ast.stmt.dal.DALSetNamesStatement;
import com.baidu.hsb.parser.ast.stmt.dal.DALSetStatement;
import com.baidu.hsb.parser.ast.stmt.dal.ShowAuthors;
import com.baidu.hsb.parser.ast.stmt.dal.ShowBinLogEvent;
import com.baidu.hsb.parser.ast.stmt.dal.ShowBinaryLog;
import com.baidu.hsb.parser.ast.stmt.dal.ShowCharaterSet;
import com.baidu.hsb.parser.ast.stmt.dal.ShowCollation;
import com.baidu.hsb.parser.ast.stmt.dal.ShowColumns;
import com.baidu.hsb.parser.ast.stmt.dal.ShowContributors;
import com.baidu.hsb.parser.ast.stmt.dal.ShowCreate;
import com.baidu.hsb.parser.ast.stmt.dal.ShowDatabases;
import com.baidu.hsb.parser.ast.stmt.dal.ShowEngine;
import com.baidu.hsb.parser.ast.stmt.dal.ShowEngines;
import com.baidu.hsb.parser.ast.stmt.dal.ShowErrors;
import com.baidu.hsb.parser.ast.stmt.dal.ShowEvents;
import com.baidu.hsb.parser.ast.stmt.dal.ShowFunctionCode;
import com.baidu.hsb.parser.ast.stmt.dal.ShowFunctionStatus;
import com.baidu.hsb.parser.ast.stmt.dal.ShowGrants;
import com.baidu.hsb.parser.ast.stmt.dal.ShowIndex;
import com.baidu.hsb.parser.ast.stmt.dal.ShowMasterStatus;
import com.baidu.hsb.parser.ast.stmt.dal.ShowOpenTables;
import com.baidu.hsb.parser.ast.stmt.dal.ShowPlugins;
import com.baidu.hsb.parser.ast.stmt.dal.ShowPrivileges;
import com.baidu.hsb.parser.ast.stmt.dal.ShowProcedureCode;
import com.baidu.hsb.parser.ast.stmt.dal.ShowProcedureStatus;
import com.baidu.hsb.parser.ast.stmt.dal.ShowProcesslist;
import com.baidu.hsb.parser.ast.stmt.dal.ShowProfile;
import com.baidu.hsb.parser.ast.stmt.dal.ShowProfiles;
import com.baidu.hsb.parser.ast.stmt.dal.ShowSlaveHosts;
import com.baidu.hsb.parser.ast.stmt.dal.ShowSlaveStatus;
import com.baidu.hsb.parser.ast.stmt.dal.ShowStatus;
import com.baidu.hsb.parser.ast.stmt.dal.ShowTableStatus;
import com.baidu.hsb.parser.ast.stmt.dal.ShowTables;
import com.baidu.hsb.parser.ast.stmt.dal.ShowTriggers;
import com.baidu.hsb.parser.ast.stmt.dal.ShowVariables;
import com.baidu.hsb.parser.ast.stmt.dal.ShowWarnings;
import com.baidu.hsb.parser.ast.stmt.ddl.DDLAlterTableStatement;
import com.baidu.hsb.parser.ast.stmt.ddl.DDLCreateIndexStatement;
import com.baidu.hsb.parser.ast.stmt.ddl.DDLCreateTableStatement;
import com.baidu.hsb.parser.ast.stmt.ddl.DDLDropIndexStatement;
import com.baidu.hsb.parser.ast.stmt.ddl.DDLDropTableStatement;
import com.baidu.hsb.parser.ast.stmt.ddl.DDLRenameTableStatement;
import com.baidu.hsb.parser.ast.stmt.ddl.DDLTruncateStatement;
import com.baidu.hsb.parser.ast.stmt.ddl.DescTableStatement;
import com.baidu.hsb.parser.ast.stmt.ddl.DDLAlterTableStatement.AlterSpecification;
import com.baidu.hsb.parser.ast.stmt.dml.DMLCallStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLDeleteStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLInsertStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLReplaceStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLSelectStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLSelectUnionStatement;
import com.baidu.hsb.parser.ast.stmt.dml.DMLUpdateStatement;
import com.baidu.hsb.parser.ast.stmt.extension.ExtDDLCreatePolicy;
import com.baidu.hsb.parser.ast.stmt.extension.ExtDDLDropPolicy;
import com.baidu.hsb.parser.ast.stmt.mts.MTSReleaseStatement;
import com.baidu.hsb.parser.ast.stmt.mts.MTSRollbackStatement;
import com.baidu.hsb.parser.ast.stmt.mts.MTSSavepointStatement;
import com.baidu.hsb.parser.ast.stmt.mts.MTSSetTransactionStatement;
import com.baidu.hsb.parser.visitor.SQLASTVisitor;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: HPartitionVisitor.java, v 0.1 2013年12月23日 下午2:08:18 HI:brucest0078 Exp $
 */
public class HPartitionVisitor implements SQLASTVisitor {

    private static final Set<Class<? extends Expression>> VERDICT_PASS_THROUGH_WHERE     = new HashSet<Class<? extends Expression>>(
                                                                                             6);
    private static final Set<Class<? extends Expression>> GROUP_FUNC_PASS_THROUGH_SELECT = new HashSet<Class<? extends Expression>>(
                                                                                             5);
    private static final Set<Class<? extends Expression>> PARTITION_OPERAND_SINGLE       = new HashSet<Class<? extends Expression>>(
                                                                                             3);
    static {
        VERDICT_PASS_THROUGH_WHERE.add(LogicalAndExpression.class);
        VERDICT_PASS_THROUGH_WHERE.add(LogicalOrExpression.class);
        VERDICT_PASS_THROUGH_WHERE.add(BetweenAndExpression.class);
        VERDICT_PASS_THROUGH_WHERE.add(InExpression.class);
        VERDICT_PASS_THROUGH_WHERE.add(ComparisionNullSafeEqualsExpression.class);
        VERDICT_PASS_THROUGH_WHERE.add(ComparisionEqualsExpression.class);
        GROUP_FUNC_PASS_THROUGH_SELECT.add(Count.class);
        GROUP_FUNC_PASS_THROUGH_SELECT.add(Sum.class);
        GROUP_FUNC_PASS_THROUGH_SELECT.add(Min.class);
        GROUP_FUNC_PASS_THROUGH_SELECT.add(Max.class);
        GROUP_FUNC_PASS_THROUGH_SELECT.add(Wildcard.class);
        PARTITION_OPERAND_SINGLE.add(BetweenAndExpression.class);
        PARTITION_OPERAND_SINGLE.add(ComparisionNullSafeEqualsExpression.class);
        PARTITION_OPERAND_SINGLE.add(ComparisionEqualsExpression.class);
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.comparison.BetweenAndExpression)
     */
    @Override
    public void visit(BetweenAndExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.comparison.ComparisionIsExpression)
     */
    @Override
    public void visit(ComparisionIsExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.misc.InExpressionList)
     */
    @Override
    public void visit(InExpressionList node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.string.LikeExpression)
     */
    @Override
    public void visit(LikeExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.type.CollateExpression)
     */
    @Override
    public void visit(CollateExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.misc.UserExpression)
     */
    @Override
    public void visit(UserExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.UnaryOperatorExpression)
     */
    @Override
    public void visit(UnaryOperatorExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.BinaryOperatorExpression)
     */
    @Override
    public void visit(BinaryOperatorExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.PolyadicOperatorExpression)
     */
    @Override
    public void visit(PolyadicOperatorExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.logical.LogicalAndExpression)
     */
    @Override
    public void visit(LogicalAndExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.logical.LogicalOrExpression)
     */
    @Override
    public void visit(LogicalOrExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.comparison.ComparisionEqualsExpression)
     */
    @Override
    public void visit(ComparisionEqualsExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.comparison.ComparisionNullSafeEqualsExpression)
     */
    @Override
    public void visit(ComparisionNullSafeEqualsExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.comparison.InExpression)
     */
    @Override
    public void visit(InExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.FunctionExpression)
     */
    @Override
    public void visit(FunctionExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.string.Char)
     */
    @Override
    public void visit(Char node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.cast.Convert)
     */
    @Override
    public void visit(Convert node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.string.Trim)
     */
    @Override
    public void visit(Trim node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.cast.Cast)
     */
    @Override
    public void visit(Cast node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.groupby.Avg)
     */
    @Override
    public void visit(Avg node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.groupby.Max)
     */
    @Override
    public void visit(Max node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.groupby.Min)
     */
    @Override
    public void visit(Min node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.groupby.Sum)
     */
    @Override
    public void visit(Sum node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.groupby.Count)
     */
    @Override
    public void visit(Count node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.groupby.GroupConcat)
     */
    @Override
    public void visit(GroupConcat node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.datetime.Extract)
     */
    @Override
    public void visit(Extract node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.datetime.Timestampdiff)
     */
    @Override
    public void visit(Timestampdiff node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.datetime.Timestampadd)
     */
    @Override
    public void visit(Timestampadd node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.function.datetime.GetFormat)
     */
    @Override
    public void visit(GetFormat node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.literal.IntervalPrimary)
     */
    @Override
    public void visit(IntervalPrimary node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.literal.LiteralBitField)
     */
    @Override
    public void visit(LiteralBitField node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.literal.LiteralBoolean)
     */
    @Override
    public void visit(LiteralBoolean node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.literal.LiteralHexadecimal)
     */
    @Override
    public void visit(LiteralHexadecimal node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.literal.LiteralNull)
     */
    @Override
    public void visit(LiteralNull node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.literal.LiteralNumber)
     */
    @Override
    public void visit(LiteralNumber node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.literal.LiteralString)
     */
    @Override
    public void visit(LiteralString node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.CaseWhenOperatorExpression)
     */
    @Override
    public void visit(CaseWhenOperatorExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.DefaultValue)
     */
    @Override
    public void visit(DefaultValue node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.ExistsPrimary)
     */
    @Override
    public void visit(ExistsPrimary node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.PlaceHolder)
     */
    @Override
    public void visit(PlaceHolder node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.Identifier)
     */
    @Override
    public void visit(Identifier node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.MatchExpression)
     */
    @Override
    public void visit(MatchExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.ParamMarker)
     */
    @Override
    public void visit(ParamMarker node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.RowExpression)
     */
    @Override
    public void visit(RowExpression node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.SysVarPrimary)
     */
    @Override
    public void visit(SysVarPrimary node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.expression.primary.UsrDefVarPrimary)
     */
    @Override
    public void visit(UsrDefVarPrimary node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.tableref.IndexHint)
     */
    @Override
    public void visit(IndexHint node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.tableref.InnerJoin)
     */
    @Override
    public void visit(InnerJoin node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.tableref.NaturalJoin)
     */
    @Override
    public void visit(NaturalJoin node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.tableref.OuterJoin)
     */
    @Override
    public void visit(OuterJoin node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.tableref.StraightJoin)
     */
    @Override
    public void visit(StraightJoin node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.tableref.SubqueryFactor)
     */
    @Override
    public void visit(SubqueryFactor node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.tableref.TableReferences)
     */
    @Override
    public void visit(TableReferences node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.tableref.TableRefFactor)
     */
    @Override
    public void visit(TableRefFactor node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.tableref.Dual)
     */
    @Override
    public void visit(Dual dual) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.GroupBy)
     */
    @Override
    public void visit(GroupBy node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.Limit)
     */
    @Override
    public void visit(Limit node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.OrderBy)
     */
    @Override
    public void visit(OrderBy node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.ddl.ColumnDefinition)
     */
    @Override
    public void visit(ColumnDefinition node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.ddl.index.IndexOption)
     */
    @Override
    public void visit(IndexOption node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.ddl.index.IndexColumnName)
     */
    @Override
    public void visit(IndexColumnName node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.ddl.TableOptions)
     */
    @Override
    public void visit(TableOptions node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.ddl.DDLAlterTableStatement.AlterSpecification)
     */
    @Override
    public void visit(AlterSpecification node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.fragment.ddl.datatype.DataType)
     */
    @Override
    public void visit(DataType node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowAuthors)
     */
    @Override
    public void visit(ShowAuthors node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowBinaryLog)
     */
    @Override
    public void visit(ShowBinaryLog node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowBinLogEvent)
     */
    @Override
    public void visit(ShowBinLogEvent node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowCharaterSet)
     */
    @Override
    public void visit(ShowCharaterSet node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowCollation)
     */
    @Override
    public void visit(ShowCollation node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowColumns)
     */
    @Override
    public void visit(ShowColumns node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowContributors)
     */
    @Override
    public void visit(ShowContributors node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowCreate)
     */
    @Override
    public void visit(ShowCreate node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowDatabases)
     */
    @Override
    public void visit(ShowDatabases node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowEngine)
     */
    @Override
    public void visit(ShowEngine node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowEngines)
     */
    @Override
    public void visit(ShowEngines node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowErrors)
     */
    @Override
    public void visit(ShowErrors node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowEvents)
     */
    @Override
    public void visit(ShowEvents node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowFunctionCode)
     */
    @Override
    public void visit(ShowFunctionCode node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowFunctionStatus)
     */
    @Override
    public void visit(ShowFunctionStatus node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowGrants)
     */
    @Override
    public void visit(ShowGrants node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowIndex)
     */
    @Override
    public void visit(ShowIndex node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowMasterStatus)
     */
    @Override
    public void visit(ShowMasterStatus node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowOpenTables)
     */
    @Override
    public void visit(ShowOpenTables node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowPlugins)
     */
    @Override
    public void visit(ShowPlugins node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowPrivileges)
     */
    @Override
    public void visit(ShowPrivileges node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowProcedureCode)
     */
    @Override
    public void visit(ShowProcedureCode node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowProcedureStatus)
     */
    @Override
    public void visit(ShowProcedureStatus node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowProcesslist)
     */
    @Override
    public void visit(ShowProcesslist node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowProfile)
     */
    @Override
    public void visit(ShowProfile node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowProfiles)
     */
    @Override
    public void visit(ShowProfiles node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowSlaveHosts)
     */
    @Override
    public void visit(ShowSlaveHosts node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowSlaveStatus)
     */
    @Override
    public void visit(ShowSlaveStatus node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowStatus)
     */
    @Override
    public void visit(ShowStatus node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowTables)
     */
    @Override
    public void visit(ShowTables node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowTableStatus)
     */
    @Override
    public void visit(ShowTableStatus node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowTriggers)
     */
    @Override
    public void visit(ShowTriggers node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowVariables)
     */
    @Override
    public void visit(ShowVariables node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.ShowWarnings)
     */
    @Override
    public void visit(ShowWarnings node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.ddl.DescTableStatement)
     */
    @Override
    public void visit(DescTableStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.DALSetStatement)
     */
    @Override
    public void visit(DALSetStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.DALSetNamesStatement)
     */
    @Override
    public void visit(DALSetNamesStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dal.DALSetCharacterSetStatement)
     */
    @Override
    public void visit(DALSetCharacterSetStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dml.DMLCallStatement)
     */
    @Override
    public void visit(DMLCallStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dml.DMLDeleteStatement)
     */
    @Override
    public void visit(DMLDeleteStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dml.DMLInsertStatement)
     */
    @Override
    public void visit(DMLInsertStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dml.DMLReplaceStatement)
     */
    @Override
    public void visit(DMLReplaceStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dml.DMLSelectStatement)
     */
    @Override
    public void visit(DMLSelectStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dml.DMLSelectUnionStatement)
     */
    @Override
    public void visit(DMLSelectUnionStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.dml.DMLUpdateStatement)
     */
    @Override
    public void visit(DMLUpdateStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.mts.MTSSetTransactionStatement)
     */
    @Override
    public void visit(MTSSetTransactionStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.mts.MTSSavepointStatement)
     */
    @Override
    public void visit(MTSSavepointStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.mts.MTSReleaseStatement)
     */
    @Override
    public void visit(MTSReleaseStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.mts.MTSRollbackStatement)
     */
    @Override
    public void visit(MTSRollbackStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.ddl.DDLTruncateStatement)
     */
    @Override
    public void visit(DDLTruncateStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.ddl.DDLAlterTableStatement)
     */
    @Override
    public void visit(DDLAlterTableStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.ddl.DDLCreateIndexStatement)
     */
    @Override
    public void visit(DDLCreateIndexStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.ddl.DDLCreateTableStatement)
     */
    @Override
    public void visit(DDLCreateTableStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.ddl.DDLRenameTableStatement)
     */
    @Override
    public void visit(DDLRenameTableStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.ddl.DDLDropIndexStatement)
     */
    @Override
    public void visit(DDLDropIndexStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.ddl.DDLDropTableStatement)
     */
    @Override
    public void visit(DDLDropTableStatement node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.extension.ExtDDLCreatePolicy)
     */
    @Override
    public void visit(ExtDDLCreatePolicy node) {
    }

    /** 
     * @see com.baidu.hsb.parser.visitor.SQLASTVisitor#visit(com.baidu.hsb.parser.ast.stmt.extension.ExtDDLDropPolicy)
     */
    @Override
    public void visit(ExtDDLDropPolicy node) {
    }

}
