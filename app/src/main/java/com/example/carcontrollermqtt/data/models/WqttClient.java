package com.example.carcontrollermqtt.data.models;

import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class WqttClient {
    private static final String TAG = "WqttClient";
    private Device device;
    private MqttConnectOptions options;
    private MqttAndroidClient client;
    private WqttClientAction callbacks;

    public interface WqttClientAction {
        void connect(Device device, MqttAndroidClient client, MqttConnectOptions options);
    }

    public WqttClient(Device device, MqttConnectOptions options, MqttAndroidClient client, WqttClientAction callbacks) {
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
//        Log.i(TAG, "connect: " + device.getUsername());
//        try {
//            client.connect(options, null, mqttActionListener);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
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
