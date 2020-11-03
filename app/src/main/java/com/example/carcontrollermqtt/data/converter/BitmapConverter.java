package com.example.carcontrollermqtt.data.converter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.TypeConverter;

import java.io.ByteArrayOutputStream;

public class BitmapConverter {
    @TypeConverter
    public static byte[] BitMapToRaw(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    @TypeConverter
    public static Bitmap RawToBitMap(byte[] blob) {
        try {
            return BitmapFactory.decodeByteArray(blob, 0, blob.length);

        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}