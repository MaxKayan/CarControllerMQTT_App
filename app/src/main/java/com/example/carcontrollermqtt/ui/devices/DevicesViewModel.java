package com.example.carcontrollermqtt.ui.devices;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.DeviceDao;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.DeviceEvent;
import com.example.carcontrollermqtt.service.WqttClientEventBus;
import com.example.carcontrollermqtt.service.WqttClientManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("ResultOfMethodCallIgnored")
@SuppressLint("CheckResult")
public class DevicesViewModel extends AndroidViewModel {
    private static final String TAG = "DevicesViewModel";

    private final AppDatabase database;
    private final DeviceDao deviceDao;

    private final Map<String, DeviceEvent> lastDeviceEvents = new HashMap<>();

    private final WqttClientManager wqttClientManager;

    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MediatorLiveData<List<Device>> devicesViewMerger = new MediatorLiveData<>();

    public DevicesViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        deviceDao = database.deviceDao();
        wqttClientManager = WqttClientManager.getInstance(application);

        WqttClientEventBus eventBus = WqttClientEventBus.getInstance();

        devicesViewMerger.addSource(deviceDao.observeDevices(), devices -> {
            List<Device> newList = devices.stream().map(device -> {
                String key = device.getUsername();
                if (!lastDeviceEvents.containsKey(key)) {
                    lastDeviceEvents.put(key, null);

                    Log.d(TAG, "DevicesViewModel: new device, adding source");
                    devicesViewMerger.addSource(eventBus.getChannel(key), getEventBusObserver(key, device));

                } else {
                    Log.d(TAG, "DevicesViewModel: key exists, setting to device - " + lastDeviceEvents.get(key));
                    device.setEvent(lastDeviceEvents.get(key));
                }

                return device;
            }).collect(Collectors.toList());

            devicesViewMerger.setValue(newList);
        });
    }

    private Observer<DeviceEvent> getEventBusObserver(String key, Device device) {
        return deviceEvent -> {
            lastDeviceEvents.put(key, deviceEvent);
            Log.i(TAG, "getEventBusObserver: event value in map - " + lastDeviceEvents.get(key));

            Log.d(TAG, "DevicesViewModel: new event arrived - " + deviceEvent);
            List<Device> currentList = devicesViewMerger.getValue();
            if (currentList != null) {
                List<Device> newList = currentList.stream().map(item -> {
                    if (item.getId() == device.getId()) item.setEvent(deviceEvent);
                    return item;
                }).collect(Collectors.toList());

                Log.i(TAG, "DevicesViewModel: putting new event " + newList.get(0).getEvent());
                devicesViewMerger.setValue(newList);
            }
        };
    }

    LiveData<String> observeMessages() {
        return message;
    }

    public LiveData<List<Device>> observeDevices() {
        return devicesViewMerger;
//        return deviceDao.observeDevices();
    }

    void deleteDevice(Device device) {
        deviceDao.deleteDevice(device)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Log.d(TAG, "deleteDevice: Device deleted");
                    message.postValue("Устройство " + device.getUsername() + " удалено");
                });
    }

    void selectDevice(Device device) {
        deviceDao.getDevices()
                .subscribeOn(Schedulers.io())
                .subscribe(devices -> {
                    deviceDao.updateDeviceList(devices.stream()
                            .map(item -> item.getId() == device.getId() ? item.cloneWithSelected(true) : item.cloneWithSelected(false))
                            .collect(Collectors.toList())
                    )
                            .subscribe(() -> {
                                Log.d(TAG, "deselectAllDevices: Succes");
                            });
                });
    }

    void setEnabledOnDevice(boolean enabled, Device device) {
        deviceDao.updateDevice(device.cloneWithEnabled(enabled))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Log.d(TAG, "setEnabledOnDevice: Success");
                    message.postValue("Связь с устройством " + device.getUsername() + (enabled ? " включена" : " отключена"));
                });
    }
}