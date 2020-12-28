package com.example.carcontrollermqtt.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.carcontrollermqtt.data.models.DqttMessage
import com.example.carcontrollermqtt.data.models.transactions.DqttMessageWithDevice

@Dao
interface DqttMessageDao {
    @Query("SELECT * FROM messages")
    fun observeMessages(): LiveData<DqttMessage>

    @Transaction
    @Query("SELECT * FROM messages")
    fun observeMessagesWithDevices(): LiveData<List<DqttMessageWithDevice>>

    @Insert
    suspend fun insertAndReadId(message: DqttMessage): Long

    @Insert
    suspend fun insert(message: DqttMessage)

    @Delete
    suspend fun delete(message: DqttMessage)

    @Update
    suspend fun update(message: DqttMessage)
}