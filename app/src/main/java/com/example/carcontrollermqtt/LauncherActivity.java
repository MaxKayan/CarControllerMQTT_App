package com.example.carcontrollermqtt;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carcontrollermqtt.databinding.ActivityLauncherBinding;

public class LauncherActivity extends AppCompatActivity {
    private static final String TAG = "LauncherActivity";

    Runnable proceed = () -> {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    };
    private ActivityLauncherBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long startTime = System.currentTimeMillis();

        binding = ActivityLauncherBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_center));

        new Handler().postDelayed(proceed, 1000 - (System.currentTimeMillis() - startTime));
    }
}