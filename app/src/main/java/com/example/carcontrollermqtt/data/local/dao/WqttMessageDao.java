package com.example.carcontrollermqtt.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.carcontrollermqtt.data.models.WqttMessage;
import com.example.carcontrollermqtt.data.models.transactions.WqttMessageWithDevice;

@Dao
public interface WqttMessageDao extends BaseDao<WqttMessage> {
    @Query("SELECT * FROM messages")
    LiveData<WqttMessage> observeMessages();

    @Transaction
    @Query("SELECT * FROM messages")
    LiveData<WqttMessageWithDevice> observeMessagesWithDevices();


}
