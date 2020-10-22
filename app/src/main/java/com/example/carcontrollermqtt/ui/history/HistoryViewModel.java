package com.example.carcontrollermqtt.ui.history;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.WqttMessageDao;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.transactions.WqttMessageWithDevice;
import com.example.carcontrollermqtt.service.WqttClientManager;
import com.example.carcontrollermqtt.service.WqttMessageManager;

import java.util.List;

import io.reactivex.schedulers.Schedulers;

public class HistoryViewModel extends AndroidViewModel {

    private final WqttMessageDao messageDao;
    private final WqttClientManager clientManager;
    private Device selectedDevice;
    private WqttMessageManager messageManager;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public HistoryViewModel(@NonNull Application application) {
        super(application);
        this.messageDao = AppDatabase.getInstance(application).messageDao();
        AppDatabase.getInstance(application).deviceDao().getAll()
                .subscribeOn(Schedulers.io())
                .subscribe(devices -> {
                    selectedDevice = devices.get(0);
                });

        clientManager = WqttClientManager.getInstance(application);
        messageManager = clientManager.getMessageManager();
    }

    LiveData<List<WqttMessageWithDevice>> observeMessagesWithDevice() {
        return messageDao.observeMessagesWithDevices();
    }

    void publishMessage(String topic, String payload) {
        messageManager.sendMessage(selectedDevice, topic, payload);
    }
}