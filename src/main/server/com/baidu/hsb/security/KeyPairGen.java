/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2014 All Rights Reserved.
 */
package com.baidu.hsb.security;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: KeyPairGenerator.java, v 0.1 2014年4月17日 上午10:01:55 HI:brucest0078 Exp $
 */
public class KeyPairGen {

    public static String decrypt(String publicKeyText, String cipherText) throws Exception {
        PublicKey publicKey = getPublicKey(publicKeyText);

        return decrypt(publicKey, cipherText);
    }

    public static PublicKey getPublicKeyByX509(String x509File) {
        if ((x509File == null) || (x509File.length() == 0)) {
            return getPublicKey(null);
        }

        FileInputStream in = null;
        try {
            in = new FileInputStream(x509File);

            CertificateFactory factory = CertificateFactory.getInstance("X.509");

            Certificate cer = factory.generateCertificate(in);
            PublicKey localPublicKey = cer.getPublicKey();
            return localPublicKey;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
    

    public static String decrypt(PublicKey publicKey, String cipherText)
      throws Exception
    {
      Cipher cipher = Cipher.getInstance("RSA");
      try {
        cipher.init(2, publicKey);
      }
      catch (InvalidKeyException e)
      {
        RSAPublicKey rsaPublicKey = (RSAPublicKey)publicKey;
        RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
        Key fakePrivateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
        cipher = Cipher.getInstance("RSA");
        cipher.init(2, fakePrivateKey);
      }

      if ((cipherText == null) || (cipherText.length() == 0)) {
        return cipherText;
      }

      byte[] cipherBytes = Base64.decodeBase64(cipherText);
      byte[] plainBytes = cipher.doFinal(cipherBytes);

      return new String(plainBytes);
    }

    public static PublicKey getPublicKey(String publicKeyText) {
        if ((publicKeyText == null) || (publicKeyText.length() == 0)) {
            publicKeyText = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKHGwq7q2RmwuRgKxBypQHw0mYu4BQZ3eMsTrdK8E6igRcxsobUC7uT0SoxIjl1WveWniCASejoQtn/BY6hVKWsCAwEAAQ==";
        }
        try {
            byte[] publicKeyBytes = Base64.decodeBase64(publicKeyText);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(x509KeySpec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        }

    }

    private static byte[][] genKeyPairBytes(int keySize) throws NoSuchAlgorithmException {
        byte[][] keyPairBytes = new byte[2][];

        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(keySize, new SecureRandom());
        KeyPair pair = gen.generateKeyPair();

        keyPairBytes[0] = pair.getPrivate().getEncoded();
        keyPairBytes[1] = pair.getPublic().getEncoded();

        return keyPairBytes;
    }

    public static String[] genKeyPair(int keySize) throws NoSuchAlgorithmException {
        byte[][] keyPairBytes = genKeyPairBytes(keySize);
        String[] keyPairs = new String[2];

        keyPairs[0] = Base64.encodeBase64String(keyPairBytes[0]);
        keyPairs[1] = Base64.encodeBase64String(keyPairBytes[1]);

        return keyPairs;
    }

    public static String encrypt(String key, String plainText) throws Exception {
        if (key == null) {
            key = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAocbCrurZGbC5GArEHKlAfDSZi7gFBnd4yxOt0rwTqKBFzGyhtQLu5PRKjEiOXVa95aeIIBJ6OhC2f8FjqFUpawIDAQABAkAPejKaBYHrwUqUEEOe8lpnB6lBAsQIUFnQI/vXU4MV+MhIzW0BLVZCiarIQqUXeOhThVWXKFt8GxCykrrUsQ6BAiEA4vMVxEHBovz1di3aozzFvSMdsjTcYRRo82hS5Ru2/OECIQC2fAPoXixVTVY7bNMeuxCP4954ZkXp7fEPDINCjcQDywIgcc8XLkkPcs3Jxk7uYofaXaPbg39wuJpEmzPIxi3k0OECIGubmdpOnin3HuCP/bbjbJLNNoUdGiEmFL5hDI4UdwAdAiEAtcAwbm08bKN7pwwvyqaCBC//VnEWaq39DCzxr+Z2EIk=";
        }

        byte[] keyBytes = Base64.decodeBase64(key);
        return encrypt(keyBytes, plainText);
    }

    public static String encrypt(byte[] keyBytes, String plainText) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = factory.generatePrivate(spec);
        Cipher cipher = Cipher.getInstance("RSA");
        try {
            cipher.init(1, privateKey);
        } catch (InvalidKeyException e) {
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rsaPrivateKey.getModulus(),
                rsaPrivateKey.getPrivateExponent());
            Key fakePublicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);
            cipher = Cipher.getInstance("RSA");
            cipher.init(1, fakePublicKey);
        }

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.encodeBase64String(encryptedBytes);

    }

    public static void main(String[] args) throws Exception {
        String[] pair = genKeyPair(512);
        System.out.println("Private Key : " + pair[0]);
        System.out.println("Public Key  : " + pair[1]);

        if (args.length == 1) {
            Properties prop = new Properties();
            prop.setProperty("privateKey", pair[0]);
            prop.setProperty("publicKey", pair[1]);

            System.out.println("Store the generated keypair into file - " + args[0] + ".");
            FileOutputStream fos = new FileOutputStream(args[0]);
            try {
                prop.store(fos, "Generated Key Pair");
            } finally {
                fos.close();
            }

        }
    }

}
