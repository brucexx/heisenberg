/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;

import com.baidu.hsb.config.model.config.TableRuleConfig;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: VelocityUtil.java, v 0.1 2013年12月21日 下午1:00:51 HI:brucest0078 Exp $
 */
public class VelocityUtil {
    private static final Logger LOGGER     = Logger.getLogger(VelocityUtil.class);

    private static DateUtil     dateUtil   = new DateUtil();
    private static StringUtil   stringUtil = new StringUtil();

    //    private static VelocityContext getContext(Map<String, Object> colsVal) {
    //        VelocityContext context = new VelocityContext();
    //        context.put("dateUtil", new DateUtil());
    //        context.put("stringUtil", new StringUtil());
    //        for (Map.Entry<String, Object> entry : colsVal.entrySet()) {
    //            context.put(entry.getKey(), entry.getValue());
    //        }
    //        return context;
    //    }

    private static VelocityContext getContext() {
        VelocityContext context = new VelocityContext();
        context.put("dateUtil", dateUtil);
        context.put("stringUtil", stringUtil);
        return context;
    }

    /**
     * 
     * 
     * @param tc
     * @param colsVal
     * @return
     */
    public static Set<Integer> evalDBRuleArray(TableRuleConfig tc, Map<String, List<Object>> colsVal) {
        List<String> dbArray = tc.getDbRuleArray();

        for (String dbRule : dbArray) {
            Set<Integer> idxs = new TreeSet<Integer>();
            //第一个取到即可

            try {
                String dbIndex = "";
                VelocityContext context = getContext();
                for (Map.Entry<String, List<Object>> entry : colsVal.entrySet()) {
                    List<Object> list = entry.getValue();
                    for (Object obj : list) {
                        Writer writer = new StringWriter();
                        try {
                            context.put(entry.getKey(), obj);
                            Velocity.evaluate(context, writer, StringUtil.EMPTY, dbRule);
                            dbIndex = StringUtil.trim(writer.toString());
                            if (StringUtil.isEmpty(dbIndex)) {
                                continue;
                            }
                            idxs.add(NumberUtils.toInt(dbIndex));

                        } finally {
                            IOUtils.closeQuietly(writer);
                        }
                    }
                }

            } catch (ParseErrorException e) {
                throw e;
            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(tc.getName() + "eval " + dbRule + " error..");

                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("db flag val: " + idxs);
            }
            if (!idxs.isEmpty()) {
                return idxs;
            }
        }
        return new HashSet<Integer>(0);

    }

    public static Set<String> evalTbRuleArray(TableRuleConfig tc, Map<String, List<Object>> colsVal) {
        List<String> tbArray = tc.getTbRuleArray();
        VelocityContext context = getContext();

        for (String tbRule : tbArray) {
            try {
                Set<String> tbPreSet = new HashSet<String>();
                for (Map.Entry<String, List<Object>> entry : colsVal.entrySet()) {
                    for (Object val : entry.getValue()) {
                        Writer writer = new StringWriter();
                        try {
                            context.put(entry.getKey(), val);
                            Velocity.evaluate(context, writer, StringUtil.EMPTY, tbRule);
                            String tbPre = StringUtil.trim(writer.toString());
                            if (tc.getTbIndexMap().containsKey(tbPre)) {
                                tbPreSet.add(tbPre);
                            }
                        } finally {
                            writer.close();
                        }
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("tb flag val: " + tbPreSet);
                    }

                    if (!tbPreSet.isEmpty()) {
                        return tbPreSet;
                    }
                }

            } catch (ParseErrorException e) {
                LOGGER.error(tc.getName() + ":eval " + tbRule + " error..", e);
                throw e;
            } catch (Exception e) {
                LOGGER.error(tc.getName() + ":eval " + tbRule + " error..", e);
            }
        }
        return new HashSet<String>();

    }

    public static void main(String[] args) throws IOException {

        String tpl = " #set($db_flag=$!stringUtil.crc32($F_CERTIFICATE_CODE))\r\n"
                     + "$!stringUtil.substring($db_flag,-3,-1)";
        Writer writer = new StringWriter();
        try {
            VelocityContext context = getContext();
            context.put("F_CERTIFICATE_CODE", "123123123123123");
            Velocity.evaluate(context, writer, "", tpl);
            System.out.println(writer.toString());
        } catch (ParseErrorException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }

    }
}
