package com.example.carcontrollermqtt.ui.devices;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.DeviceDao;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.WqttClient;
import com.example.carcontrollermqtt.service.WqttClientManager;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("ResultOfMethodCallIgnored")
@SuppressLint("CheckResult")
public class DevicesViewModel extends AndroidViewModel {
    private static final String TAG = "DevicesViewModel";

    private AppDatabase database;
    private DeviceDao deviceDao;

    private WqttClientManager wqttClientManager;

    private MutableLiveData<String> message = new MutableLiveData<>();
    private MediatorLiveData<List<Device>> devicesViewMerger = new MediatorLiveData<>();

    public DevicesViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        deviceDao = database.deviceDao();
        wqttClientManager = WqttClientManager.getInstance(application);

        devicesViewMerger.addSource(deviceDao.observeDevices(), devices -> {
//            devices.stream().map(device -> {
//                WqttClient wqttClient = wqttClientManager.getWqttClient(device);
//                if (wqttClient != null) {
//                    device.setUp(wqttClient.getClient().isConnected());
//                } else {
//                    device.setUp(false);
//                }
//                return device;
//            }).collect(Collectors.toList());
//
//            devicesViewMerger.setValue(devices);
//            Log.d(TAG, "DevicesViewModel: set list with state - " + devices.get(0).isUp());
        });

//        devicesViewMerger.addSource(wqttClientManager.observeWqttClients(), wqttClientMap -> {
//            List<Device> currentList = devicesViewMerger.getValue();
//            if (currentList == null) return;
//
//            for (Device device : currentList) {
//                WqttClient wqttClient = wqttClientMap.get(device.getUsername());
//                if (wqttClient != null) {
//                    device.setUp(wqttClient.getClient().isConnected());
//                } else {
//                    device.setUp(false);
//                }
//            }
//
//            devicesViewMerger.setValue(currentList);
//            Log.d(TAG, "DevicesViewModel: set list with state - " + currentList.get(0).isUp());
//        });
    }

    LiveData<String> observeMessages() {
        return message;
    }

    public LiveData<List<Device>> observeDevices() {
//        return devicesViewMerger;
        return deviceDao.observeDevices();
    }

    void saveDevice(Device device) {
        deviceDao.insertDevice(device)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Log.d(TAG, "saveDevice: Saved");
                });
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