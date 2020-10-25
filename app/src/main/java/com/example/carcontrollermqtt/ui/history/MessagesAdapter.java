package com.example.carcontrollermqtt.ui.history;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carcontrollermqtt.R;
import com.example.carcontrollermqtt.data.models.WqttMessage;
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
                    oldItem.message.getDeviceId().equals(newItem.message.getDeviceId()) &&
                    oldItem.message.getDateTime().equals(newItem.message.getDateTime()) &&
                    // FIXME status change callback comes very fast so it breaks the slide-in anim
//                    oldItem.message.getStatus().equals(newItem.message.getStatus()) &&
                    oldItem.message.getPayload().equals(newItem.message.getPayload());

            return same;
        }
    };
    private static final String TAG = "MessagesAdapter";
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
        boolean isNew = position == getItemCount() - 1;
        Log.d(TAG, "onBindViewHolder: " + position + " isNew = " + isNew);
        holder.bind(currentItem, isNew);
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

        void bind(WqttMessageWithDevice transaction, boolean isNew) {
            isIncoming = transaction.message.isIncoming();

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.cardMessage.getLayoutParams();
            if (isIncoming) {
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
                setDeliveryStatus(transaction.message.getStatus());
            }
            binding.cardMessage.setLayoutParams(layoutParams);

            binding.deviceName.setText(transaction.device.getUsername());
            binding.topic.setText(transaction.message.getTopic());
            binding.dateTime.setText(transaction.message.getDateTime().toLocaleString());
            binding.payload.setText(transaction.message.getPayload().trim());
        }

        private void playAnimation(boolean isNew, boolean isIncoming) {
            if (!isNew) return;

            Context context = binding.cardMessage.getContext();
            binding.cardMessage.startAnimation(AnimationUtils.loadAnimation(context, isIncoming ? R.anim.slidein_from_left : R.anim.slidein_from_right));
        }

        private void setDeliveryStatus(WqttMessage.MessageStatus status) {
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
