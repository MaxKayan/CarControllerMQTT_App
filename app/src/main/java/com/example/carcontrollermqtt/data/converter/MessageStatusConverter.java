package com.example.carcontrollermqtt.data.converter;

import androidx.room.TypeConverter;

import com.example.carcontrollermqtt.data.models.WqttMessage;

public class MessageStatusConverter {
    @TypeConverter
    public static WqttMessage.MessageStatus toStatus(int ordinal) {
        return WqttMessage.MessageStatus.values()[ordinal];
    }

    @TypeConverter
    public static int fromStatus(WqttMessage.MessageStatus status) {
        return status.ordinal();
    }
}
