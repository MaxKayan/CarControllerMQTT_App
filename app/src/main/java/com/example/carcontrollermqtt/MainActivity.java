package com.example.carcontrollermqtt;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.carcontrollermqtt.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    MainViewModel viewModel;
    NavOptions navOptions = new NavOptions.Builder()
            .setLaunchSingleTop(true)  // Used to prevent multiple copies of the same destination
            .setEnterAnim(R.anim.nav_default_enter_anim)
            .setExitAnim(R.anim.nav_default_exit_anim)
            .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
            .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
            .build();
    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        navController = ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)).getNavController();
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_devices, R.id.navigation_dashboard, R.id.navigation_history, R.id.navigation_settings)
                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_devices) {
                navigateTo(R.id.navigation_devices);
            } else if (id == R.id.navigation_history) {
                navigateTo(R.id.navigation_history);
            } else if (id == R.id.navigation_dashboard) {
                navigateTo(R.id.navigation_dashboard);
            } else if (id == R.id.navigation_settings) {
                navigateTo(R.id.navigation_settings);
            } else return false;

            closeDrawer();
            return true;
        });
    }

    private void navigateTo(final int navigation) {
        if (navController.getCurrentDestination().getId() == navigation) return;

        navController.navigate(navigation, null, navOptions);
    }

    public void openDrawer() {
        binding.drawerLayout.open();
    }

    public void closeDrawer() {
        binding.drawerLayout.close();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
