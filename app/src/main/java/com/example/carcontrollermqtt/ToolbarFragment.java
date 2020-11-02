package com.example.carcontrollermqtt;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.appbar.MaterialToolbar;

public abstract class ToolbarFragment extends Fragment {
    @Nullable
    protected MaterialToolbar toolbar;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        toolbar = view.findViewById(R.id.toolbar);
        FragmentActivity fragmentActivity = getActivity();
        if (toolbar != null && fragmentActivity instanceof MainActivity) {
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_nav) {
                    ((MainActivity) fragmentActivity).openDrawer();
                    return true;
                }
                return false;
            });
            toolbar.setOnClickListener(v -> {
                ((MainActivity) fragmentActivity).openDrawer();
            });
        }

        super.onViewCreated(view, savedInstanceState);
    }
}
