package com.example.carcontrollermqtt.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.transactions.DeviceWithMessages;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface DeviceDao extends BaseDao<Device> {
    @Query("SELECT * FROM devices")
    LiveData<List<Device>> observeAll();

    @Query("SELECT * FROM devices")
    Single<List<Device>> getAll();

    @Query("SELECT * FROM devices WHERE username = :name")
    Single<Device> get(String name);

    @Query("SELECT * FROM devices WHERE selected = 1")
    LiveData<Device> observeSelectedDevice();

    @Query("SELECT * FROM devices WHERE selected = 1")
    Single<Device> getSelectedDevice();

    @Transaction
    @Query("SELECT * FROM devices WHERE id = :id")
    Single<DeviceWithMessages> getDeviceWithMessages(long id);

    @Update
    Completable updateMultiple(Device... devices);

}
