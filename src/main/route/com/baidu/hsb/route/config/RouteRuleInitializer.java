/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.route.config;


/**
 * @author xiongzhao@baidu.com
 */
public class RouteRuleInitializer {
//    public static void initRouteRule(SchemaLoader loader) throws SQLSyntaxErrorException {
//        Map<String, RuleAlgorithm> functions = loader.getFunctions();
//        MySQLFunctionManager functionManager = new MySQLFunctionManager(true);
//        buildFuncManager(functionManager, functions);
//        for (RuleConfig conf : loader.listRuleConfig()) {
//            String algorithmString = conf.getAlgorithm();
//            MySQLLexer lexer = new MySQLLexer(algorithmString);
//            MySQLExprParser parser = new MySQLExprParser(lexer, functionManager, false, MySQLParser.DEFAULT_CHARSET);
//            Expression expression = parser.expression();
//            if (lexer.token() != MySQLToken.EOF) {
//                throw new ConfigException("route algorithm not end with EOF: " + algorithmString);
//            }
//            RuleAlgorithm algorithm;
//            if (expression instanceof RuleAlgorithm) {
//                algorithm = (RuleAlgorithm) expression;
//            } else {
//                algorithm = new ExpressionAdapter(expression);
//            }
//            conf.setRuleAlgorithm(algorithm);
//        }
//    }

//    private static void buildFuncManager(MySQLFunctionManager functionManager, Map<String, RuleAlgorithm> functions) {
//        Map<String, FunctionExpression> extFuncPrototypeMap = new HashMap<String, FunctionExpression>(functions.size());
//        for (Entry<String, RuleAlgorithm> en : functions.entrySet()) {
//            extFuncPrototypeMap.put(en.getKey(), (FunctionExpression) en.getValue());
//        }
//        functionManager.addExtendFunction(extFuncPrototypeMap);
//    }
}
