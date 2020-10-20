package com.example.carcontrollermqtt.service;

import androidx.lifecycle.MutableLiveData;

import com.example.carcontrollermqtt.data.models.DeviceEvent;

import java.util.HashMap;
import java.util.Map;

public final class WqttClientEventBus {
    private final Map<String, MutableLiveData<DeviceEvent>> bus;

    private WqttClientEventBus() {
        bus = new HashMap<>();
    }

    private static class SingletonHolder {
        private static final WqttClientEventBus DATA_BUS = new WqttClientEventBus();
    }

    public static WqttClientEventBus getInstance() {
        return SingletonHolder.DATA_BUS;
    }

    public MutableLiveData<DeviceEvent> getChannel(String deviceName) {
        if (!bus.containsKey(deviceName)) {
            bus.put(deviceName, new MutableLiveData<>());
        }
        return bus.get(deviceName);
    }

//    public <T> MutableLiveData<T> getChannel(String target, Class<T> type) {
//        if (!bus.containsKey(target)) {
//            bus.put(target, new MutableLiveData<>());
//        }
//        return (MutableLiveData<T>) bus.get(target);
//    }
//
//    public MutableLiveData<Object> getChannel(String target) {
//        return getChannel(target, Object.class);
//    }
}
