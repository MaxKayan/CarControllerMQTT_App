package com.example.carcontrollermqtt.service;

import com.example.carcontrollermqtt.data.models.Device;

public class WqttActions {
    public static void requestInfo(WqttMessageManager messageManager) {
        messageManager.sendMessage("dev/get", "info");
    }

    public static void requestInfo(WqttMessageManager messageManager, Device device) {
        messageManager.sendMessage(device, "dev/get", "info");
    }
}
