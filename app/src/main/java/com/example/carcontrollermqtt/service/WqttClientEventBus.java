package com.example.carcontrollermqtt.service;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.DeviceEvent;

import java.util.HashMap;
import java.util.Map;

public final class WqttClientEventBus {
    private static final String TAG = "WqttClientEventBus";
    private final Map<String, MutableLiveData<DeviceEvent>> bus;
    private final Map<String, DeviceEvent> cachedEvents = new HashMap<>();

    private WqttClientEventBus() {
        bus = new HashMap<>();
    }

    public static WqttClientEventBus getInstance() {
        return SingletonHolder.DATA_BUS;
    }

    public MutableLiveData<DeviceEvent> getChannel(String deviceName) {
        if (!bus.containsKey(deviceName)) {
            MutableLiveData<DeviceEvent> liveData = new MutableLiveData<>();
            // TODO: Should probably switch to using RxJava bus instead of LiveData
            liveData.observeForever(deviceEvent -> {
                if (deviceEvent == null) return;
                cachedEvents.put(deviceEvent.getDevice().getUsername(), deviceEvent);
                Log.d(TAG, "getChannel: cached event - " + cachedEvents.get(deviceEvent.getDevice().getUsername()));
            });

            bus.put(deviceName, liveData);
        }
        return bus.get(deviceName);
    }

    @Nullable
    public DeviceEvent getLastEventForDevice(Device device) {
        return cachedEvents.get(device.getUsername());
    }

    private static class SingletonHolder {
        private static final WqttClientEventBus DATA_BUS = new WqttClientEventBus();
    }

}
