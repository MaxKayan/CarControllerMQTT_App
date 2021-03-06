package com.example.carcontrollermqtt.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.DeviceDao;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.DeviceEvent;
import com.example.carcontrollermqtt.data.models.DqttClient;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DqttClientManager {
    public static final String SERVER_URI = "wss://dqtt.tk:8083/";
    private static final String TAG = "WqttClientManager";
    private static DqttClientManager instance;
    private static Device selectedDevice;

    // TODO: A way to avoid passing context to this singleton?
    private final Context context;
    private final DeviceDao deviceDao;

    private final DqttClientDiffUtil deviceListManager;
    private final DqttClientEventBus eventBus;
    private final DqttMessageManager messageManager;

    // Device username is the key
    private final Map<String, DqttClient> dqttClientsMap = new HashMap<>();

    private DqttClientManager(Context appContext) {
        context = appContext.getApplicationContext();
        eventBus = DqttClientEventBus.getInstance();
        deviceDao = AppDatabase.getInstance(appContext).deviceDao();
        messageManager = DqttMessageManager.getInstance(appContext, this);
        deviceListManager = new DqttClientDiffUtil(new DqttClientDiffUtil.DqttClientCallbacks() {
            @Override
            public void initiateDevice(Device device) {
                if (device.getEnabled())
                    dqttClientsMap.put(device.getUsername(), createClientForDevice(device));
            }

            @Override
            public void removeDevice(Device device) {
                String key = device.getUsername();
                DqttClient dqttClient = getDqttClient(device);
                if (dqttClient != null) {
                    dqttClient.disconnect();
                    dqttClientsMap.remove(key);
                }
            }
        });

        // Observe selected device from the Room database for the whole app lifetime, and cache it for the easy access.
        deviceDao.observeSelectedDevice().observeForever(device -> {
            selectedDevice = device;
        });
    }

    @Nullable
    public static Device getSelectedDevice() {
        return selectedDevice;
    }

    public static DqttClientManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DqttClientManager.class) {
                instance = new DqttClientManager(context);
            }
        }

        return instance;
    }

    public void refreshMainInfo() {
        if (selectedDevice != null) {
            selectedDevice.requestInfo(messageManager);
        }
    }

    public void refreshMainLocation() {
        if (selectedDevice != null) {
            selectedDevice.requestLocation(messageManager);
        }
    }

    public DqttMessageManager getMessageManager() {
        return messageManager;
    }

    public DqttClient getDqttClient(Device device) {
        return getDqttClient(device.getUsername());
    }

    @Nullable
    public DqttClient getDqttClient(String username) {
        return dqttClientsMap.get(username);
    }

    public void submitDeviceList(List<Device> devices) {
        deviceListManager.submitList(devices);
    }

    private void handleClientConnection(DqttClient wqtt) {
        boolean deviceEnabled = wqtt.getDevice().getEnabled();
        boolean mqttConnected = wqtt.getClient().isConnected();

        if (deviceEnabled && !mqttConnected) {
            wqtt.connect();
        } else if (!deviceEnabled && mqttConnected) {
            wqtt.disconnect();
        }
    }

    private DqttClient createClientForDevice(Device device) {
        MqttAndroidClient client = new MqttAndroidClient(context, SERVER_URI, "wsc_" + new Random().nextInt(99));
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(device.getUsername());
        options.setPassword(device.getPassword().toCharArray());

        MutableLiveData<DeviceEvent> eventChannel = eventBus.getChannel(device.getUsername());

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.w(TAG, "connectionLost: " + device.getUsername(), cause);
                eventChannel.postValue(DeviceEvent.disconnected(device, cause == null ? null : cause.getMessage()));
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "messageArrived: " + device + " - " + topic + " - " + message.toString());
//                Toast.makeText(context, topic + " - " + message.toString(), Toast.LENGTH_LONG).show();
                messageManager.receiveMessage(device, topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "deliveryComplete: " + device.getUsername());
            }
        });

        DqttClient dqtt = new DqttClient(device, options, client, (connDevice, connClient, connOptions) -> {
            eventChannel.setValue(DeviceEvent.pending(device, null));
            Log.i(TAG, "createClientForDevice: Connecting... - " + device.getUsername());
            try {
                connClient.connect(options, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "onSuccess: connected " + device.getUsername());
                        try {
                            // Subscribe client to correct topic!
                            client.subscribe(String.format("data/%s/#", device.getDeviceId()), 0);
                            eventChannel.postValue(DeviceEvent.connected(device, null));
                            device.requestInfo(messageManager);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w(TAG, "onFailure: failed mqtt " + device.getUsername(), exception);
                        eventChannel.postValue(DeviceEvent.error(device, exception.getLocalizedMessage()));
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
        handleClientConnection(dqtt);
        return dqtt;
    }

}
