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

import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.databinding.ItemDeviceBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

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
                    oldItem.isActive() == newItem.isActive();

            Log.d(TAG, "areContentsTheSame for " + oldItem.getUsername() + "-" + newItem.getUsername() + "? - " + same +
                    "/n " + oldItem.isActive() + " - " + newItem.isActive());
            return same;
//            return false;
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
        private MaterialCardView container;

        DeviceViewHolder(ItemDeviceBinding binding) {
            super(binding.getRoot());
            container = binding.cardDevice;
            deviceId = binding.deviceId;
            isSelected = binding.isSelected;
            deviceName = binding.deviceName;
            textStatus = binding.textStatus;
            iconConnected = binding.iconConnected;
            iconDisconnected = binding.iconDisconnected;
            buttonEdit = binding.buttonEdit;
        }

        void bind(int pos, Device device, OnDeviceCardInteraction callback) {
            Log.d(TAG, "binding " + device.getUsername());
            deviceId.setText(String.valueOf(device.getId()));

            if (device.isActive()) {
                Log.d(TAG, "bind: Active device!");
                isSelected.setVisibility(View.VISIBLE);
            } else {
                isSelected.setVisibility(View.GONE);
            }

            deviceName.setText(device.getUsername());
            buttonEdit.setOnClickListener(v -> {
                callback.edit(device);
            });
            container.setOnClickListener(v -> {
                callback.select(pos, device);
            });
        }

        public interface OnDeviceCardInteraction {
            void edit(Device device);

            void select(int pos, Device device);
        }

    }
}
