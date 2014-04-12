/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.loader.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.baidu.hsb.config.model.config.TableRuleConfig;
import com.baidu.hsb.config.util.ConfigException;
import com.baidu.hsb.config.util.ConfigUtil;

/**
 * @author xiongzhao@baidu.com
 */
@SuppressWarnings("unchecked")
public class XMLRuleLoader {
    //private final static String                DEFAULT_DTD = "/rule.dtd";
    public final static String                 DEFAULT_XML = "/rule.xml";

    private final Map<String, TableRuleConfig> tableRules;

    public XMLRuleLoader(String ruleFile) {
        this.tableRules = new HashMap<String, TableRuleConfig>();
        if (ruleFile == null) {
            load(DEFAULT_XML, true);
        } else {
            load(ruleFile, false);
        }

    }

    public XMLRuleLoader() {
        this(null);
    }

    public Map<String, TableRuleConfig> getTableRules() {
        return (Map<String, TableRuleConfig>) (tableRules.isEmpty() ? Collections.emptyMap()
            : tableRules);
    }

    private void load(String xmlFile, boolean isCp) {
        InputStream xml = null;
        try {
            if (isCp) {
                xml = XMLRuleLoader.class.getResourceAsStream(xmlFile);
            } else {
                xml = new FileInputStream(xmlFile);
            }
            Element root = ConfigUtil.getDocument(xml).getDocumentElement();
            loadTableRules(root);
        } catch (ConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigException(e);
        } finally {

            if (xml != null) {
                try {
                    xml.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 
     * 
     * @param root
     * @throws SQLSyntaxErrorException
     */
    private void loadTableRules(Element root) throws SQLSyntaxErrorException {
        NodeList list = root.getElementsByTagName("tableRule");
        for (int i = 0, n = list.getLength(); i < n; ++i) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Element e = (Element) node;
                String name = e.getAttribute("name");
                String forceHit = e.getAttribute("forceHit");
                if (tableRules.containsKey(name)) {
                    throw new ConfigException("table rule " + name + " duplicated!");
                }

                NodeList cols = e.getElementsByTagName("columns");
                NodeList dbRuleList = e.getElementsByTagName("dbRuleList");
                NodeList tbRuleList = e.getElementsByTagName("tbRuleList");
                NodeList tbPrefix = e.getElementsByTagName("tbPrefix");
                String colsStr = cols.getLength() > 0 ? ((Element) cols.item(0)).getTextContent()
                    .toUpperCase() : "";
                String tbPrefixStr = tbPrefix.getLength() > 0 ? ((Element) tbPrefix.item(0))
                    .getTextContent() : "";
                List<String> dbRuleStrList = new ArrayList<String>();
                if (dbRuleList != null && dbRuleList.getLength() > 0) {
                    Node dbRuleListNode = dbRuleList.item(0);
                    NodeList dbRuleNodes = ((Element) dbRuleListNode)
                        .getElementsByTagName("dbRule");
                    for (int j = 0; j < dbRuleNodes.getLength(); j++) {
                        Node dbRuleNode = dbRuleNodes.item(j);
                        if (dbRuleNode instanceof Element) {
                            dbRuleStrList.add(((Element) dbRuleNodes.item(j)).getTextContent());
                        }
                    }
                }

                List<String> tbRuleStrList = new ArrayList<String>();
                if (tbRuleList != null && tbRuleList.getLength() > 0) {
                    Node tbRuleListNode = tbRuleList.item(0);
                    NodeList tbRuleNodeList = ((Element) tbRuleListNode)
                        .getElementsByTagName("tbRule");
                    for (int j = 0; j < tbRuleNodeList.getLength(); j++) {
                        Node tbRuleNode = tbRuleNodeList.item(j);
                        if (tbRuleNode instanceof Element) {
                            tbRuleStrList.add(((Element) tbRuleNodeList.item(j)).getTextContent());
                        }
                    }
                }

                tableRules.put(name, new TableRuleConfig(name, forceHit, colsStr.split(",", -1),
                    dbRuleStrList, tbRuleStrList, tbPrefixStr));
            }
        }
    }

}
