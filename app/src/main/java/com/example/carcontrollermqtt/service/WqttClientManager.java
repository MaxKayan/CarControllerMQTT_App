package com.example.carcontrollermqtt.service;

import android.content.Context;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.DeviceDao;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.WqttClient;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class WqttClientManager {
    public static final String SERVER_URI = "wss://wqtt.ru:6618/";
    private static final String TAG = "WqttClient";
    private static WqttClientManager instance;

    private Context context;
    private AppDatabase database;
    private DeviceDao deviceDao;

    // Device username is the key
    private Map<String, WqttClient> deviceClients = new HashMap<>();

    private WqttClientManager(Context appContext) {
        context = appContext;
        database = AppDatabase.getInstance(context);
        deviceDao = database.deviceDao();
    }

    public static WqttClientManager getInstance(Context context) {
        if (instance != null)
            return instance;

        return new WqttClientManager(context);
    }

    public void postDeviceList(List<Device> devices) {
        Set<String> knownDeviceKeys = new HashSet<>();

        for (Device device : devices) {
            String key = device.getUsername();
            knownDeviceKeys.add(key);

            if (!deviceClients.containsKey(key)) {
                deviceClients.put(key, createClientForDevice(device));
            } else {
                WqttClient client = deviceClients.get(key);
                if (client != null) {
                    handleClientConnection(client);
                }
            }
        }

        cleanDeletedDevices(knownDeviceKeys);
    }

//    private void subscribeToData() {
//        deviceDao.observeDevices().observe(activity, devices -> {
//
//        });
//    }

    @SuppressWarnings("ConstantConditions")
    private void cleanDeletedDevices(Set<String> knownKeys) {
        for (String key : deviceClients.keySet()) {
            if (!knownKeys.contains(key)) {
                WqttClient oldClient = deviceClients.get(key);
                oldClient.disconnect();
                deviceClients.remove(key);
            }
        }
    }

    private void handleClientConnection(WqttClient wqtt) {
        boolean deviceEnabled = wqtt.getDevice().isEnabled();
        boolean mqttConnected = wqtt.getClient().isConnected();

        if (deviceEnabled && !mqttConnected) {
            wqtt.connect();
        } else if (!deviceEnabled && mqttConnected) {
            wqtt.disconnect();
        }
    }

    private WqttClient createClientForDevice(Device device) {
        MqttAndroidClient client = new MqttAndroidClient(context, SERVER_URI, "wsc_" + new Random().nextInt(100));
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(device.getUsername());
        options.setPassword(device.getPassword().toCharArray());

        WqttClient wqtt = new WqttClient(device, options, client);
        handleClientConnection(wqtt);
        return wqtt;
    }

}
