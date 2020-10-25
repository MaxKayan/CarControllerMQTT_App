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

public class HistoryViewModel extends AndroidViewModel {
    private static final String TAG = "HistoryViewModel";

    private final WqttMessageDao messageDao;
    private final WqttMessageManager messageManager;

    @SuppressLint("CheckResult")
    public HistoryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        this.messageDao = database.messageDao();

        messageManager = WqttClientManager.getInstance(application).getMessageManager();
    }

    LiveData<List<WqttMessageWithDevice>> observeMessagesWithDevice() {
        return messageDao.observeMessagesWithDevices();
    }

    void publishMessage(String topic, String payload) {
        Device selectedDevice = WqttClientManager.getSelectedDevice();
        if (selectedDevice != null) {
            messageManager.sendMessage(selectedDevice, topic, payload);
        }
    }
}