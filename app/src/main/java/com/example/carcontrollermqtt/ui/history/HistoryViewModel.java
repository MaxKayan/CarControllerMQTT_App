package com.example.carcontrollermqtt.ui.history;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.DqttMessageDao;
import com.example.carcontrollermqtt.data.models.transactions.DqttMessageWithDevice;
import com.example.carcontrollermqtt.service.DqttClientManager;
import com.example.carcontrollermqtt.service.DqttMessageManager;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private static final String TAG = "HistoryViewModel";

    private final DqttMessageDao messageDao;
    private final DqttMessageManager messageManager;

    @SuppressLint("CheckResult")
    public HistoryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        this.messageDao = database.messageDao();

        messageManager = DqttClientManager.getInstance(application).getMessageManager();
    }

    LiveData<List<DqttMessageWithDevice>> observeMessagesWithDevice() {
        return messageDao.observeMessagesWithDevices();
    }

    void publishMessage(String topic, String payload) {
        messageManager.sendMessage(topic, payload);
    }
}