package com.example.carcontrollermqtt.ui.fragments.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.carcontrollermqtt.databinding.DialogNewMessageBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ModalBottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = "ModalBottomSheet";
    private DialogNewMessageBinding binding;

    private OnSubmitMessage callback;

    public ModalBottomSheet(OnSubmitMessage callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogNewMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.send.setOnClickListener(v -> {
            callback.send(
                    String.valueOf(binding.topicEditText.getText()),
                    String.valueOf(binding.payloadEditText.getText())
            );
            dismiss();
        });
    }

    public interface OnSubmitMessage {
        void send(String topic, String payload);
    }
}
