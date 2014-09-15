/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baidu.hsb.config.model.RealTableCache;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.config.model.config.TableConfig;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: SqlUtil.java, v 0.1 2013年12月23日 下午9:42:58 HI:brucest0078 Exp $
 */
public class SqlUtil {

    /**
     * 
     * 
     * @param oSql
     * @param tbName
     * @param dnIndex
     * @param tbMap
     * @return
     */
    public static String[] scan(SchemaConfig schema, String oSql, Set<String> tbNameSet,
                                int dnIndex, Map<Integer, List<String>> tbMap) {

        if (tbMap == null) {
            return new String[] { oSql };
        }

        List<String> prefixSet = tbMap.get(dnIndex);
        if (prefixSet != null && !prefixSet.isEmpty() && StringUtil.isNotEmpty(oSql)) {

            List<String> sqlList = new ArrayList<String>();
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
            return sqlList.toArray(new String[sqlList.size()]);
        } else {
            return new String[] { oSql };
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
    public static String[] renderTB(SchemaConfig schema, String oSql,
                                    Map<String, List<Object>> columnValues, TableConfig tc,
                                    Set<String> tbSet, int dnIdx) {

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
            //如果没一个替换的
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
    public static String replaceSql(SchemaConfig schema, String oSql, Set<String> tbNameSet,
                                    int dnIndex, Map<String, List<String>> tbMap) {
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
                if ((s.equals(",") || s.equals(".") || s.equals(" ") || s.equals("\t")
                     || s.equals("\r") || s.equals("\n") || s.equals("("))
                    && (s1.equals(",") || s1.equals(".") || s1.equals(" ") || s1.equals("\t")
                        || s1.equals("\r") || s1.equals("\n"))) {
                    m.appendReplacement(sb, logic);
                }
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static void main(String args[]) {
        //        String sql = "select * from \r\ntrans_tb";
        //        System.out.println(replaceSqlTb(sql, "trans_tb", "trans_tb1"));
        //        sql = "select * from trans_tb,trans_tb_ext";
        //        System.out.println(replaceSqlTb(sql, "trans_tb", "trans_tb1"));
        //        sql = "select * from trans_tb.";
        //        System.out.println(replaceSqlTb(sql, "trans_tb", "trans_tb1"));
        //        sql = "select * from trans_tb ";
        //        System.out.println(replaceSqlTb(sql, "trans_tb", "trans_tb1"));
        //        sql = "select * from trans_tb\r\n";
        //        //System.out.println(sql + "|");
        //        System.out.println(replaceSqlTb(sql, "trans_tb", "trans_tb1") + "-->");
        System.out.println(replaceSqlTb("insert into t_crowdfunding_trans(id) values('22')",
            "t_crowdfunding_trans", "t_crowdfunding_trans_55_6"));

    }
}
