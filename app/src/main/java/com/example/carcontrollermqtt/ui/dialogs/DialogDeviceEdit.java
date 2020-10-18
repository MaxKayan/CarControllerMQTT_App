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

import io.reactivex.schedulers.Schedulers;

public class DialogDeviceEdit extends DialogFragment {
    private static final String TAG = "DialogDeviceEdit";
    private static final String ID = "DeviceID";
    private static final String NAME = "NameField";
    private static final String PASSWORD = "PasswordField";
    private static final String KEEP_ALIVE = "KeepAliveField";

    DialogDeviceEditBinding binding;

    DeviceDao deviceDao;

    private long id;

    public static DialogDeviceEdit newInstance() {
        return new DialogDeviceEdit();
    }

    public static DialogDeviceEdit newInstance(Device device) {
        DialogDeviceEdit instance = new DialogDeviceEdit();
        Bundle args = new Bundle();
        args.putLong(ID, device.getId());
        args.putString(NAME, device.getUsername());
        args.putString(PASSWORD, device.getPassword());
        args.putInt(KEEP_ALIVE, device.getKeepAlive());
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
            id = args.getLong(ID);
            binding.inputName.setText(args.getString(NAME));
            binding.inputPassword.setText(args.getString(PASSWORD));
            binding.inputKeepAlive.setText(String.valueOf(args.getInt(KEEP_ALIVE)));
        }
    }

    private void setupListeners() {
        binding.btnConfirm.setOnClickListener(v -> {
            writeToDB();
            dismiss();
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void writeToDB() {
        String name = binding.inputName.getText().toString();
        String password = binding.inputPassword.getText().toString();
        String keepAlive = binding.inputKeepAlive.getText().toString();
        Log.d(TAG, "writeToDB: " + name);
        Log.d(TAG, "writeToDB: " + password);
        Log.d(TAG, "writeToDB: " + keepAlive);

        deviceDao.insertDevice(new Device(id, false, name, password, keepAlive.isEmpty() ? 60 : Integer.parseInt(keepAlive)))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Log.d(TAG, "setupListeners: Inserted new device");
//                    Toast.makeText(getContext(), "Устройство "+name+" добавлено", Toast.LENGTH_SHORT).show();
                });
    }
}
