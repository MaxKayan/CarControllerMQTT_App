package com.example.carcontrollermqtt.data.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Objects;

@Entity(tableName = "messages",
        indices = {
                @Index(value = "deviceId")
        },
        foreignKeys = {
                @ForeignKey(
                        entity = Device.class,
                        parentColumns = "id",
                        childColumns = "deviceId",
                        onDelete = ForeignKey.SET_NULL
                )
        })
public class WqttMessage {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private final long deviceId;
    private final Date dateTime;
    private final boolean isIncoming;
    private final String topic;
    private final String payload;

    public WqttMessage(long id, long deviceId, Date dateTime, boolean isIncoming, String topic, String payload) {
        if (id != 0L)
            this.id = id;
        this.deviceId = deviceId;
        this.dateTime = dateTime;
        this.isIncoming = isIncoming;
        this.topic = topic;
        this.payload = payload;
    }

    public static WqttMessage newInstance(long deviceId, Date dateTime, boolean isIncoming, String topic, String payload) {
        return new WqttMessage(0L, deviceId, dateTime, isIncoming, topic, payload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WqttMessage that = (WqttMessage) o;
        return id == that.id &&
                deviceId == that.deviceId &&
                isIncoming == that.isIncoming &&
                Objects.equals(dateTime, that.dateTime) &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceId, dateTime, isIncoming, topic, payload);
    }

    public long getId() {
        return id;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public boolean isIncoming() {
        return isIncoming;
    }

    public String getTopic() {
        return topic;
    }

    public String getPayload() {
        return payload;
    }
}
