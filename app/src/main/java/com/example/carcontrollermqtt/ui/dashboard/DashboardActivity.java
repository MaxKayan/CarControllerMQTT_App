package com.example.carcontrollermqtt.ui.dashboard;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.carcontrollermqtt.MainActivity;
import com.example.carcontrollermqtt.R;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.messages.InfoMessage;
import com.example.carcontrollermqtt.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";

    private ActivityDashboardBinding binding;

    private DashboardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        subscribeObservers();
    }

    private void subscribeObservers() {
        viewModel.observeSelectedDevice().observe(this, device -> {
            Log.d(TAG, "subscribeObservers: current device " + device);
            setupDeviceView(device);

            viewModel.observeInfo(this, device).observe(this, infoMessage -> {
                Log.d(TAG, "subscribeObservers: new info - " + infoMessage.toString());
                setupInfoView(infoMessage);
            });
        });
    }

    private void setupDeviceView(Device device) {
        binding.label.setText(device.getLabel());
        binding.username.setText(device.getUsername());
    }

    private void setupInfoView(InfoMessage message) {
        binding.voltageValue.setText(String.valueOf(message.getBatteryVoltage()));
        binding.temperatureValue.setText(String.valueOf(message.getIndoorTemperature()));
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
