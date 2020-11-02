package com.example.carcontrollermqtt.ui.history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carcontrollermqtt.ToolbarFragment;
import com.example.carcontrollermqtt.databinding.FragmentHistoryBinding;

public class HistoryFragment extends ToolbarFragment {
    private static final String TAG = "HistoryFragment";

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private MessagesAdapter adapter;

    private ModalBottomSheet bottomSheet;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setTitle("История");

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        // Kek
        setupListeners();
        setupRecyclerView();
        subscribeObservers(view);

        bottomSheet = new ModalBottomSheet((topic, payload) -> {
            viewModel.publishMessage(topic, payload);
        });
    }

    private void showModalSheet() {
        bottomSheet.show(getParentFragmentManager(), ModalBottomSheet.TAG);
    }

    private void setupListeners() {
        binding.testSendBtn.setOnClickListener(view -> {
            showModalSheet();
        });
    }

    private void subscribeObservers(View view) {
        viewModel.observeMessagesWithDevice().observe(getViewLifecycleOwner(), wqttMessageWithDevices -> {
            Log.d(TAG, "subscribeObservers: got data " + wqttMessageWithDevices);
            adapter.submitList(wqttMessageWithDevices);
        });
    }

    private void setupRecyclerView() {
        adapter = new MessagesAdapter(() -> {
            binding.recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setAdapter(adapter);

        binding.recyclerView.setItemAnimator(new MessageItemAnimator());
    }
}