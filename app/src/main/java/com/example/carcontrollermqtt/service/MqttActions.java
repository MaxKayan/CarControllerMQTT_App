package com.example.carcontrollermqtt.service;

import com.example.carcontrollermqtt.data.models.Device;

public class MqttActions {
    public static void requestInfo(DqttMessageManager messageManager) {
        messageManager.sendMessage("get", "info");
    }

    public static void requestInfo(DqttMessageManager messageManager, Device device) {
        messageManager.sendMessage(device, "get", "info");
    }
}
