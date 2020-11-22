package com.example.carcontrollermqtt.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.WqttMessageDao;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.WqttClient;
import com.example.carcontrollermqtt.data.models.WqttMessage;
import com.example.carcontrollermqtt.data.models.messages.InfoMessage;
import com.example.carcontrollermqtt.data.models.messages.LocationMessage;
import com.example.carcontrollermqtt.utils.LiveDataMap;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Date;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WqttMessageManager {
    private static final String TAG = "WqttMessageManager";

    // Common rxJava observer callbacks meant to catch exceptions
    private static final CompletableObserver writeToDbObserver = new CompletableObserver() {
        @Override
        public void onSubscribe(@NonNull Disposable d) {

        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Log.e(TAG, "onError: failed to write the new message", e);
        }
    };

    // Singleton instance
    private static WqttMessageManager instance;
    // App database
    private final AppDatabase database;
    private final WqttMessageDao messageDao;

    private final Gson gson;

    // Client manager to get MqttClient based on device name
    private final WqttClientManager clientManager;

    private final LiveDataMap<String, InfoMessage> deviceInfoMessages = new LiveDataMap<>();
    private final LiveDataMap<String, LocationMessage> deviceLocationMessages = new LiveDataMap<>();

    // Main private constructor to be called from public static function
    private WqttMessageManager(Context context, WqttClientManager clientManager) {
        this.database = AppDatabase.getInstance(context);
        this.messageDao = database.messageDao();
        this.clientManager = clientManager;
        this.gson = GsonProvider.getGsonInstance();
    }

    /**
     * @param context       App context
     * @param clientManager Instantiated singleton. Needed to get client connections from
     * @return Singleton instance
     */
    public static WqttMessageManager getInstance(Context context, WqttClientManager clientManager) {
        if (instance == null) {
            instance = new WqttMessageManager(context, clientManager);
        }

        return instance;
    }

    public static String qualifyTopic(Device device, String endpointTopic) {
        return String.format("data/%s/%s", device.getDeviceId(), endpointTopic);
    }

    public static String getEndpointTopic(@NonNull String fullTopic) {
        return fullTopic.substring(fullTopic.indexOf("/", fullTopic.indexOf("/") + 1) + 1);
    }

    /**
     * @param device  Which device received the message
     * @param fullTopic   Message fullTopic
     * @param message Message object from client callback
     */
    public void receiveMessage(Device device, String fullTopic, MqttMessage message) {
        writeToDb(
                messageDao.insert(WqttMessage.newInstance(device.getId(), message.getId(), new Date(), true, fullTopic, message.toString()))
        );

        final String endpointTopic = getEndpointTopic(fullTopic);

        switch (endpointTopic) {
            case "info":
                Log.d(TAG, "receiveMessage: info " + message.toString());
                deviceInfoMessages.set(device.getUsername(), serializeMessage(message.toString(), InfoMessage.class));
                break;

            case "location":
                Log.d(TAG, "receiveMessage: location" + message);
                deviceLocationMessages.set(device.getUsername(), serializeMessage(message.toString(), LocationMessage.class));
                break;

            default:
                Log.w(TAG, "receiveMessage: unknown fullTopic! - " + fullTopic);
        }
    }


    @Nullable
    private <T> T serializeMessage(String payload, Class<T> messageClass) {
        T result = null;
        try {
            result = gson.fromJson(payload, messageClass);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "receiveMessage: failed to parse", e);
        }

        return result;
    }


    /**
     * Send new message from the current selected {@link Device device}
     *
     * @param endpointTopic   Message topic
     * @param payload Payload string
     */
    public void sendMessage(String endpointTopic, String payload) {
        Device selectedDevice = WqttClientManager.getSelectedDevice();
        Log.d(TAG, "sendMessage: selected device " + selectedDevice);
        if (selectedDevice != null) {
            sendMessage(selectedDevice, endpointTopic, payload);
        }
    }

    /**
     * Send new message from the specified {@link Device device}
     *
     * @param device  Device to send message from
     * @param endpointTopic   Message topic
     * @param payload Payload string
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void sendMessage(Device device, String endpointTopic, String payload) {
        WqttClient client = clientManager.getWqttClient(device.getUsername());
        if (client != null) {
            final String fullTopic = qualifyTopic(device, endpointTopic);

            MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
            WqttMessage wqttMessage = WqttMessage.newInstance(device.getId(), mqttMessage.getId(), new Date(), false, fullTopic, payload);
            messageDao.insertAndReadId(wqttMessage)
                    .subscribeOn(Schedulers.io())
                    .subscribe((id) -> {
                        Log.d(TAG, "sendMessage: written and value is - " + id);
                        try {
                            client.getClient().publish(fullTopic, new MqttMessage(payload.getBytes()), null, new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.d(TAG, "onSuccess: updating msg status");
                                    writeToDb(
                                            messageDao.update(wqttMessage.cloneAndUpdate(id, WqttMessage.MessageStatus.DELIVERED))
                                    );
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    Log.e(TAG, "onFailure: failed to send", exception);
                                    writeToDb(
                                            messageDao.update(wqttMessage.cloneAndUpdate(id, WqttMessage.MessageStatus.FAILED))
                                    );
                                }
                            });
                        } catch (MqttException e) {
                            Log.e(TAG, "sendMessage: failed to send - " + e.getMessage(), e);
                            writeToDb(
                                    messageDao.update(wqttMessage.cloneAndUpdate(id, WqttMessage.MessageStatus.FAILED))
                            );
                        }
                    }, throwable -> {
                        Log.e(TAG, "sendMessage: failed to write to DB", throwable);
                    });

        }
    }

    /**
     * Subscribe to rxJava Room Database task with the common observer callbacks.
     *
     * @param task RxJava Completable task from Room Dao
     */
    private void writeToDb(Completable task) {
        task.subscribeOn(Schedulers.io()).subscribe(writeToDbObserver);
    }

    public LiveData<InfoMessage> observeDeviceInfo(Device device) {
        return observeDeviceInfo(device.getUsername());
    }

    public LiveData<InfoMessage> observeDeviceInfo(String deviceUsername) {
        return deviceInfoMessages.observe(deviceUsername);
    }

    public LiveData<LocationMessage> observeDeviceLocation(Device device) {
        return deviceLocationMessages.observe(device.getUsername());
    }
}
