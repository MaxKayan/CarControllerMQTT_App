package com.example.carcontrollermqtt.data.models;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.carcontrollermqtt.service.DqttMessageManager;

import java.io.Serializable;
import java.util.Objects;

/**
 * Device object that has credentials and options for Mqtt Connection
 */
@Entity(tableName = "devices",
        indices = {@Index(value = {"username"}, unique = true)})
public class Device implements Serializable {
    private final boolean enabled;
    private final boolean selected;
    private final String label;
    private final String avatarUriString;
    private final String deviceId;
    private final String username;
    private final String password;
    private final int keepAlive;
    @PrimaryKey(autoGenerate = true)
    private long id;
    @Nullable
    @Ignore
    private DeviceEvent event;

    public Device(long id, boolean enabled, boolean selected, String label, String avatarUriString, String deviceId, String username, String password, int keepAlive) {
        if (id > 0L)
            this.id = id;
        this.enabled = enabled;
        this.selected = selected;
        this.label = label;
        this.avatarUriString = avatarUriString;
        this.deviceId = deviceId;
        this.username = username;
        this.password = password;
        this.keepAlive = keepAlive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return enabled == device.enabled &&
                selected == device.selected &&
                keepAlive == device.keepAlive &&
                id == device.id &&
                Objects.equals(label, device.label) &&
                Objects.equals(avatarUriString, device.avatarUriString) &&
                Objects.equals(deviceId, device.deviceId) &&
                Objects.equals(username, device.username) &&
                Objects.equals(password, device.password) &&
                Objects.equals(event, device.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, selected, label, avatarUriString, deviceId, username, password, keepAlive, id, event);
    }

    @NonNull
    @Override
    public String toString() {
        return username + "-" + super.toString();
    }

    public long getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getLabel() {
        return label;
    }

    public String getAvatarUriString() {
        return avatarUriString;
    }

    @Nullable
    public Uri getAvatarUri() {
        if (avatarUriString == null) return null;
        return Uri.parse(avatarUriString);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void requestInfo(DqttMessageManager manager) {
        manager.sendMessage(this, "get", "info");
    }

    public void requestLocation(DqttMessageManager manager) {
        manager.sendMessage(this, "get", "location");
    }

    @Nullable
    public DeviceEvent getEvent() {
        return event;
    }

    public void setEvent(@Nullable DeviceEvent event) {
        this.event = event;
    }

    public Device cloneWithEnabled(boolean isEnabled) {
        return new Device(this.id, isEnabled, this.selected, this.label, avatarUriString, this.deviceId, this.username, this.password, this.keepAlive);
    }

    public Device cloneWithSelected(boolean isSelected) {
        return new Device(this.id, this.enabled, isSelected, this.label, avatarUriString, this.deviceId, this.username, this.password, this.keepAlive);
    }

}
