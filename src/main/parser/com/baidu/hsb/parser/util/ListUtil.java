/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author xiongzhao@baidu.com
 */
public final class ListUtil {

    @SuppressWarnings("rawtypes")
    public static List<?> createList(Object... objs) {
        return createList(new ArrayList(), objs);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<?> createList(List list, Object... objs) {
        if (objs != null) {
            for (Object obj : objs) {
                list.add(obj);
            }
        }
        return list;
    }

    public static boolean isEquals(List<? extends Object> l1, List<? extends Object> l2) {
        if (l1 == l2) return true;
        if (l1 == null) return l2 == null;
        if (l2 == null) return false;
        if (l1.size() != l2.size()) return false;
        Iterator<? extends Object> iter1 = l1.iterator();
        Iterator<? extends Object> iter2 = l2.iterator();
        while (iter1.hasNext()) {
            Object o1 = iter1.next();
            Object o2 = iter2.next();
            if (o1 == o2) continue;
            if (o1 == null && o2 != null) return false;
            if (!o1.equals(o2)) return false;
        }
        return true;
    }

}
