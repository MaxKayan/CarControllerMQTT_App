package com.example.carcontrollermqtt.data.models.messages;

import com.google.gson.annotations.SerializedName;

public class LocationMessage {
    @SerializedName("long")
    private final double longitude;
    @SerializedName("lati")
    private final double latitude;
    @SerializedName("accu")
    private final int accuracy;

    public LocationMessage(double longitude, double latitude, int accuracy) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.accuracy = accuracy;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getAccuracy() {
        return accuracy;
    }
}
