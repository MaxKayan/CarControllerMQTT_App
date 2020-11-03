package com.example.carcontrollermqtt.data.models.messages;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class InfoMessage {
    @SerializedName("heating")
    private final boolean engineRunning;
    @SerializedName("battery")
    private final float batteryVoltage;
    @SerializedName("security")
    private final boolean carLocked;
    @SerializedName("temp0")
    private final float indoorTemperature;
    @SerializedName("timer")
    private final float remainingTime;
    @SerializedName("signal")
    private final int signalQuality;

    public InfoMessage(boolean engineRunning, float batteryVoltage, boolean carLocked, float indoorTemperature, float remainingTime, int signalQuality) {
        this.engineRunning = engineRunning;
        this.batteryVoltage = batteryVoltage;
        this.carLocked = carLocked;
        this.indoorTemperature = indoorTemperature;
        this.remainingTime = remainingTime;
        this.signalQuality = signalQuality;
    }

    public boolean isEngineRunning() {
        return engineRunning;
    }

    public float getBatteryVoltage() {
        return batteryVoltage;
    }

    public boolean isCarLocked() {
        return carLocked;
    }

    public float getIndoorTemperature() {
        return indoorTemperature;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + engineRunning + batteryVoltage + carLocked + indoorTemperature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfoMessage that = (InfoMessage) o;
        return engineRunning == that.engineRunning &&
                Float.compare(that.batteryVoltage, batteryVoltage) == 0 &&
                carLocked == that.carLocked &&
                Float.compare(that.indoorTemperature, indoorTemperature) == 0 &&
                Float.compare(that.remainingTime, remainingTime) == 0 &&
                signalQuality == that.signalQuality;
    }

    @Override
    public int hashCode() {
        return Objects.hash(engineRunning, batteryVoltage, carLocked, indoorTemperature, remainingTime, signalQuality);
    }

    public float getRemainingTime() {
        return remainingTime;
    }

    public int getSignalQuality() {
        return signalQuality;
    }
}
