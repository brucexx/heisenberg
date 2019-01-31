/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.store;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * 
 * @author brucexx
 *
 */
public final class ConsistentHash<T> {

    private final TreeMap<Integer, T> buckets = new TreeMap<Integer, T>();

    public ConsistentHash() {
    }

    public void add(String desc_of_node, T node) {
        final String vNode = desc_of_node;
        final byte[] md5 = getMD5(vNode);
        final int hash = getValue(md5, 0);
        buckets.put(hash, node);
    }

    public void remove(T node) {
        final String vNode = node.toString();
        final byte[] md5 = getMD5(vNode);
        final int hash = getValue(md5, 0);
        @SuppressWarnings("unused")
        T old = buckets.remove(hash);
    }

    public T get(String key) {
        if (buckets.isEmpty()) {
            return null;
        }
        int hash = md5HashingAlg(key, 0);
        return find(hash);
    }

    public T find(Integer hash) {
        Entry<Integer, T> entry = buckets.ceilingEntry(hash);
        if (entry != null) {
            return entry.getValue();
        } else {
            return buckets.get(buckets.firstKey());
        }
    }

    /**
     * Calculates the ketama hash value for a string
     */
    public static int md5HashingAlg(String key, int offset) {
        byte[] bKey = getMD5(key);
        int res = getValue(bKey, offset);
        return res;
    }

    public static byte[] getMD5(String key) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // md5.reset();
        md5.update(key.getBytes());
        return md5.digest();
    }

    public static int getValue(byte[] bKey, int offset) {
        int res = ((int) (bKey[3 + 4 * offset] & 0xFF) << 24) | ((int) (bKey[2 + 4 * offset] & 0xFF) << 16)
                | ((int) (bKey[1 + 4 * offset] & 0xFF) << 8) | (int) (bKey[4 * offset] & 0xFF);
        return res;
    }

    public static void main(String args[]) {
        ConsistentHash<String> c = new ConsistentHash<String>();
        c.add("1", "hh1");
        c.add("2", "hh2");
        c.add("3", "hh3");
        c.add("4", "hh4");

        System.out.println(c.get("good"));
        System.out.println(c.get("good1"));
        System.out.println(c.get("good2"));
        System.out.println(c.get("good3"));

    }
}