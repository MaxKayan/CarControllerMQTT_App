package com.example.carcontrollermqtt.service

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.carcontrollermqtt.data.local.AppDatabase
import com.example.carcontrollermqtt.data.local.dao.DqttMessageDao
import com.example.carcontrollermqtt.data.models.Device
import com.example.carcontrollermqtt.data.models.DqttMessage
import com.example.carcontrollermqtt.data.models.DqttMessage.Companion.newInstance
import com.example.carcontrollermqtt.data.models.messages.InfoMessage
import com.example.carcontrollermqtt.data.models.messages.LocationMessage
import com.example.carcontrollermqtt.utils.LiveDataMap
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

class DqttMessageManager private constructor(context: Context, clientManager: DqttClientManager) {
    // App database
    private val database: AppDatabase = AppDatabase.getInstance(context)
    private val messageDao: DqttMessageDao
    private val gson: Gson

    // Client manager to get MqttClient based on device name
    private val clientManager: DqttClientManager
    private val deviceInfoMessages = LiveDataMap<String, InfoMessage>()
    private val deviceLocationMessages = LiveDataMap<String, LocationMessage>()

    // Main private constructor to be called from public static function
    init {
        messageDao = database.messageDao()
        this.clientManager = clientManager
        gson = GsonProvider.getGsonInstance()
    }

    companion object {
        private const val TAG = "WqttMessageManager"

        // Singleton instance
        private var instance: DqttMessageManager? = null

        /**
         * @param context       App context
         * @param clientManager Instantiated singleton. Needed to get client connections from
         * @return Singleton instance
         */
        @JvmStatic
        fun getInstance(context: Context, clientManager: DqttClientManager): DqttMessageManager? {
            if (instance == null) {
                instance = DqttMessageManager(context, clientManager)
            }
            return instance
        }

        /**
         * Makes a subscribe-ready mqtt topic based on the device.
         *
         * @param device        Device to be subscribed.
         * @param endpointTopic Device's unique id, usually a MAC
         * @return Fully qualified topic string, ready to be used in mqtt subscription.
         */
        fun getQualifiedTopic(device: Device, endpointTopic: String?): String {
            return String.format("data/%s/%s", device.deviceId, endpointTopic)
        }

        /**
         * Basically the reverse for [this static method][.getQualifiedTopic]
         *
         * @param fullTopic Device's fully qualified mqtt topic string
         * @return Device's endpoint topic - a unique id, usually a MAC
         */
        @JvmStatic
        fun getEndpointTopic(fullTopic: String): String {
            return fullTopic.substring(fullTopic.indexOf("/", fullTopic.indexOf("/") + 1) + 1)
        }
    }

    /**
     * @param device    Device that received the message
     * @param fullTopic Message fullTopic
     * @param message   Message object from client callback
     */
    fun receiveMessage(device: Device, fullTopic: String, message: MqttMessage) {
        writeToDb { messageDao.insert(newInstance(device.id!!, message.id, Date(), true, fullTopic, message.toString())) }

        when (getEndpointTopic(fullTopic)) {
            "info" -> {
                Log.d(TAG, "receiveMessage: info $message")
                deviceInfoMessages[device.username] = deserializeJSON(message.toString(), InfoMessage::class.java)
            }
            "location" -> {
                Log.d(TAG, "receiveMessage: location $message")
                deviceLocationMessages[device.username] = deserializeJSON(message.toString(), LocationMessage::class.java)
            }
            else -> Log.w(TAG, "receiveMessage: unknown fullTopic! - $fullTopic")
        }
    }

    private fun <T> deserializeJSON(payload: String, messageClass: Class<T>): T? {
        var result: T? = null
        try {
            result = gson.fromJson(payload, messageClass)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "receiveMessage: failed to parse", e)
        }
        return result
    }

    /**
     * Send new message from the current selected [device][Device]
     *
     * @param endpointTopic Message topic
     * @param payload       Payload string
     */
    fun sendMessage(endpointTopic: String?, payload: String) {
        val selectedDevice = DqttClientManager.getSelectedDevice()
        Log.d(TAG, "sendMessage: selected device $selectedDevice")
        selectedDevice?.let { sendMessage(it, endpointTopic, payload) }
    }

    /**
     * Send new message from the specified [device][Device]
     *
     * @param device        Device to send message from
     * @param endpointTopic Message topic
     * @param payload       Payload string
     */
    @SuppressLint("CheckResult")
    fun sendMessage(device: Device, endpointTopic: String?, payload: String) {
        val client = clientManager.getDqttClient(device.username)
        client?.let {
            val fullTopic = getQualifiedTopic(device, endpointTopic)
            val mqttMessage = MqttMessage(payload.toByteArray())
            val dqttMessage = newInstance(device.id!!, mqttMessage.id, Date(), false, fullTopic, payload)

            CoroutineScope(IO).launch {
                val id = messageDao.insertAndReadId(dqttMessage)
                Log.d(TAG, "sendMessage: id is $id")

                try {
                    it.client.publish(fullTopic, MqttMessage(payload.toByteArray()), null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            Log.d(TAG, "onSuccess: updating msg status")
                            writeToDb { messageDao.update(dqttMessage.copy(id = id, status = DqttMessage.MessageStatus.DELIVERED)) }
                        }

                        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable?) {
                            Log.e(TAG, "onFailure: failed to send", exception)
                            writeToDb { messageDao.update(dqttMessage.copy(id = id, status = DqttMessage.MessageStatus.FAILED)) }
                        }
                    })
                } catch (e: MqttException) {
                    Log.e(TAG, "sendMessage: failed to send - " + e.message, e)
                    writeToDb { messageDao.update(dqttMessage.copy(id = id, status = DqttMessage.MessageStatus.FAILED)) }
                }
            }
        }
    }


    /**
     * Subscribe to rxJava Room Database task with the common observer callbacks.
     *
     * @param task Kotlin Coroutine task from Room Dao
     */
    private fun writeToDb(task: suspend () -> Unit) {
        CoroutineScope(IO).launch {
            task.invoke()
        }
    }

    fun observeDeviceInfo(device: Device): LiveData<InfoMessage> {
        return observeDeviceInfo(device.username)
    }

    fun observeDeviceInfo(deviceUsername: String): LiveData<InfoMessage> {
        return deviceInfoMessages.observe(deviceUsername)
    }

    fun observeDeviceLocation(device: Device): LiveData<LocationMessage> {
        return deviceLocationMessages.observe(device.username)
    }
}