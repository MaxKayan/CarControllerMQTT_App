package com.example.carcontrollermqtt.data.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "messages",
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
    private long deviceId;
    private Date dateTime;
    private boolean isIncoming;
    private String topic;
    private String payload;
}
