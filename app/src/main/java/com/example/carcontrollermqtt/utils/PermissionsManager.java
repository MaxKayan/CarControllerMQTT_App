package com.example.carcontrollermqtt.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

//@TargetApi(Build.VERSION_CODES.M)
public class PermissionsManager {
    private static final String TAG = "PermissionsManager";

    public static void requestPermissionsIfNecessary(ActivityResultLauncher<String> resultLauncher, Activity activity, String[] permissions) {
        if (Build.VERSION.SDK_INT < 23) return;
        Log.d(TAG, "requestPermissionsIfNecessary: running...");

//        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "requestPermissionsIfNecessary: " + permission + " is already granted");
                // Permission is granted
//                permissionsToRequest.add(permission);
            } else if (activity.shouldShowRequestPermissionRationale(permission)) {
                Log.d(TAG, "requestPermissionsIfNecessary: show permission ui");
//                showInContextUI(...);
            } else {
                Log.d(TAG, "requestPermissionsIfNecessary: ask for permission");
                resultLauncher.launch(permission);
            }
        }
//        if (permissionsToRequest.size() > 0) {
////            ActivityCompat.requestPermissions(
////                    context,
////                    permissionsToRequest.toArray(new String[0]),
////                    REQUEST_PERMISSIONS_REQUEST_CODE);
//            launcher.launch();
//        }
    }
}
