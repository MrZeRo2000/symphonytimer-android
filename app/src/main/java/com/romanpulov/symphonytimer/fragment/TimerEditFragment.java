package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.romanpulov.symphonytimer.databinding.FragmentTimerEditBinding;

public class TimerEditFragment extends Fragment {
    private FragmentTimerEditBinding binding;

    public TimerEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTimerEditBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
    }
}