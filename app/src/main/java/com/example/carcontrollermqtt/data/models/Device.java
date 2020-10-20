package com.example.carcontrollermqtt.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "devices")
public class Device implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private boolean enabled = true;
    private boolean selected = false;
    private String username;
    private String password;
    private int keepAlive;

    @Ignore
    private boolean isUp = false;

    public Device(long id, boolean enabled, boolean selected, String username, String password, int keepAlive) {
        if (id > 0L)
            this.id = id;
        this.enabled = enabled;
        this.selected = selected;
        this.username = username;
        this.password = password;
        this.keepAlive = keepAlive;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return id == device.id &&
                enabled == device.enabled &&
                selected == device.selected &&
                keepAlive == device.keepAlive &&
                Objects.equals(username, device.username) &&
                Objects.equals(password, device.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, enabled, selected, username, password, keepAlive);
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

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public boolean isUp() {
        return isUp;
    }

    public void setUp(boolean up) {
        isUp = up;
    }

    public Device cloneWithEnabled(boolean isEnabled) {
        return new Device(this.id, isEnabled, this.selected, this.username, this.password, this.keepAlive);
    }

    public Device cloneWithSelected(boolean isSelected) {
        return new Device(this.id, this.enabled, isSelected, this.username, this.password, this.keepAlive);
    }

}
