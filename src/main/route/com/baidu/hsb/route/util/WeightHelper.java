/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2014 All Rights Reserved.
 */
package com.baidu.hsb.route.util;

import java.util.Random;

import com.baidu.hsb.config.model.config.DataNodeConfig;
import com.baidu.hsb.mysql.MySQLDataNode;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: WeightHelper.java, v 0.1 2014年3月6日 上午11:40:52 HI:brucest0078 Exp $
 */
public class WeightHelper {

    private static Random r = new Random();

    public static int getReadIndex(MySQLDataNode dataNode) {
        DataNodeConfig config = dataNode.getConfig();
        int idx = dataNode.getActivedIndex();
        int m = config.getMasterReadWeight() + config.getSlaveReadWeight();
        int k = (int) (r.nextDouble() * (double) m);
        boolean isSlave = k >= config.getMasterReadWeight();
        if (isSlave) {
            if (idx == 0) {
                k = 1;
            }
            if (idx == 1) {
                k = 2;
            }
            if (idx == 2) {
                k = 1;
            }
        }
        return k;

    }

    public static void main(String[] args) {
        DataNodeConfig config = new DataNodeConfig();
        config.setMasterReadWeight(9999);
        config.setSlaveReadWeight(1);
        int c = 0;
        int m = config.getMasterReadWeight() + config.getSlaveReadWeight();
        for (int i = 0; i < 10000; i++) {
            int k = (int) (r.nextDouble() * (double) m);
            boolean isSlave = k >= config.getMasterReadWeight();
            if (isSlave)
                c++;
        }
        System.out.print(c);

    }

}
