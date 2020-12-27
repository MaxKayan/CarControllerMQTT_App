package com.example.carcontrollermqtt.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.carcontrollermqtt.data.models.DqttMessage;
import com.example.carcontrollermqtt.data.models.transactions.DqttMessageWithDevice;

import java.util.List;

@Dao
public interface DqttMessageDao extends BaseDao<DqttMessage> {
    @Query("SELECT * FROM messages")
    LiveData<DqttMessage> observeMessages();

    @Transaction
    @Query("SELECT * FROM messages")
    LiveData<List<DqttMessageWithDevice>> observeMessagesWithDevices();

}
