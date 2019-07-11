package com.yunji.gateway.netty.http.match;


/**
 * @author maple 2018.09.06 上午10:25
 */
public class Pair<K, V> {


    private K key;


    public K getKey() {
        return key;
    }


    private V value;


    public V getValue() {
        return value;
    }


    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
