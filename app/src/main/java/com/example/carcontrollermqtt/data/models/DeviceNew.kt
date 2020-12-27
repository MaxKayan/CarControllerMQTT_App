package com.example.carcontrollermqtt.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.carcontrollermqtt.service.DqttMessageManager

@Entity(tableName = "devices", indices = [Index(value = ["username"], unique = true)])
data class DeviceNew(
        @PrimaryKey(autoGenerate = true) val id: Long,
        val enabled: Boolean,
        val selected: Boolean,
        val label: String,
        val avatarUriString: String,
        val deviceId: String,
        val username: String,
        val password: String,
        val keepAlive: Int,

        @Ignore
        var event: DeviceEvent?
) {
    override fun toString(): String {
        return username + "-" + super.toString()
    }

    fun requestInfo(manager: DqttMessageManager) {
//        manager.sendMessage(this, "get", "info")
    }

    fun requestLocation(manager: DqttMessageManager) {
//        manager.sendMessage(this, "get", "location")
    }

    //  I left these methods so both Device implementations can work
    //  without editing the existing Java code.
    //  I understand that if i'll use Kotlin for the whole project, these methods would be redundant.
    fun cloneWithEnabled(isEnabled: Boolean): DeviceNew {
        return this.copy(enabled = isEnabled)
    }

    fun cloneWithSelected(isSelected: Boolean): DeviceNew {
        return this.copy(selected = isSelected)
    }
}
