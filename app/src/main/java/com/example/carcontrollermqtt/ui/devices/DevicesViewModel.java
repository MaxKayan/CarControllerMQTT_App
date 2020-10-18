package com.example.carcontrollermqtt.ui.devices;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.EmptyResultSetException;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.DeviceDao;
import com.example.carcontrollermqtt.data.models.Device;

import java.util.List;

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

    void selectDevice(Device device) {
        Device updatedDevice = new Device(device.getId(), device.isActive(), device.getUsername(),
                device.getPassword(), device.getKeepAlive());
        updatedDevice.setActive(true);
        deviceDao.getSelectedDevice()
                .subscribeOn(Schedulers.io())
                .subscribe(activeDevice -> {
                    activeDevice.setActive(false);
                    deviceDao.updateDevices(new Device[]{activeDevice, updatedDevice})
                            .subscribe(() -> {
                                Log.d(TAG, "selectDevice: Success!");
                            });
                }, throwable -> {
                    if (throwable instanceof EmptyResultSetException) {
                        deviceDao.updateDevice(updatedDevice).subscribe(() -> {
                            Log.d(TAG, "selectDevice: Success");
                        });
                    } else Log.e(TAG, "selectDevice: Failed", throwable);
                });
    }
}