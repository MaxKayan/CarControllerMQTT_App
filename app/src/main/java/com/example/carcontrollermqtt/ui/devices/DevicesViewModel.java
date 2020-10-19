package com.example.carcontrollermqtt.ui.devices;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.DeviceDao;
import com.example.carcontrollermqtt.data.models.Device;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("ResultOfMethodCallIgnored")
@SuppressLint("CheckResult")
public class DevicesViewModel extends AndroidViewModel {
    private static final String TAG = "DevicesViewModel";

    private AppDatabase database;
    private DeviceDao deviceDao;

    private MutableLiveData<String> message = new MutableLiveData<>();

    public DevicesViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        deviceDao = database.deviceDao();
    }

    LiveData<String> observeMessages() {
        return message;
    }

    public LiveData<List<Device>> observeDevices() {
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

    private void deselectAllDevices() {
        deviceDao.getDevices()
                .subscribeOn(Schedulers.io())
                .subscribe(devices -> {
                    deviceDao.updateDeviceList(
                            devices.stream().map(device -> device.cloneWithSelected(false)).collect(Collectors.toList())
                    )
                            .subscribe(() -> {
                                Log.d(TAG, "deselectAllDevices: Succes");
                            });
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

//        deviceDao.getSelectedDevice()
//                .subscribeOn(Schedulers.io())
//                .subscribe(activeDevice -> {
//                    deviceDao.updateMultipleDevices(activeDevice.cloneWithSelected(false), updatedDevice)
//                            .subscribe(() -> {
//                                Log.d(TAG, "selectDevice: Success!");
//                            });
//                }, throwable -> {
//                    if (throwable instanceof EmptyResultSetException) {
//                        deviceDao.updateDevice(updatedDevice).subscribe(() -> {
//                            Log.d(TAG, "selectDevice: Success");
//                        });
//                    } else Log.e(TAG, "selectDevice: Failed", throwable);
//                });
    }

    void setEnabledOnDevice(boolean enabled, Device device) {
        deviceDao.updateDevice(device.cloneWithEnabled(enabled))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Log.d(TAG, "setEnabledOnDevice: Success");
//                    message.postValue("Связь с устройством " + device.getUsername() + (enabled ? " включена" : " отключена"));
                });
    }
}