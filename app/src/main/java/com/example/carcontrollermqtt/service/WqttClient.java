package com.example.carcontrollermqtt.service;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Arrays;

public class WqttClient {
    public static final String SERVER_URI = "wss://wqtt.ru:6618/";
    private static final String TAG = "WqttClient";
    private static WqttClient instance;

    private MqttAndroidClient mqqtAndroidClient;

    private WqttClient(Context context) {
        mqqtAndroidClient = new MqttAndroidClient(context, SERVER_URI, "wsc_77");
        mqqtAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.w(TAG, "connectionLost: " + cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "messageArrived: " + topic + " - " + message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "deliveryComplete: " + token);
            }
        });
    }

    public static WqttClient getInstance(Context context) {
        if (instance != null)
            return instance;

        return new WqttClient(context);
    }

    public void connect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("u_LE8XSH");
        options.setPassword("0XL5EgyF".toCharArray());

        try {
            mqqtAndroidClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "onSuccess: connected " + asyncActionToken);
                    try {
                        mqqtAndroidClient.subscribe("#", 1);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "onFailure: connection failed", exception);
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
