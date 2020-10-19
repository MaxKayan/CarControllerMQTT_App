package com.example.carcontrollermqtt.ui.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.DeviceDao;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.databinding.DialogDeviceEditBinding;
import com.google.android.material.textfield.TextInputLayout;

import io.reactivex.schedulers.Schedulers;

public class DialogDeviceEdit extends DialogFragment {
    private static final String TAG = "DialogDeviceEdit";
    private static final String DEVICE_SERIALIZED = "DeviceSerialized";


    DialogDeviceEditBinding binding;

    DeviceDao deviceDao;

    private Device editedDevice;

    public static DialogDeviceEdit newInstance() {
        return new DialogDeviceEdit();
    }

    public static DialogDeviceEdit newInstance(Device device) {
        DialogDeviceEdit instance = new DialogDeviceEdit();
        Bundle args = new Bundle();
        args.putSerializable(DEVICE_SERIALIZED, device);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogDeviceEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deviceDao = AppDatabase.getInstance(view.getContext()).deviceDao();
        unpackBundleArgs(getArguments());
        setupListeners();
    }

    private void unpackBundleArgs(Bundle args) {
        if (args != null) {
            editedDevice = (Device) args.getSerializable(DEVICE_SERIALIZED);
            if (editedDevice == null) return;

            binding.textHeader.setText("Редактировать устройство");
            binding.inputName.setText(editedDevice.getUsername());
            binding.inputPassword.setText(editedDevice.getPassword());
            binding.inputKeepAlive.setText(String.valueOf(editedDevice.getKeepAlive()));
        }
    }

    private void setupListeners() {
        binding.btnConfirm.setOnClickListener(v -> {
            if (!validateInput()) return;

            writeToDB();
            dismiss();
        });
    }

    private boolean layoutTextNotEmpty(TextInputLayout layout, String errorText) {
        if (layout.getEditText().getText().toString().isEmpty()) {
            layout.setError(errorText);
            Log.d(TAG, "layoutTextNotEmpty: setting false");
            return false;
        } else {
            layout.setError(null);
            Log.d(TAG, "layoutTextNotEmpty: setting true");
            return true;
        }
    }

    private boolean validateInput() {
        boolean nameValid = layoutTextNotEmpty(binding.inputNameLayout, "Укажите имя устройства");
        boolean passValid = layoutTextNotEmpty(binding.inputPasswordLayout, "Укажите пароль");

        return nameValid && passValid;
    }

    private Device getResult() {
        Device result;
        String name = binding.inputName.getText().toString();
        String password = binding.inputPassword.getText().toString();
        String keepAlive = binding.inputKeepAlive.getText().toString();
        int keepAliveValue = keepAlive.isEmpty() ? 60 : Integer.parseInt(keepAlive);

        if (editedDevice == null) {
            result = new Device(0L, true, false, name, password, keepAliveValue);
        } else {
            result = new Device(editedDevice.getId(), editedDevice.isEnabled(), editedDevice.isSelected(), name, password, keepAliveValue);
        }

        return result;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void writeToDB() {
        deviceDao.insertDevice(getResult())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Log.d(TAG, "setupListeners: Inserted new device");
                });
    }

}
