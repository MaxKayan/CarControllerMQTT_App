package com.example.carcontrollermqtt.ui.devices;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carcontrollermqtt.data.models.Device;
import com.example.carcontrollermqtt.databinding.FragmentDevicesBinding;
import com.example.carcontrollermqtt.ui.dialogs.DialogDeviceEdit;
import com.google.android.material.snackbar.Snackbar;

public class DevicesFragment extends Fragment {
    private static final String TAG = "DevicesFragment";
    FragmentDevicesBinding binding;
    DevicesAdapter adapter;
    private DevicesViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDevicesBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DevicesViewModel.class);

        setupListeners();
        setupRecyclerView();
        subscribeObservers(view);
    }

    private void setupRecyclerView() {
        adapter = new DevicesAdapter(new DevicesAdapter.DeviceViewHolder.OnDeviceCardInteraction() {
            @Override
            public void edit(Device device) {
                showDialog(DialogDeviceEdit.newInstance(device));
            }

            @Override
            public void select(Device device) {
                if (!device.isSelected())
                    viewModel.selectDevice(device);
            }

            @Override
            public void setEnabled(boolean enabled, Device device) {
                viewModel.setEnabledOnDevice(enabled, device);
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setAdapter(adapter);

        DevicesSwipeCallback devicesSwipeCallback = new DevicesSwipeCallback(getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                viewModel.deleteDevice(adapter.getDeviceAt(viewHolder.getAdapterPosition()));
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(devicesSwipeCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
    }

    private void setupListeners() {
        binding.fabAdd.setOnClickListener(v -> {
            DialogDeviceEdit dialog = DialogDeviceEdit.newInstance();
            showDialog(dialog);
        });
    }

    private void subscribeObservers(View view) {
        viewModel.observeDevices().observe(getViewLifecycleOwner(), devices -> {
            adapter.submitList(devices);
        });

        viewModel.observeMessages().observe(getViewLifecycleOwner(), s -> {
            Snackbar.make(view, s, Snackbar.LENGTH_LONG).show();
        });
    }

    /**
     * Performs a valid dialog opening: waits for manager's pending transactions, checks if there's
     * already a dialog opened.
     *
     * @param dialog Valid dialog instance that is ready to be opened (showed).
     */
    private void showDialog(DialogFragment dialog) {
        String tag = TAG;
        FragmentManager manager = getParentFragmentManager();
        manager.executePendingTransactions();

        if (manager.findFragmentByTag(tag) != null) {
            Log.w(TAG, "showDialog: dialog " + tag + " already exists!");
            return;
        }

        dialog.show(manager, tag);
    }
}