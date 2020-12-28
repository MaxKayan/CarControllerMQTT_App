package com.example.carcontrollermqtt.ui.dialogs;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.room.EmptyResultSetException;

import com.bumptech.glide.Glide;
import com.example.carcontrollermqtt.R;
import com.example.carcontrollermqtt.data.local.AppDatabase;
import com.example.carcontrollermqtt.data.local.dao.DeviceDao;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.databinding.DialogDeviceEditBinding;
import com.example.carcontrollermqtt.utils.FileManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DialogDeviceEdit extends DialogFragment {
    private static final String TAG = "DialogDeviceEdit";
    private static final String DEVICE_SERIALIZED = "DeviceSerialized";

    DialogDeviceEditBinding binding;

    DeviceDao deviceDao;

    private String avatarImagePath;

    private Device editedDevice;

    private ActivityResultLauncher<Void> takePicture;
    private ActivityResultLauncher<String> getPicture;

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

        takePicture = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
            Log.d(TAG, "setupListeners: picture " + result + " - ");
            if (result == null) return;

            binding.deviceAvatar.setImageBitmap(result);
            avatarImagePath = FileManager.writeImage(getContext(), "car_avatar_" + new Random().nextDouble(), result).toString();
        });

        getPicture = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            Bitmap bitmap;
            if (result != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(binding.getRoot().getContext().getContentResolver(), result);
                    Log.d(TAG, "onCreateView: " + bitmap.toString());

                    binding.deviceAvatar.setImageBitmap(bitmap);
                    avatarImagePath = FileManager.writeImage(getContext(), "car_avatar_" + new Random().nextDouble(), bitmap).toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "onCreateView: received null result!");
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deviceDao = AppDatabase.getInstance(view.getContext()).deviceDao();
        unpackBundleArgs(getArguments());
        setupListeners(view);
    }

    private void unpackBundleArgs(Bundle args) {
        if (args != null) {
            editedDevice = (Device) args.getSerializable(DEVICE_SERIALIZED);
            if (editedDevice == null) return;

            if (editedDevice.getAvatarUri() != null) {
                Glide.with(this)
                        .load(editedDevice.getAvatarUri().getPath())
                        .into(binding.deviceAvatar);
            }

            Log.d(TAG, "unpackBundleArgs: editedDevice id " + editedDevice.getId());

            binding.textHeader.setText("Редактировать устройство");
            binding.inputLabel.setText(editedDevice.getLabel());
            binding.inputMAC.setText(editedDevice.getDeviceId());
            avatarImagePath = editedDevice.getAvatarUriString();
            binding.inputName.setText(editedDevice.getUsername());
            binding.inputPassword.setText(editedDevice.getPassword());
            binding.inputKeepAlive.setText(String.valueOf(editedDevice.getKeepAlive()));
        }
    }

    private void setupListeners(View view) {
        binding.btnConfirm.setOnClickListener(v -> saveEditedDevice());
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(view.getContext());
        dialog.setTitle("Назначить фото")
                .setIcon(R.drawable.ic_baseline_add_photo_alternate_24)
                .setMessage("Выберите источник")
                .setPositiveButton("Камера", (dialogInterface, i) -> takePicture.launch(null))
                .setNeutralButton("Галерея", (dialogInterface, i) -> getPicture.launch("image/*"));
//                .setNegativeButton("Отмена", (dialogInterface, i) -> { });
        binding.btnTakePicture.setOnClickListener(v -> {
            dialog.show();
        });
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
                            writeToDB(getResult(input));
                            dismiss();
                        }
                    });

        } else {
            writeToDB(getResult(input));
            dismiss();
        }
    }

    private boolean usernameWasChanged(String newUsername) {
        return editedDevice != null && !editedDevice.getUsername().equals(newUsername);
    }

    @Nullable
    private DeviceOptions getValidInput() {
        String label = String.valueOf(binding.inputLabel.getText());
        String deviceId = String.valueOf(binding.inputMAC.getText());
        String username = String.valueOf(binding.inputName.getText());
        String password = String.valueOf(binding.inputPassword.getText());

        if (!validateInput(label, username, password)) return null;

        return new DeviceOptions(label, deviceId, avatarImagePath, username, password);
    }

    private boolean validateInput(String label, String username, String password) {
        boolean labelNotEmpty = layoutTextNotEmpty(binding.inputLabelLayout, label, "Укажите название для авто");
        boolean deviceIdNotEmpty = layoutTextNotEmpty(binding.inputMACLayout, label, "Укажите MAC-адрес устройства");
        boolean nameNotEmpty = layoutTextNotEmpty(binding.inputNameLayout, username, "Укажите логин устройства");
        boolean passNotEmpty = layoutTextNotEmpty(binding.inputPasswordLayout, password, "Укажите пароль");

        return labelNotEmpty && deviceIdNotEmpty && nameNotEmpty && passNotEmpty;
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

    private Device getResult(DeviceOptions options) {
        Device result;
        String keepAlive = binding.inputKeepAlive.getText().toString();
        int keepAliveValue = keepAlive.isEmpty() ? 60 : Integer.parseInt(keepAlive);

        if (editedDevice == null) {
            result = new Device(null, true, false, options.label, options.avatar, options.deviceId, options.username, options.password, keepAliveValue);
        } else {
            result = new Device(editedDevice.getId(), editedDevice.getEnabled(), editedDevice.getSelected(), options.label, options.avatar, options.deviceId, options.username, options.password, keepAliveValue);
        }

        return result;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void writeToDB(Device device) {
        deviceDao.getSelectedDevice()
                .subscribeOn(Schedulers.io())
                .doOnSuccess(selectedDevice -> {
                    Log.d(TAG, "writeToDB: selected device found " + device.getUsername());
                    Log.d(TAG, "writeToDB: saving device with id - " + device.getId());
                    deviceDao.insert(device)
                            .subscribe(() -> {
                            }, throwable -> Log.e(TAG, "writeToDB: failed to insert", throwable));
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
        String label;
        String deviceId;
        String avatar;
        String username;
        String password;

        public DeviceOptions(String label, String deviceId, String avatar, String username, String password) {
            this.label = label;
            this.deviceId = deviceId;
            this.username = username;
            this.password = password;
            this.avatar = avatar;
        }
    }

}
