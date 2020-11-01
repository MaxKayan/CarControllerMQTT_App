package com.example.carcontrollermqtt;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.carcontrollermqtt.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    MainViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_devices, R.id.navigation_dashboard, R.id.navigation_history, R.id.navigation_settings)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(binding.navView, navController);

        binding.navView.setNavigationItemSelectedListener(item -> {
//            int id = item.getItemId();
//            Fragment fragment = null;
//            Class<? extends Fragment> fragmentClass;
//            if (id == R.id.navigation_devices) {
//                fragmentClass = DevicesFragment.class;
//            } else if (id == R.id.navigation_history) {
//                fragmentClass = HistoryFragment.class;
//            } else if (id == R.id.navigation_dashboard) {
//                fragmentClass = DashboardFragment.class;
//            } else return false;
//
//            try {
//                fragment = fragmentClass.newInstance();
//            } catch (IllegalAccessException | InstantiationException e) {
//                e.printStackTrace();
//                return false;
//            }

            NavController navController = ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)).getNavController();
            navController.navigate(R.id.action_navigation_dashboard_to_navigation_devices);
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
            closeDrawer();
            return true;
        });
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
}
