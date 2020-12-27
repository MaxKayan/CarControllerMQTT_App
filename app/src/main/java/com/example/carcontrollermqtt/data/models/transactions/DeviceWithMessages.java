package com.example.carcontrollermqtt.data.models.transactions;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.DqttMessage;

import java.util.List;

public class DeviceWithMessages {
    @Embedded
    public Device device;
    @Relation(
            parentColumn = "id",
            entityColumn = "deviceId"
    )
    public List<DqttMessage> messages;
}
