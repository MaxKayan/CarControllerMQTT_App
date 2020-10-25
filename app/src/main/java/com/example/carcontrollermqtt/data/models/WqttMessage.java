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
    private final Long deviceId;
    private final int mqttMessageId;
    private final MessageStatus status;
    private final Date dateTime;
    private final boolean isIncoming;
    private final String topic;
    private final String payload;
    @PrimaryKey(autoGenerate = true)
    private long id;

    public WqttMessage(long id, long deviceId, int mqttMessageId, MessageStatus status, Date dateTime, boolean isIncoming, String topic, String payload) {
        if (id != 0L)
            this.id = id;
        this.deviceId = deviceId;
        this.mqttMessageId = mqttMessageId;
        this.status = status;
        this.dateTime = dateTime;
        this.isIncoming = isIncoming;
        this.topic = topic;
        this.payload = payload;
    }

    public static WqttMessage newInstance(long deviceId, int mqttMessageId, Date dateTime, boolean isIncoming, String topic, String payload) {
        return new WqttMessage(0L, deviceId, mqttMessageId, isIncoming ? MessageStatus.DELIVERED : MessageStatus.PENDING,
                dateTime, isIncoming, topic, payload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WqttMessage that = (WqttMessage) o;
        return deviceId == that.deviceId &&
                mqttMessageId == that.mqttMessageId &&
                isIncoming == that.isIncoming &&
                id == that.id &&
                status == that.status &&
                Objects.equals(dateTime, that.dateTime) &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, mqttMessageId, status, dateTime, isIncoming, topic, payload, id);
    }

    public long getId() {
        return id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public int getMqttMessageId() {
        return mqttMessageId;
    }

    public MessageStatus getStatus() {
        return status;
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

    public enum MessageStatus {
        PENDING,
        DELIVERED,
        FAILED
    }

    public WqttMessage cloneAndUpdate(long id, MessageStatus newStatus) {
        return new WqttMessage(id, this.deviceId, this.mqttMessageId, newStatus, this.dateTime, this.isIncoming, this.topic, this.payload);
    }
}
