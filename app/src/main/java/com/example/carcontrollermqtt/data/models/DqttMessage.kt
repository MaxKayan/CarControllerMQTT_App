package com.example.carcontrollermqtt.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "messages",
        indices = [Index(value = ["deviceId"])],
        foreignKeys = [
            ForeignKey(entity = Device::class,
                    parentColumns = ["id"],
                    childColumns = ["deviceId"], onDelete = ForeignKey.CASCADE)
        ])
data class DqttMessage(
        @PrimaryKey(autoGenerate = true)
        val id: Long? = null,
        val deviceId: Long,
        val mqttMessageId: Int,
        val status: MessageStatus,
        val dateTime: Date,
        val isIncoming: Boolean,
        val topic: String,
        val payload: String
) {

    fun cloneAndUpdate(id: Long, newStatus: MessageStatus): DqttMessage {
        return this.copy(id = id, status = newStatus)
    }

    enum class MessageStatus {
        PENDING, DELIVERED, FAILED
    }

    companion object {
        fun newInstance(deviceId: Long, mqttMessageId: Int, dateTime: Date, isIncoming: Boolean, topic: String, payload: String): DqttMessage {
            return DqttMessage(null, deviceId, mqttMessageId, if (isIncoming) MessageStatus.DELIVERED else MessageStatus.PENDING,
                    dateTime, isIncoming, topic, payload)
        }
    }
}