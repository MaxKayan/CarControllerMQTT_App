package com.example.carcontrollermqtt.ui.history;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carcontrollermqtt.data.models.DqttMessage;
import com.example.carcontrollermqtt.data.models.transactions.DqttMessageWithDevice;
import com.example.carcontrollermqtt.databinding.ItemMessageBinding;
import com.example.carcontrollermqtt.service.DqttMessageManager;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MessagesAdapter extends ListAdapter<DqttMessageWithDevice, MessagesAdapter.MessageViewHolder> {
    public static final DiffUtil.ItemCallback<DqttMessageWithDevice> DEVICE_ITEM_CALLBACK = new DiffUtil.ItemCallback<DqttMessageWithDevice>() {
        @Override
        public boolean areItemsTheSame(@NonNull DqttMessageWithDevice oldItem, @NonNull DqttMessageWithDevice newItem) {
            return oldItem.message.getId() == newItem.message.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DqttMessageWithDevice oldItem, @NonNull DqttMessageWithDevice newItem) {
            boolean same = oldItem.message.isIncoming() == newItem.message.isIncoming() &&
                    oldItem.message.getTopic().equals(newItem.message.getTopic()) &&
                    Objects.equals(oldItem.message.getDeviceId(), newItem.message.getDeviceId()) &&
                    oldItem.message.getDateTime().equals(newItem.message.getDateTime()) &&
                    // FIXME status change callback comes very fast so it breaks the slide-in anim
//                    oldItem.message.getStatus().equals(newItem.message.getStatus()) &&
                    oldItem.message.getPayload().equals(newItem.message.getPayload());

            return same;
        }
    };
    private static final String TAG = "MessagesAdapter";
    private final ListChangedCallback changedCallback;

    public MessagesAdapter(ListChangedCallback callback) {
        super(DEVICE_ITEM_CALLBACK);
        changedCallback = callback;
    }

    public DqttMessageWithDevice getDeviceAt(int position) {
        return getItem(position);
    }

    @Override
    public void onCurrentListChanged(@NonNull List<DqttMessageWithDevice> previousList, @NonNull List<DqttMessageWithDevice> currentList) {
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
        DqttMessageWithDevice currentItem = getItem(position);
        holder.bind(currentItem);
    }

    public interface ListChangedCallback {
        void run();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        boolean isIncoming;
        ItemMessageBinding binding;

        MessageViewHolder(ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DqttMessageWithDevice transaction) {
            isIncoming = transaction.message.isIncoming();

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.cardMessage.getLayoutParams();
            if (isIncoming) {
                layoutParams.rightMargin = 128;
                layoutParams.leftMargin = 16;
                binding.iconIncoming.setVisibility(View.VISIBLE);
                binding.iconOutgoing.setVisibility(View.GONE);
            } else {
                layoutParams.rightMargin = 16;
                layoutParams.leftMargin = 128;
                binding.iconOutgoing.setVisibility(View.VISIBLE);
                binding.iconIncoming.setVisibility(View.GONE);
                setDeliveryStatus(transaction.message.getStatus());
            }
            binding.cardMessage.setLayoutParams(layoutParams);

            binding.deviceName.setText(transaction.device != null ? transaction.device.getUsername() : "Удалёно");
            String topic = transaction.message.getTopic();
            String[] topicArray = transaction.message.getTopic().split("/");
            Log.d(TAG, "bind: topicArray: " + Arrays.toString(topicArray));
            binding.topic.setText(topicArray.length <= 1 ? topic : DqttMessageManager.getEndpointTopic(topic));
            binding.dateTime.setText(transaction.message.getDateTime().toLocaleString());
            binding.payload.setText(transaction.message.getPayload().trim());
        }

        private void setDeliveryStatus(DqttMessage.MessageStatus status) {
            switch (status) {
                case PENDING:
                    binding.iconStatusSent.setVisibility(View.VISIBLE);
                    break;
                case DELIVERED:
                    binding.iconStatusDelivered.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
