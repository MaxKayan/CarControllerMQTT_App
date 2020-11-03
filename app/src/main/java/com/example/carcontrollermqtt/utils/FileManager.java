package com.example.carcontrollermqtt.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.content.Context.MODE_PRIVATE;

public class FileManager {
    public static Uri writeImage(Context appContext, String filename, Bitmap bitmap) {
        ContextWrapper wrapper = new ContextWrapper(appContext);

        File file = wrapper.getDir("Images", MODE_PRIVATE);

        file = new File(file, filename + ".jpg");

        try {
            OutputStream stream = null;
            stream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            stream.flush();

            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parse the gallery image url to uri
        return Uri.parse(file.getAbsolutePath());
    }
}
