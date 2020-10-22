package com.example.carcontrollermqtt.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carcontrollermqtt.data.models.transactions.WqttMessageWithDevice;
import com.example.carcontrollermqtt.databinding.ItemMessageBinding;

import java.util.List;

public class MessagesAdapter extends ListAdapter<WqttMessageWithDevice, MessagesAdapter.MessageViewHolder> {
    public static final DiffUtil.ItemCallback<WqttMessageWithDevice> DEVICE_ITEM_CALLBACK = new DiffUtil.ItemCallback<WqttMessageWithDevice>() {
        @Override
        public boolean areItemsTheSame(@NonNull WqttMessageWithDevice oldItem, @NonNull WqttMessageWithDevice newItem) {
            return oldItem.message.getId() == newItem.message.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull WqttMessageWithDevice oldItem, @NonNull WqttMessageWithDevice newItem) {
            boolean same = oldItem.message.isIncoming() == newItem.message.isIncoming() &&
                    oldItem.message.getTopic().equals(newItem.message.getTopic()) &&
                    oldItem.message.getDeviceId() == newItem.message.getDeviceId() &&
                    oldItem.message.getDateTime().equals(newItem.message.getDateTime()) &&
                    oldItem.message.getPayload().equals(newItem.message.getPayload());

            return same;
        }
    };
    private ListChangedCallback changedCallback;

    public MessagesAdapter(ListChangedCallback callback) {
        super(DEVICE_ITEM_CALLBACK);
        changedCallback = callback;
    }

    public WqttMessageWithDevice getDeviceAt(int position) {
        return getItem(position);
    }

    @Override
    public void onCurrentListChanged(@NonNull List<WqttMessageWithDevice> previousList, @NonNull List<WqttMessageWithDevice> currentList) {
        super.onCurrentListChanged(previousList, currentList);
        changedCallback.run();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMessageBinding binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        WqttMessageWithDevice currentItem = getItem(position);
        holder.bind(currentItem);
    }

    public interface ListChangedCallback {
        void run();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        ItemMessageBinding binding;

        MessageViewHolder(ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(WqttMessageWithDevice transaction) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.cardMessage.getLayoutParams();
            if (transaction.message.isIncoming()) {
                layoutParams.rightMargin = 128;
                layoutParams.leftMargin = 16;
//                binding.cardMessage;
                binding.iconIncoming.setVisibility(View.VISIBLE);
                binding.iconOutgoing.setVisibility(View.GONE);
            } else {
                layoutParams.rightMargin = 16;
                layoutParams.leftMargin = 128;
                binding.iconOutgoing.setVisibility(View.VISIBLE);
                binding.iconIncoming.setVisibility(View.GONE);
            }
            binding.cardMessage.setLayoutParams(layoutParams);

            binding.deviceName.setText(transaction.device.getUsername());
            binding.topic.setText(transaction.message.getTopic());
            binding.dateTime.setText(transaction.message.getDateTime().toLocaleString());
            binding.payload.setText(transaction.message.getPayload().trim());
        }

    }
}
