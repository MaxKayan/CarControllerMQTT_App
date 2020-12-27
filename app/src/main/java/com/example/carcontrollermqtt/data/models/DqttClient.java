package com.example.carcontrollermqtt.data.models;

import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class DqttClient {
    private static final String TAG = "WqttClient";
    private final Device device;
    private final MqttConnectOptions options;
    private final MqttAndroidClient client;
    private final DqttClientAction callbacks;

    public interface DqttClientAction {
        void connect(Device device, MqttAndroidClient client, MqttConnectOptions options);
    }

    public DqttClient(Device device, MqttConnectOptions options, MqttAndroidClient client, DqttClientAction callbacks) {
        this.device = device;
        this.options = options;
        this.client = client;
        this.callbacks = callbacks;
    }

    private void subscribeToTopics() throws MqttException {
        client.subscribe("#", 0);
    }

    public void connect() {
        callbacks.connect(device, client, options);
    }

    public void disconnect() {
        if (!client.isConnected()) {
            Log.d(TAG, "disconnect: client is already disconnected");
            return;
        }

        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public Device getDevice() {
        return device;
    }

    public MqttConnectOptions getOptions() {
        return options;
    }

    public MqttAndroidClient getClient() {
        return client;
    }
}
