package com.example.carcontrollermqtt.ui.dashboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.carcontrollermqtt.MainActivity;
import com.example.carcontrollermqtt.R;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.messages.InfoMessage;
import com.example.carcontrollermqtt.data.models.messages.LocationMessage;
import com.example.carcontrollermqtt.databinding.ActivityDashboardBinding;
import com.example.carcontrollermqtt.utils.PermissionsManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.util.Locale;

public class DashboardFragment extends Fragment {
    private static final String TAG = "DashboardFragment";
    private ActivityDashboardBinding binding;
    private DashboardViewModel viewModel;
    private Animation scaleLoopAnim;

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "permissions: granted");
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    Log.d(TAG, "permissions: denied");
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityDashboardBinding.inflate(getLayoutInflater(), container, false);
        initializeMaps(inflater.getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        precacheAnimations();
        subscribeObservers();
        setupViewListeners();
        configureMaps();
    }

    private void initializeMaps(Context context) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    private void configureMaps() {
        PermissionsManager.requestPermissionsIfNecessary(requestPermissionLauncher, getActivity(),
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.ACCESS_FINE_LOCATION
                }
        );

        binding.map.setTileSource(TileSourceFactory.MAPNIK);
        binding.map.getZoomController().activate();
        binding.map.setMultiTouchControls(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupViewListeners() {
        binding.btnDetails.setOnClickListener(v -> {
            ((MainActivity) getActivity()).openDrawer();
        });

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

        binding.refreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshDeviceInfo();
            binding.refreshLayout.setRefreshing(false);
        });

        binding.locationIcon.setOnClickListener(v -> {
            Log.d(TAG, "setupViewListeners: refresh location");
            viewModel.refreshDeviceLocation();
        });
    }

    private void precacheAnimations() {
        scaleLoopAnim = AnimationUtils.loadAnimation(getContext(), R.anim.scale_loop);
    }

    private void subscribeObservers() {
        viewModel.observeSelectedDevice().observe(getViewLifecycleOwner(), device -> {
            Log.d(TAG, "subscribeObservers: current device " + device);
            if (device == null) return;

            setupDeviceView(device);

            LifecycleOwner owner = getViewLifecycleOwner();

            viewModel.observeInfo(owner, device).observe(owner, infoMessage -> {
                Log.d(TAG, "subscribeObservers: new info - " + infoMessage.toString());
                setupInfoView(infoMessage);
            });

            viewModel.observeLocation(owner, device).observe(owner, locationMessage -> {
                Log.d(TAG, "subscribeObservers: new location message " + locationMessage.toString());
                setupLocationView(locationMessage);
            });
        });
    }

    /**
     * Apply received message that contains location data to the view
     *
     * @param message POJO that contains latitude & longitude
     */
    private void setupLocationView(LocationMessage message) {
        binding.latitudeValue.setText(String.valueOf(message.getLatitude()));
        binding.longitudeValue.setText(String.valueOf(message.getLongitude()));
    }

    /**
     * Apply currently selected device object to the view.
     *
     * @param device Current active device with label, name and avatar bitmap URI.
     */
    private void setupDeviceView(Device device) {
        binding.label.setText(device.getLabel());
        binding.username.setText(device.getUsername());

        Glide.with(this)
                .load(device.getAvatarUri().getPath())
                .into(binding.avatar);
    }

    /**
     * Apply received message that contains various device data.
     *
     * @param message POJO that contains data like voltage, temperature etc.
     */
    private void setupInfoView(InfoMessage message) {
        setEngineStateView(message.isEngineRunning());
        binding.voltageValue.setText(String.format("%s %s", message.getBatteryVoltage(), "v"));
        binding.temperatureValue.setText(String.format(Locale.US, "%.1f %s", message.getIndoorTemperature(), getString(R.string.celsius_symbol)));
        binding.signalValue.setText(String.valueOf(message.getSignalQuality()));
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
}