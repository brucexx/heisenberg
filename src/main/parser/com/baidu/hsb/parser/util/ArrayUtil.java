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
 * (created at 2011-10-24)
 */
package com.baidu.hsb.parser.util;


/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: ArrayUtil.java, v 0.1 2013年12月25日 下午5:15:51 HI:brucest0078 Exp $
 */
public class ArrayUtil {
    public static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

    public static boolean contains(String[] list, String str) {
        if (list == null)
            return false;
        for (String string : list) {
            if (equals(str, string)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * 
     * @param obj
     * @param seperator
     * @return
     */
    public static String join(Object[] obj, String seperator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = obj.length; i < len; i++) {
            sb.append(obj[i] == null ? "" : obj[i].toString());
            if (i < obj.length - 1) {
                sb.append(seperator);
            }
        }
        return sb.toString();
    }
}
