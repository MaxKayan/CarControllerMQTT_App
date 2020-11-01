package com.example.carcontrollermqtt.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.carcontrollermqtt.MainActivityOld;
import com.example.carcontrollermqtt.R;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.messages.InfoMessage;
import com.example.carcontrollermqtt.databinding.ActivityDashboardBinding;

import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";

    private ActivityDashboardBinding binding;

    private DashboardViewModel viewModel;
    private Animation scaleLoopAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding.refreshLayout.setOnRefreshListener(() -> {
            binding.refreshLayout.setRefreshing(false);
        });

        precacheAnimations();
        subscribeObservers();
        setupViewListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupViewListeners() {
        binding.engineStatusDisabled.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "setupViewListeners: eng down");
                    return true;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "setupViewListeners: eng up");
                    return true;
                default:
                    return false;
            }
        });
    }

    private void precacheAnimations() {
        scaleLoopAnim = AnimationUtils.loadAnimation(this, R.anim.scale_loop);
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
        setEngineStateView(message.isEngineRunning());
        binding.voltageValue.setText(String.format("%s %s", message.getBatteryVoltage(), "v"));
        binding.temperatureValue.setText(String.format(Locale.US, "%.1f %s", message.getIndoorTemperature(), getString(R.string.celsius_symbol)));
    }

    private void setEngineStateView(boolean running) {
        if (running) {
            binding.engineStatusEnabled.startAnimation(scaleLoopAnim);
        } else {
            binding.engineStatusEnabled.clearAnimation();
        }

        binding.engineStatusEnabled.setVisibility(running ? View.VISIBLE : View.GONE);
        binding.engineStatusDisabled.setVisibility(!running ? View.VISIBLE : View.GONE);
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
                startActivity(new Intent(this, MainActivityOld.class));
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
