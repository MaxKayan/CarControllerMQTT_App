package com.example.carcontrollermqtt.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "devices")
public class Device {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private boolean active = false;
    private String username;
    private String password;
    private int keepAlive;

    public Device(long id, boolean active, String username, String password, int keepAlive) {
        if (id > 0L)
            this.id = id;
        this.active = active;
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
                active == device.active &&
                keepAlive == device.keepAlive &&
                Objects.equals(username, device.username) &&
                Objects.equals(password, device.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, active, username, password, keepAlive);
    }

    public long getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
}
