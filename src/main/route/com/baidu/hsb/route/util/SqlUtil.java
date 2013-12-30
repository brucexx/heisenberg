/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        List<String> prefixSet = tbMap.get(dnIndex);
        if (prefixSet != null && !prefixSet.isEmpty() && StringUtil.isNotEmpty(oSql)) {

            List<String> sqlList = new ArrayList<String>();
            for (int i = 0; i < prefixSet.size(); i++) {
                String tbPrefix = prefixSet.get(i);
                for (String tbName : tbNameSet) {
                    String rTb = RealTableCache.getRealByUp(tbName);
                    TableConfig tc = schema.getTables().get(tbName);
                    if (tc != null && tc.getRule().isTbShard()) {
                        sqlList.add(oSql.replaceAll(rTb, rTb + tbPrefix));
                    }
                }

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
        for (String tbName : tbSet) {
            String rTb = RealTableCache.getRealByUp(tbName);
            TableConfig scanTc = schema.getTables().get(tbName);
            if (scanTc != null && scanTc.getRule().isTbShard()) {
                for (String tbPrefix : tbPrefixSet) {
                    if (scanTc.getRule().getTbIndexMap().get(tbPrefix) == dnIdx) {
                        sqlStrs.add(oSql.replaceAll(rTb, rTb + tbPrefix.toUpperCase()));
                    }
                }
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
                        sqlTmp = sqlTmp.replaceAll(rTb, rTb + tbPrefix);
                    }
                }
            }
            return sqlTmp;
        } else {
            return oSql;
        }

    }
}
