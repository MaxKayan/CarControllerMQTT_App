package com.example.carcontrollermqtt.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.WqttClient;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WqttClientManager {
    public static final String SERVER_URI = "wss://wqtt.ru:6618/";
    private static final String TAG = "WqttClientManager";
    private static WqttClientManager instance;

    // TODO: A way to avoid passing context to this singleton?
    private Context context;
//    private AppDatabase database;
//    private DeviceDao deviceDao;

    private WqttClientDiffUtil deviceListManager;

    // Device username is the key
    private MutableLiveData<Map<String, WqttClient>> wqttClientsLD = new MutableLiveData<>(new HashMap<>());

    private WqttClientManager(Context appContext) {
        context = appContext.getApplicationContext();
//        database = AppDatabase.getInstance(context);
//        deviceDao = database.deviceDao();
        deviceListManager = new WqttClientDiffUtil(new WqttClientDiffUtil.WqttClientCallbacks() {
            @Override
            public void initiateDevice(Device device) {
                if (device.isEnabled())
                    editDeviceMapLiveData(deviceMap -> {
                        deviceMap.put(device.getUsername(), createClientForDevice(device));
                        return deviceMap;
                    });
            }

            @Override
            public void removeDevice(Device device) {
                String key = device.getUsername();
                WqttClient wqttClient = getWqttClient(device);
                if (wqttClient != null) {
                    wqttClient.disconnect();

                    editDeviceMapLiveData(deviceMap -> {
                        deviceMap.remove(key);
                        return deviceMap;
                    });
                }
            }
        });
    }

    public static WqttClientManager getInstance(Context context) {
        if (instance == null) {
            synchronized (WqttClientManager.class) {
                instance = new WqttClientManager(context);
            }
        }

        return instance;
    }

    @Nullable
    public WqttClient getWqttClient(@NonNull Device device) {
        Map<String, WqttClient> deviceMap = wqttClientsLD.getValue();
        if (deviceMap != null) {
            return deviceMap.get(device.getUsername());
        }

        return null;
    }

    private void editDeviceMapLiveData(DeviceMapAction action) {
        Map<String, WqttClient> deviceMap = wqttClientsLD.getValue();
        if (deviceMap != null) {
            wqttClientsLD.setValue(action.run(deviceMap));
        }
    }

    public void postDeviceList(List<Device> devices) {
        deviceListManager.submitList(devices);
//        Log.d(TAG, "postDeviceList: new list posted - " + devices);
//        Set<String> knownDeviceKeys = new HashSet<>();
//
//        for (Device device : devices) {
//            String key = device.getUsername();
//            knownDeviceKeys.add(key);
//            Log.d(TAG, "postDeviceList: processing - " + key);
//
//            if (!deviceClients.containsKey(key)) {
//                Log.d(TAG, "postDeviceList: key is new, creating new client");
//                deviceClients.put(key, createClientForDevice(device));
//            } else {
//                Log.d(TAG, "postDeviceList: key's already in the map");
//                WqttClient client = deviceClients.get(key);
//                if (client != null) {
//                    client.disconnect();
//
//                    deviceClients.remove(key);
//                    deviceClients.put(key, createClientForDevice(device));
//                }
//            }
//        }
//
//        cleanDeletedDevices(knownDeviceKeys);
//        Log.d(TAG, "postDeviceList: map after cleanup - " + deviceClients.keySet().toString());
//        deviceClients.forEach((s, wqttClient) -> {
//            Log.d(TAG, "postDeviceList: " + s + "/n " + wqttClient);
//        });
    }

    private void handleClientConnection(WqttClient wqtt) {
        boolean deviceEnabled = wqtt.getDevice().isEnabled();
        boolean mqttConnected = wqtt.getClient().isConnected();
//        Log.d(TAG, "handleClientConnection: processing - " + wqtt.getDevice().getUsername() +
//                "/n enabled - " + deviceEnabled + " : connected - " + mqttConnected);

        if (deviceEnabled && !mqttConnected) {
//            Log.d(TAG, "handleClientConnection: device is enabled but not connected - connecting...");
            wqtt.connect();
        } else if (!deviceEnabled && mqttConnected) {
//            Log.d(TAG, "handleClientConnection: device is disabled but connected - disconnecting...");
            wqtt.disconnect();
        }
    }

    private WqttClient createClientForDevice(Device device) {
        MqttAndroidClient client = new MqttAndroidClient(context, SERVER_URI, "wsc_" + new Random().nextInt(99));
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(device.getUsername());
        options.setPassword(device.getPassword().toCharArray());

        WqttClient wqtt = new WqttClient(device, options, client);
        handleClientConnection(wqtt);
        return wqtt;
    }

    private interface DeviceMapAction {
        Map<String, WqttClient> run(Map<String, WqttClient> deviceMap);
    }

}
