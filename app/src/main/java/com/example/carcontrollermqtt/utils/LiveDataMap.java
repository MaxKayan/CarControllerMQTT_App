package com.example.carcontrollermqtt.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.Map;

public class LiveDataMap<K, V> {
    private final Map<K, MutableLiveData<V>> map;

    public LiveDataMap() {
        this.map = new HashMap<>();
    }

    public void set(K key, V value) {
        if (map.containsKey(key)) {
            map.get(key).setValue(value);
        } else {
            map.put(key, new MutableLiveData<>(value));
        }
    }

    public void post(K key, V value) {
        if (map.containsKey(key)) {
            map.get(key).postValue(value);
        } else {
            map.put(key, new MutableLiveData<>(value));
        }
    }

    public LiveData<V> observe(K key) {
        if (!map.containsKey(key)) {
            map.put(key, new MutableLiveData<>());
        }

        return map.get(key);
    }

    public void remove(K key) {
        map.remove(key);
    }
}
