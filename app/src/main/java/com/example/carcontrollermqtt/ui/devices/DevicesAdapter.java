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
import com.example.carcontrollermqtt.databinding.ItemDeviceBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class DevicesAdapter extends ListAdapter<Device, DevicesAdapter.DeviceViewHolder> {
    private static final String TAG = "DevicesAdapter";
    private static final DiffUtil.ItemCallback<Device> DIFF_CALLBACK = new DiffUtil.ItemCallback<Device>() {
        @Override
        public boolean areItemsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
            boolean same = oldItem.getUsername().equals(newItem.getUsername()) &&
                    oldItem.isEnabled() == newItem.isEnabled() &&
                    oldItem.isSelected() == newItem.isSelected();

            return same;
        }
    };
    private final DeviceViewHolder.OnDeviceCardInteraction editClickCallback;

    public DevicesAdapter(DeviceViewHolder.OnDeviceCardInteraction editClickCallback) {
        super(DIFF_CALLBACK);
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
        holder.bind(position, currentItem, editClickCallback);
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceId, deviceName, textStatus;
        private ImageView iconConnected, iconDisconnected;
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
            buttonEdit = binding.buttonEdit;
            switchEnable = binding.switchEnable;
        }

        void bind(int pos, Device device, OnDeviceCardInteraction callbacks) {
            Log.d(TAG, "binding " + device.getUsername());
            deviceId.setText(String.valueOf(device.getId()));

            switchEnable.setChecked(device.isEnabled());
            switchEnable.setOnClickListener(v -> {
                Log.d(TAG, "bind: setting enabled for " + device.getUsername());
                callbacks.setEnabled(switchEnable.isEnabled(), device);
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
        }

        public interface OnDeviceCardInteraction {
            void edit(Device device);

            void select(Device device);

            void setEnabled(boolean enabled, Device device);
        }

    }
}
