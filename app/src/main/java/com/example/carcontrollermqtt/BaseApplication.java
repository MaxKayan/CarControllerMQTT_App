package com.example.carcontrollermqtt;

import android.app.Application;
import android.util.Log;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.service.WqttClientManager;

public class BaseApplication extends Application {
    private static final String TAG = "BaseApplication";

    private AppDatabase database;
    private WqttClientManager wqttClientManager;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: instantiating application");

        // Instantiate client manager singleton
        wqttClientManager = WqttClientManager.getInstance(this);

        // Instantiate Room Database singleton
        database = AppDatabase.getInstance(this);
        // Each time device list changes in the SQL table, post the list to client manager.
        database.deviceDao().observeAll().observeForever(devices -> wqttClientManager.submitDeviceList(devices));
    }

}
