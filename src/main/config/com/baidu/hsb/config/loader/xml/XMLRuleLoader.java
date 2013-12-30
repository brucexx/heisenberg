/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * (created at 2012-6-13)
 */
package com.baidu.hsb.config.loader.xml;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.baidu.hsb.config.model.config.RuleConfig;
import com.baidu.hsb.config.model.config.TableRuleConfig;
import com.baidu.hsb.config.util.ConfigException;
import com.baidu.hsb.config.util.ConfigUtil;
import com.baidu.hsb.util.SplitUtil;

/**
 * @author xiongzhao@baidu.com
 */
@SuppressWarnings("unchecked")
public class XMLRuleLoader {
    //private final static String                DEFAULT_DTD = "/rule.dtd";
    private final static String                DEFAULT_XML = "/rule.xml";

    private final Map<String, TableRuleConfig> tableRules;
    private final Set<RuleConfig>              rules;

    public XMLRuleLoader(String ruleFile) {
        this.rules = new HashSet<RuleConfig>();
        this.tableRules = new HashMap<String, TableRuleConfig>();
        load(ruleFile == null ? DEFAULT_XML : ruleFile);
    }

    public XMLRuleLoader() {
        this(null);
    }

    public Map<String, TableRuleConfig> getTableRules() {
        return (Map<String, TableRuleConfig>) (tableRules.isEmpty() ? Collections.emptyMap()
            : tableRules);
    }

    public Set<RuleConfig> listRuleConfig() {
        return (Set<RuleConfig>) ((rules == null || rules.isEmpty()) ? Collections.emptySet()
            : rules);
    }

    private void load(String xmlFile) {

        InputStream xml = null;
        try {

            xml = XMLRuleLoader.class.getResourceAsStream(xmlFile);
            Element root = ConfigUtil.getDocument(xml).getDocumentElement();
            // loadFunctions(root);
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

                tableRules.put(name, new TableRuleConfig(name, colsStr.split(",", -1),
                    dbRuleStrList, tbRuleStrList, tbPrefixStr));
            }
        }
    }

}
