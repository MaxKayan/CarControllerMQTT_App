package com.example.carcontrollermqtt.data.converter;

import androidx.room.TypeConverter;

import com.example.carcontrollermqtt.data.models.DqttMessage;

public class MessageStatusConverter {
    @TypeConverter
    public static DqttMessage.MessageStatus toStatus(int ordinal) {
        return DqttMessage.MessageStatus.values()[ordinal];
    }

    @TypeConverter
    public static int fromStatus(DqttMessage.MessageStatus status) {
        return status.ordinal();
    }
}
