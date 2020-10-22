package com.example.carcontrollermqtt.data.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "devices",
        indices = {@Index(value = {"username"}, unique = true)})
public class Device implements Serializable {
    private final boolean enabled;
    private final boolean selected;
    private final String label;
    private final String baseTopic;
    private final String username;
    private final String password;
    private final int keepAlive;
    @PrimaryKey(autoGenerate = true)
    private long id;
    @Nullable
    @Ignore
    private DeviceEvent event;

    public Device(long id, boolean enabled, boolean selected, String label, String baseTopic, String username, String password, int keepAlive) {
        if (id > 0L)
            this.id = id;
        this.enabled = enabled;
        this.selected = selected;
        this.label = label;
        this.baseTopic = baseTopic;
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
                Objects.equals(baseTopic, device.baseTopic) &&
                Objects.equals(username, device.username) &&
                Objects.equals(password, device.password) &&
                Objects.equals(event, device.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, selected, label, baseTopic, username, password, keepAlive, id, event);
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

    public String getBaseTopic() {
        return baseTopic;
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

    @Nullable
    public DeviceEvent getEvent() {
        return event;
    }

    public void setEvent(@Nullable DeviceEvent event) {
        this.event = event;
    }

    public Device cloneWithEnabled(boolean isEnabled) {
        return new Device(this.id, isEnabled, this.selected, this.label, this.baseTopic, this.username, this.password, this.keepAlive);
    }

    public Device cloneWithSelected(boolean isSelected) {
        return new Device(this.id, this.enabled, isSelected, this.label, this.baseTopic, this.username, this.password, this.keepAlive);
    }

}
