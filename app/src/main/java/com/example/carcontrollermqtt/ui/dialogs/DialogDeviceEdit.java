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
import androidx.room.EmptyResultSetException;

import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.DeviceDao;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.databinding.DialogDeviceEditBinding;
import com.google.android.material.textfield.TextInputLayout;

import io.reactivex.android.schedulers.AndroidSchedulers;
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
        binding.btnConfirm.setOnClickListener(v -> saveEditedDevice());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void saveEditedDevice() {
        DeviceOptions input = getValidInput();
        if (input == null) return;

        if (editedDevice == null || usernameWasChanged(input.username)) {
            deviceDao.get(input.username)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess(device -> {
                        Log.w(TAG, "saveEditedDevice: device name is occupied + " + device.getUsername());
                        binding.inputNameLayout.setError("Логин уже используется");
                    })
                    .subscribe((device, throwable) -> {
                        if (throwable instanceof EmptyResultSetException) {
                            writeToDB(getResult(input.username, input.password));
                            dismiss();
                        }
                    });

        } else {
            writeToDB(getResult(input.username, input.password));
            dismiss();
        }
    }

    private boolean usernameWasChanged(String newUsername) {
        return editedDevice != null && !editedDevice.getUsername().equals(newUsername);
    }

    @Nullable
    private DeviceOptions getValidInput() {
        String username = String.valueOf(binding.inputName.getText());
        String password = String.valueOf(binding.inputPassword.getText());
        if (!validateInput(username, password)) return null;

        return new DeviceOptions(username, password);
    }

    private boolean validateInput(String username, String password) {
        boolean nameNotEmpty = layoutTextNotEmpty(binding.inputNameLayout, username, "Укажите имя устройства");
        boolean passNotEmpty = layoutTextNotEmpty(binding.inputPasswordLayout, password, "Укажите пароль");

        return nameNotEmpty && passNotEmpty;
    }

    private boolean layoutTextNotEmpty(TextInputLayout layout, String value, String errorText) {
        if (value.isEmpty() || value.equals("null")) {
            layout.setError(errorText);
            return false;
        } else {
            layout.setError(null);
            return true;
        }
    }

    private Device getResult(String name, String password) {
        Device result;
        String keepAlive = binding.inputKeepAlive.getText().toString();
        int keepAliveValue = keepAlive.isEmpty() ? 60 : Integer.parseInt(keepAlive);

        if (editedDevice == null) {
            result = new Device(0L, true, false, null, null, name, password, keepAliveValue);
        } else {
            result = new Device(editedDevice.getId(), editedDevice.isEnabled(), editedDevice.isSelected(), null, null, name, password, keepAliveValue);
        }

        return result;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void writeToDB(Device device) {
        deviceDao.getSelectedDevice()
                .subscribeOn(Schedulers.io())
                .doOnSuccess(selectedDevice -> {
                    Log.d(TAG, "writeToDB: selected device found" + device.getUsername());
                    deviceDao.insert(device)
                            .subscribe();
                })
                .subscribe((device1, throwable) -> {
                    if (throwable instanceof EmptyResultSetException) {
                        Log.w(TAG, "writeToDB: there's no selected device", throwable);
                        deviceDao.insert(device.cloneWithSelected(true))
                                .subscribe();
                    }
                });
    }

    private static class DeviceOptions {
        String username;
        String password;

        public DeviceOptions(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

}
