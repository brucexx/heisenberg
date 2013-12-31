/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.ast.fragment.tableref;

import com.baidu.hsb.parser.ast.expression.primary.Identifier;
import com.baidu.hsb.parser.ast.expression.primary.literal.LiteralString;

/**
 * @author xiongzhao@baidu.com
 */
public abstract class AliasableTableReference implements TableReference {
    protected final String alias;
    protected String aliasUpEscape;

    public AliasableTableReference(String alias) {
        this.alias = alias;
    }

    /**
     * @return upper-case, empty is possible
     */
    public String getAliasUnescapeUppercase() {
        if (alias == null || alias.length() <= 0) return alias;
        if (aliasUpEscape != null) return aliasUpEscape;

        switch (alias.charAt(0)) {
        case '`':
            return aliasUpEscape = Identifier.unescapeName(alias, true);
        case '\'':
            return aliasUpEscape = LiteralString.getUnescapedString(alias.substring(1, alias.length() - 1), true);
        case '_':
            int ind = -1;
            for (int i = 1; i < alias.length(); ++i) {
                if (alias.charAt(i) == '\'') {
                    ind = i;
                    break;
                }
            }
            if (ind >= 0) {
                LiteralString st =
                        new LiteralString(alias.substring(0, ind), alias.substring(ind + 1, alias.length() - 1), false);
                return aliasUpEscape = st.getUnescapedString(true);
            }
        default:
            return aliasUpEscape = alias.toUpperCase();
        }
    }

    public String getAlias() {
        return alias;
    }
}
