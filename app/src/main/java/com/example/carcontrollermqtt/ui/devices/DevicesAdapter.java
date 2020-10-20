package com.example.carcontrollermqtt.ui.devices;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carcontrollermqtt.R;
import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.data.models.DeviceEvent;
import com.example.carcontrollermqtt.databinding.ItemDeviceBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Objects;

public class DevicesAdapter extends ListAdapter<Device, DevicesAdapter.DeviceViewHolder> {
    public static final DiffUtil.ItemCallback<Device> DEVICE_ITEM_CALLBACK = new DiffUtil.ItemCallback<Device>() {
        @Override
        public boolean areItemsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
            boolean same = oldItem.getUsername().equals(newItem.getUsername()) &&
                    oldItem.isEnabled() == newItem.isEnabled() &&
                    oldItem.isSelected() == newItem.isSelected() &&
                    Objects.equals(oldItem.getEvent(), newItem.getEvent());

            return false;
        }
    };
    private static final String TAG = "DevicesAdapter";
    private final DeviceViewHolder.OnDeviceCardInteraction editClickCallback;

    public DevicesAdapter(DeviceViewHolder.OnDeviceCardInteraction editClickCallback) {
        super(DEVICE_ITEM_CALLBACK);
        this.editClickCallback = editClickCallback;
    }

    public Device getDeviceAt(int position) {
        return getItem(position);
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeviceBinding binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DeviceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device currentItem = getItem(position);
        holder.bind(currentItem, editClickCallback);
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceId, deviceName, textStatus;
        private ImageView iconConnected, iconDisconnected, iconError;
        private MaterialButton buttonEdit;
        private View isSelected;
        private MaterialCardView cardDevice;
        private SwitchMaterial switchEnable;

//        private ItemDeviceBinding binding;

        DeviceViewHolder(ItemDeviceBinding binding) {
            super(binding.getRoot());
            cardDevice = binding.cardDevice;
            deviceId = binding.deviceId;
            isSelected = binding.isSelected;
            deviceName = binding.deviceName;
            textStatus = binding.textStatus;
            iconConnected = binding.iconConnected;
            iconDisconnected = binding.iconDisconnected;
            iconError = binding.iconError;
            buttonEdit = binding.buttonEdit;
            switchEnable = binding.switchEnable;
        }

        private int boolToVisibility(boolean isVisible) {
            return isVisible ? View.VISIBLE : View.GONE;
        }

        void bind(Device device, OnDeviceCardInteraction callbacks) {
            Log.i(TAG, "binding " + device.getUsername() + " event - " + device.getEvent());
            deviceId.setText(String.valueOf(device.getId()));

            switchEnable.setOnCheckedChangeListener(null);
            switchEnable.setChecked(device.isEnabled());
//            switchEnable.setOnClickListener(v -> {
//                Log.d(TAG, "bind: setting enabled for " + device.getUsername());
//                callbacks.setEnabled(switchEnable.isEnabled(), device);
//            });
            switchEnable.setOnCheckedChangeListener((compoundButton, b) -> {
                Log.d(TAG, "bind: switch checked changed on " + device.getUsername() + " to - " + b);
                callbacks.setEnabled(b, device);
            });

            textStatus.setText(device.isEnabled() ? R.string.device_enabled : R.string.device_disabled);

            if (device.isSelected()) {
                isSelected.setVisibility(View.VISIBLE);
            } else {
                isSelected.setVisibility(View.GONE);
            }

            deviceName.setText(device.getUsername());
            buttonEdit.setOnClickListener(v -> {
                callbacks.edit(device);
            });
            cardDevice.setOnClickListener(v -> {
                callbacks.select(device);
            });

            DeviceEvent event = device.getEvent();
            if (event != null) {
                switch (event.getStatus()) {
                    case CONNECTED:
                        iconConnected.setVisibility(View.VISIBLE);
                        iconDisconnected.setVisibility(View.GONE);
                        break;
                    case DISCONNECTED:
                        iconDisconnected.setVisibility(View.VISIBLE);
                        iconConnected.setVisibility(View.GONE);
                        break;
                    case ERROR:
                        iconError.setVisibility(View.VISIBLE);
                        iconDisconnected.setVisibility(View.GONE);
//                        Toast.makeText(cardDevice.getContext(), event.getMessage(), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }

        public interface OnDeviceCardInteraction {
            void edit(Device device);

            void select(Device device);

            void setEnabled(boolean enabled, Device device);
        }

    }
}
