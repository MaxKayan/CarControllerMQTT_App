package com.example.carcontrollermqtt.data.models.transactions;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.WqttMessage;

public class WqttMessageWithDevice {
    @Embedded
    public WqttMessage message;
    @Relation(
            parentColumn = "deviceId",
            entityColumn = "id"
    )
    public Device carModels;
}