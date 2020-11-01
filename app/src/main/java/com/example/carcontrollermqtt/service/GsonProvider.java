package com.example.carcontrollermqtt.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonProvider {
    private static Gson instance;

    public static Gson getGsonInstance() {
        if (instance == null) {
            instance = new GsonBuilder()
                    .create();
        }

        return instance;
    }
}
