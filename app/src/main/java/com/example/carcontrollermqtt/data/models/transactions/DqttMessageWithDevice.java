package com.example.carcontrollermqtt.data.models.transactions;

import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.DqttMessage;

public class DqttMessageWithDevice {
    @Embedded
    public DqttMessage message;
    @Relation(
            parentColumn = "deviceId",
            entityColumn = "id"
    )
    @Nullable
    public Device device;
}