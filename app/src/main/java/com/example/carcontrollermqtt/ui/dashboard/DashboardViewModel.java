package com.example.carcontrollermqtt.ui.dashboard;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.DeviceDao;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.messages.InfoMessage;
import com.example.carcontrollermqtt.data.models.messages.LocationMessage;
import com.example.carcontrollermqtt.service.DqttClientManager;
import com.example.carcontrollermqtt.service.DqttMessageManager;

@SuppressLint("CheckResult")
public class DashboardViewModel extends AndroidViewModel {
    private static final String TAG = "DevicesViewModel";

    private final DqttMessageManager messageManager;
    private final DqttClientManager clientManager;
    private final DeviceDao deviceDao;
    private Device currentDevice;

    public DashboardViewModel(@NonNull Application application) {
        super(application);

        this.deviceDao = AppDatabase.getInstance(application).deviceDao();
        this.clientManager = DqttClientManager.getInstance(application);
        this.messageManager = clientManager.getMessageManager();
    }

    public LiveData<InfoMessage> observeInfo(LifecycleOwner owner, Device device) {
        if (currentDevice != null) {
            messageManager.observeDeviceInfo(currentDevice).removeObservers(owner);
        }
        currentDevice = device;
        return messageManager.observeDeviceInfo(device);
    }

    public LiveData<LocationMessage> observeLocation(LifecycleOwner owner, Device device) {
        if (currentDevice != null) {
            messageManager.observeDeviceLocation(currentDevice).removeObservers(owner);
        }
        currentDevice = device;
        return messageManager.observeDeviceLocation(device);
    }

    public void refreshDeviceInfo() {
        clientManager.refreshMainInfo();
    }

    public void refreshDeviceLocation() {
        clientManager.refreshMainLocation();
    }

    public LiveData<Device> observeSelectedDevice() {
        return deviceDao.observeSelectedDevice();
    }

}