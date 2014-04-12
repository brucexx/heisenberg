/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2014 All Rights Reserved.
 */
package com.baidu.hsb.security;

import java.net.URL;

import com.baidu.hsb.HeisenbergContext;
import com.baidu.hsb.HeisenbergStartup;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: EncryptGen.java, v 0.1 2014年4月17日 上午10:20:53 HI:brucest0078 Exp $
 */
public class EncryptGen {

    public static void main(String[] args) throws Exception {
        args = new String[1];
        args[0] = "MiraCle";

        String fp = null;

        URL uri = HeisenbergStartup.class.getResource("/hsb.properties");
        fp = uri.getPath();

        HeisenbergContext.load(fp);
        //System.out.println("-->"+HeisenbergContext.getPriKey());

        if ((args.length < 1) || (args.length > 2)) {
            System.out.println("Arguments Wrong: <plaintext> [path to key.properties]");
            System.out.println("Attention: the key in properties for private key is 'privateKey'");
            return;
        }
        String privateKey = HeisenbergContext.getPriKey();
        System.out.println("priKey-->" + privateKey);
        System.out.println(KeyPairGen.encrypt(privateKey, args[0]));
    }
}
