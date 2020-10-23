package com.example.carcontrollermqtt.data.local.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(T entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertAndReadId(T entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<T> list);

    @Update
    Completable update(T entity);

    @Update
    Completable updateAll(List<T> list);

    @Delete
    Completable delete(T entity);

    @Delete
    Completable deleteAll(List<T> list);
}
