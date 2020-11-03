package com.example.carcontrollermqtt.data.converter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.room.TypeConverter;

import java.io.ByteArrayOutputStream;

public class UriConverter {
    @TypeConverter
    public static String UriToString(Uri uri) {
        return uri.toString();
    }

    @TypeConverter
    public static Uri StringToUri(String rawUri) {
        return Uri.parse(rawUri);
    }
}