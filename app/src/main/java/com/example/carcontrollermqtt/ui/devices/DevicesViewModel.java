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
import com.example.carcontrollermqtt.service.DqttClientEventBus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("ResultOfMethodCallIgnored")
@SuppressLint("CheckResult")
public class DevicesViewModel extends AndroidViewModel {
    private static final String TAG = "DevicesViewModel";

    private final DeviceDao deviceDao;

    //    private final Map<String, DeviceEvent> lastDeviceEvents = new HashMap<>();
    private final Set<String> knownDevices = new HashSet<>();

    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MediatorLiveData<List<Device>> devicesViewMerger = new MediatorLiveData<>();

    public DevicesViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        deviceDao = database.deviceDao();

        DqttClientEventBus eventBus = DqttClientEventBus.getInstance();

        // TODO: is this system over-engineered and i should instead just write device status enum to Room database, cleaning all states upon launch?
        devicesViewMerger.addSource(deviceDao.observeAll(), devices -> {
            List<Device> newList = devices.stream().peek(device -> {
                String key = device.getUsername();
                if (!knownDevices.contains(key)) {
                    knownDevices.add(key);

                    Log.d(TAG, "DevicesViewModel: new device, adding source");
                    devicesViewMerger.addSource(eventBus.getChannel(key), getEventBusObserver(key, device));
                }

                device.setEvent(eventBus.getLastEventForDevice(device));

                Log.d(TAG, "DevicesViewModel: set event from saved - " + device.getUsername() + " - " + device.getEvent());
            }).collect(Collectors.toList());

            devicesViewMerger.setValue(newList);
        });
    }

    private Observer<DeviceEvent> getEventBusObserver(String key, Device device) {
        return deviceEvent -> {
            Log.d(TAG, "DevicesViewModel: new event arrived - " + deviceEvent);
            List<Device> currentList = devicesViewMerger.getValue();
            if (currentList != null) {
                List<Device> newList = currentList.stream().peek(item -> {
                    if (item.getId().equals(device.getId())) item.setEvent(deviceEvent);
                }).collect(Collectors.toList());

                devicesViewMerger.setValue(newList);
            }
        };
    }

    public LiveData<String> observeMessages() {
        return message;
    }

    public LiveData<List<Device>> observeDevices() {
        return devicesViewMerger;
//        return deviceDao.observeDevices();
    }

    public void deleteDevice(Device device) {
        deviceDao.delete(device)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Log.d(TAG, "deleteDevice: Device deleted");
                    message.postValue("Устройство " + device.getUsername() + " удалено");
                }, throwable -> {
                    Log.e(TAG, "deleteDevice: Failed to delete the device - ", throwable);
                });
    }

    public void selectDevice(Device device) {
        deviceDao.getAll()
                .subscribeOn(Schedulers.io())
                .subscribe(devices -> {
                    deviceDao.updateAll(devices.stream()
                            .map(item -> item.getId().equals(device.getId()) ? item.cloneWithSelected(true) : item.cloneWithSelected(false))
                            .collect(Collectors.toList())
                    )
                            .subscribe(() -> {
                                Log.d(TAG, "deselectAllDevices: Succes");
                            });
                });
    }

    public void setEnabledOnDevice(boolean enabled, Device device) {
        deviceDao.update(device.cloneWithEnabled(enabled))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Log.d(TAG, "setEnabledOnDevice: Success");
//                    message.postValue("Связь с устройством " + device.getUsername() + (enabled ? " включена" : " отключена"));
                });
    }
}