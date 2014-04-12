/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.parser.util;

/**
 * 
 * @author xiongzhao@baidu.com
 */
public final class Pair<K, V> {

    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(key).append(", ").append(value).append(")");
        return sb.toString();
    }

    private static final int HASH_CONST = 37;

    @Override
    public int hashCode() {
        int hash = 17;
        if (key == null) {
            hash += HASH_CONST;
        } else {
            hash = hash << 5 + hash << 1 + hash + key.hashCode();
        }
        if (value == null) {
            hash += HASH_CONST;
        } else {
            hash = hash << 5 + hash << 1 + hash + value.hashCode();
        }
        return hash;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pair)) return false;
        Pair that = (Pair) obj;
        return isEquals(this.key, that.key) && isEquals(this.value, that.value);
    }

    private boolean isEquals(Object o1, Object o2) {
        if (o1 == o2) return true;
        if (o1 == null) return o2 == null;
        return o1.equals(o2);
    }

}
