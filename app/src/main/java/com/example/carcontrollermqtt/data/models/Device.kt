package com.example.carcontrollermqtt.data.models

import android.net.Uri
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.carcontrollermqtt.service.DqttMessageManager
import java.io.Serializable

/**
 * Device object that has credentials and options for Mqtt Connection
 */
@Entity(tableName = "devices", indices = [Index(value = ["username"], unique = true)])
data class Device(
        @PrimaryKey(autoGenerate = true) val id: Long?,
        val enabled: Boolean,
        val selected: Boolean,
        val label: String,
        val avatarUriString: String?,
        val deviceId: String,
        val username: String,
        val password: String,
        val keepAlive: Int,
) : Serializable {

    @Ignore
    var event: DeviceEvent? = null

    override fun toString(): String {
        return username + "-" + super.toString()
    }

    fun getAvatarUri(): Uri? {
//        return if (avatarUriString == null) null else Uri.parse(avatarUriString)
        avatarUriString?.let { return Uri.parse(it) }
        return null
    }

    fun requestInfo(manager: DqttMessageManager) {
        manager.sendMessage(this, "get", "info")
    }

    fun requestLocation(manager: DqttMessageManager) {
        manager.sendMessage(this, "get", "location")
    }

    //  I left these methods so both Device implementations can work
    //  without editing the existing Java code.
    //  I understand that if i'll use Kotlin for the whole project, these methods would be redundant.
    fun cloneWithEnabled(isEnabled: Boolean): Device {
        return this.copy(enabled = isEnabled)
    }

    fun cloneWithSelected(isSelected: Boolean): Device {
        return this.copy(selected = isSelected)
    }
}