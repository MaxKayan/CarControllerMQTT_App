package com.example.carcontrollermqtt.data.models;

public class DeviceEvent {
    private final DeviceEventStatus status;
    private final Device device;
    private final String message;

    public DeviceEvent(DeviceEventStatus status, Device device, String message) {
        this.status = status;
        this.device = device;
        this.message = message;
    }

    public DeviceEventStatus getStatus() {
        return status;
    }

    public Device getDevice() {
        return device;
    }

    public String getMessage() {
        return message;
    }

    public enum DeviceEventStatus {
        DISCONNECTED,
        PENDING,
        CONNECTED,
        ERROR
    }
}
