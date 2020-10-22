package com.example.carcontrollermqtt.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.WqttMessageDao;
import com.example.carcontrollermqtt.data.models.WqttClient;
import com.example.carcontrollermqtt.data.models.WqttMessage;
import com.example.carcontrollermqtt.data.models.transactions.WqttMessageWithDevice;
import com.example.carcontrollermqtt.service.WqttClientManager;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Calendar;
import java.util.List;

import io.reactivex.schedulers.Schedulers;

public class HistoryViewModel extends AndroidViewModel {

    private final WqttMessageDao messageDao;

    private final WqttClientManager clientManager;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        this.messageDao = AppDatabase.getInstance(application).messageDao();
        clientManager = WqttClientManager.getInstance(application);
    }

    LiveData<List<WqttMessageWithDevice>> observeMessagesWithDevice() {
        return messageDao.observeMessagesWithDevices();
    }

    void publishMessage(String topic, String payload) {
        WqttClient client = clientManager.getWqttClient("testUser");
        if (client != null) {
            try {
                messageDao.insert(new WqttMessage(0L, 1, Calendar.getInstance().getTime(), false, topic, payload))
                        .subscribeOn(Schedulers.io())
                        .subscribe();
                client.getClient().publish(topic, new MqttMessage(payload.getBytes()));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}