package com.example.carcontrollermqtt.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carcontrollermqtt.databinding.FragmentHistoryBinding;

public class HistoryFragment extends Fragment {
    private static final String TAG = "HistoryFragment";

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private MessagesAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        setupListeners();
        setupRecyclerView();
        subscribeObservers(view);
    }

    private void setupListeners() {
        binding.testSendBtn.setOnClickListener(view -> {
            viewModel.publishMessage("/androidApp", "This is a test message sent from the Android app.");
        });
    }

    private void subscribeObservers(View view) {
        viewModel.observeMessagesWithDevice().observe(getViewLifecycleOwner(), wqttMessageWithDevices -> {
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
    }
}