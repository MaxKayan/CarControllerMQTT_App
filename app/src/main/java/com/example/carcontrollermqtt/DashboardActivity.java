package com.example.carcontrollermqtt;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    public void showPopup(View view) {
        Log.d(TAG, "showPopup: called");
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.options_main);

        popupMenu.setOnMenuItemClickListener(item -> {
            Log.d(TAG, "showPopup: clicked on " + item.getItemId());
            int itemId = item.getItemId();

            if (itemId == R.id.menu_devices) {
                Log.d(TAG, "showPopup: Devices");
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.menu_settings) {
                Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popupMenu.setOnDismissListener(menu -> {
            Log.d(TAG, "showPopup: Dismissed");
        });
        popupMenu.show();
    }
}
