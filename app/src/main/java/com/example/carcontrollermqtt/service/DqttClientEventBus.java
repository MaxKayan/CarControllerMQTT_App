package com.example.carcontrollermqtt.service;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.DeviceEvent;

import java.util.HashMap;
import java.util.Map;

public final class DqttClientEventBus {
    private static final String TAG = "WqttClientEventBus";
    private final Map<String, MutableLiveData<DeviceEvent>> bus;
    private final Map<String, DeviceEvent> lastDeviceStatus = new HashMap<>();

    private DqttClientEventBus() {
        bus = new HashMap<>();
    }

    public static DqttClientEventBus getInstance() {
        return SingletonHolder.DATA_BUS;
    }

    public MutableLiveData<DeviceEvent> getChannel(String deviceName) {
        if (!bus.containsKey(deviceName)) {
            MutableLiveData<DeviceEvent> liveData = new MutableLiveData<>();
            // TODO: Should probably switch to using RxJava bus instead of LiveData
            liveData.observeForever(deviceEvent -> {
                if (deviceEvent == null) return;
                lastDeviceStatus.put(deviceEvent.getDevice().getUsername(), deviceEvent);
                Log.d(TAG, "getChannel: cached event - " + lastDeviceStatus.get(deviceEvent.getDevice().getUsername()));
            });

            bus.put(deviceName, liveData);
        }
        return bus.get(deviceName);
    }

    @Nullable
    public DeviceEvent getLastEventForDevice(Device device) {
        return lastDeviceStatus.get(device.getUsername());
    }

    private static class SingletonHolder {
        private static final DqttClientEventBus DATA_BUS = new DqttClientEventBus();
    }

}
