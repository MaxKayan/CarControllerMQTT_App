package com.example.carcontrollermqtt.service;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;

import com.example.carcontrollermqtt.data.models.Device;

import java.util.List;

public class DqttClientDiffUtil {
    private static final String TAG = "WqttClientDiffUtil";

    private static final DiffUtil.ItemCallback<Device> DEVICE_ITEM_CALLBACK = new DiffUtil.ItemCallback<Device>() {
        @Override
        public boolean areItemsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
            boolean same = oldItem.getUsername().equals(newItem.getUsername()) &&
                    oldItem.getPassword().equals(newItem.getPassword()) &&
                    oldItem.getEnabled() == newItem.getEnabled();

            return same;
        }
    };

    private final AsyncListDiffer<Device> mDiffer;

    private final DqttClientCallbacks callbacks;

    public DqttClientDiffUtil(DqttClientCallbacks callbacks) {
        this.callbacks = callbacks;
        mDiffer = new AsyncListDiffer<>(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.d(TAG, "onInserted: " + position + " count: " + count);
                initiateDevices(position, position + count - 1);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d(TAG, "onRemoved: " + position + "count: " + count);
                Log.d(TAG, "onRemoved: List size now is - " + mDiffer.getCurrentList().size());
//                removeDevices(position, position + count - 1);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.d(TAG, "onMoved: from - " + fromPosition + " to - " + toPosition);
            }

            @Override
            public void onChanged(int position, int count, @Nullable Object payload) {
                Log.d(TAG, "onChanged: at " + position + " count: " + count);
                reInitiateDevices(position, position + count - 1);
            }
        },
                new AsyncDifferConfig.Builder<>(DEVICE_ITEM_CALLBACK).build());
    }

    public void submitList(List<Device> newList) {
        mDiffer.submitList(newList);
    }

    public Device getItem(int position) {
        return mDiffer.getCurrentList().get(position);
    }

    private void initiateDevices(int fromPosition, int toPosition) {
        Log.d(TAG, "initiateDevices: from " + fromPosition + " to " + toPosition);
        for (int i = fromPosition; i <= toPosition; i++) {
            callbacks.initiateDevice(getItem(i));
        }
    }

    private void removeDevices(int fromPosition, int toPosition) {
        for (int i = fromPosition; i <= toPosition; i++) {
            callbacks.removeDevice(getItem(i));
        }
    }

    private void reInitiateDevices(int fromPosition, int toPosition) {
        for (int i = fromPosition; i <= toPosition; i++) {
            Device device = getItem(i);
            callbacks.removeDevice(device);
            callbacks.initiateDevice(device);
        }
    }

    public interface DqttClientCallbacks {
        void initiateDevice(Device device);

        void removeDevice(Device device);
    }
}
