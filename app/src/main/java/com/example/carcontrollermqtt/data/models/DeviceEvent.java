package com.example.carcontrollermqtt.data.models;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class DeviceEvent implements Serializable {
    private final DeviceEventStatus status;
    private final Device device;
    @Nullable
    private final String message;

    private DeviceEvent(DeviceEventStatus status, Device device, @Nullable String message) {
        this.status = status;
        this.device = device;
        this.message = message;
    }

    public static DeviceEvent disconnected(Device device, @Nullable String message) {
        return new DeviceEvent(DeviceEventStatus.DISCONNECTED, device, message);
    }

    public static DeviceEvent pending(Device device, @Nullable String message) {
        return new DeviceEvent(DeviceEventStatus.PENDING, device, message);
    }

    public static DeviceEvent connected(Device device, @Nullable String message) {
        return new DeviceEvent(DeviceEventStatus.CONNECTED, device, message);
    }

    public static DeviceEvent error(Device device, @Nullable String message) {
        return new DeviceEvent(DeviceEventStatus.ERROR, device, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceEvent event = (DeviceEvent) o;
        return status == event.status &&
                Objects.equals(device, event.device) &&
                Objects.equals(message, event.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, device, message);
    }

    public DeviceEventStatus getStatus() {
        return status;
    }

    public Device getDevice() {
        return device;
    }

    @Nullable
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
