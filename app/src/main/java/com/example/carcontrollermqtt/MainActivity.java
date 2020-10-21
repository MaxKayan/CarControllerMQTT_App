package com.example.carcontrollermqtt;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.databinding.ActivityMainBinding;
import com.example.carcontrollermqtt.service.WqttClientManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    MainViewModel viewModel;
    private ActivityMainBinding binding;
    private AppDatabase database;

    private WqttClientManager wqttClientManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        database = AppDatabase.getInstance(this);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_devices, R.id.navigation_dashboard, R.id.navigation_history, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        wqttClientManager = WqttClientManager.getInstance(this);
        Log.d(TAG, "onCreate: got wqqt manager instance - " + wqttClientManager);

        database.deviceDao().observeAll().observe(this, devices -> wqttClientManager.postDeviceList(devices));
    }
}