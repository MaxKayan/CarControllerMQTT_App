package com.example.carcontrollermqtt.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.WqttMessageDao;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.DeviceEvent;
import com.example.carcontrollermqtt.data.models.WqttClient;

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

public class WqttClientManager {
    public static final String SERVER_URI = "wss://wqtt.ru:6618/";
    private static final String TAG = "WqttClientManager";
    private static WqttClientManager instance;

    // TODO: A way to avoid passing context to this singleton?
    private final Context context;
    //    private AppDatabase database;
//    private DeviceDao deviceDao;
    private final WqttMessageDao messageDao;

    private final WqttClientDiffUtil deviceListManager;
    private final WqttClientEventBus eventBus;
    private final WqttMessageManager messageManager;

    // Device username is the key
    private final Map<String, WqttClient> wqttClientsMap = new HashMap<>();

    private WqttClientManager(Context appContext) {
        context = appContext.getApplicationContext();
        eventBus = WqttClientEventBus.getInstance();
        messageDao = AppDatabase.getInstance(appContext).messageDao();
        messageManager = WqttMessageManager.getInstance(appContext);
        deviceListManager = new WqttClientDiffUtil(new WqttClientDiffUtil.WqttClientCallbacks() {
            @Override
            public void initiateDevice(Device device) {
                if (device.isEnabled())
                    wqttClientsMap.put(device.getUsername(), createClientForDevice(device));
            }

            @Override
            public void removeDevice(Device device) {
                String key = device.getUsername();
                WqttClient wqttClient = getWqttClient(device);
                if (wqttClient != null) {
                    wqttClient.disconnect();
                    wqttClientsMap.remove(key);
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

    public WqttClient getWqttClient(Device device) {
        return getWqttClient(device.getUsername());
    }

    @Nullable
    public WqttClient getWqttClient(String username) {
            return wqttClientsMap.get(username);
    }

    public void postDeviceList(List<Device> devices) {
        deviceListManager.submitList(devices);
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
                Toast.makeText(context, topic + " - " + message.toString(), Toast.LENGTH_LONG).show();

                messageManager.receiveMessage(device, topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "deliveryComplete: " + device.getUsername());
            }
        });

        WqttClient wqtt = new WqttClient(device, options, client, (connDevice, connClient, connOptions) -> {
            eventChannel.setValue(DeviceEvent.pending(device, null));
            Log.i(TAG, "createClientForDevice: Connecting... - " + device.getUsername());
            try {
                connClient.connect(options, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "onSuccess: connected " + device.getUsername());
                        try {
                            client.subscribe("#", 0);
                            eventChannel.postValue(DeviceEvent.connected(device, null));
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
        handleClientConnection(wqtt);
        return wqtt;
    }

    private interface DeviceMapAction {
        Map<String, WqttClient> run(Map<String, WqttClient> deviceMap);
    }

}
