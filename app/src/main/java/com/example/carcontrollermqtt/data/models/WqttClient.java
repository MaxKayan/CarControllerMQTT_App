package com.example.carcontrollermqtt.data.models;

import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class WqttClient {
    private static final String TAG = "WqttClient";
    private Device device;
    private MqttConnectOptions options;
    private MqttAndroidClient client;

    public WqttClient(Device device, MqttConnectOptions options, MqttAndroidClient client) {
        this.device = device;
        this.options = options;
        this.client = client;

        setClientCallbacks();
    }

    private void setClientCallbacks() {
        this.client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.w(TAG, "connectionLost: " + device.getUsername(), cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "messageArrived: " + device.getUsername() + " - " + topic + " - " + message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "deliveryComplete: " + device.getUsername());
            }
        });
    }

    private void subscribeToTopics() throws MqttException {
        client.subscribe("#", 1);
    }

    public void connect() {
        Log.d(TAG, "connect: " + device.getUsername());
        try {
            client.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "onSuccess: " + device.getUsername());
                    try {
                        subscribeToTopics();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "onFailure: " + device.getUsername(), exception);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        Log.d(TAG, "disconnect: " + device.getUsername());
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
