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
 * @version $Id: DecryptGen.java, v 0.1 2014年4月17日 下午8:31:45 HI:brucest0078 Exp $
 */
public class DecryptGen {

    public static void main(String[] args) throws Exception {

        args = new String[1];
        args[0] = "h1/bSN8vYdBAlFxvHfoCBZM+yehb1HvH8tUK3WU4Fqki7CALPVLrOJi5/pl+PhmU14YLGoxwSFqaYo+D2taMhQ==";

        String fp = null;

        URL uri = HeisenbergStartup.class.getResource("/hsb.properties");
        fp = uri.getPath();

        HeisenbergContext.load(fp);
        //;

        if ((args.length < 1) || (args.length > 2)) {
            System.out.println("Arguments Wrong: <plaintext> [path to key.properties]");
            System.out.println("Attention: the key in properties for private key is 'privateKey'");
            return;
        }
        String pubKey = HeisenbergContext.getPubKey();
        System.out.println("publicKey-->" + pubKey);
        System.out.println(KeyPairGen.decrypt(pubKey, args[0]));

    }

}
