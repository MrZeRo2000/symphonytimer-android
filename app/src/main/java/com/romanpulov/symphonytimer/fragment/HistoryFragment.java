package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;

import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.Lifecycle;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.databinding.FragmentHistoryBinding;
import com.romanpulov.symphonytimer.model.DMTimers;

import java.lang.reflect.InvocationTargetException;

public class HistoryFragment extends Fragment {
    private static final String TAG = HistoryFragment.class.getSimpleName();
    private static final String ARG_TIMERS = "timers";
    private static final String ARG_HISTORY_FILTER_ID = "history_filter_id";

    private FragmentHistoryBinding binding;

    protected DMTimers mDMTimers;
    protected int mHistoryFilterId = -1;
	protected ArrayAdapter<?> mAdapter;

	@NonNull
    public static <T extends HistoryFragment> T newInstance(Class<T> historyFragmentClass, DMTimers dmTimers, int historyFilterId) {
        T fragment;
        try {
            fragment = historyFragmentClass.getDeclaredConstructor().newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            Log.e(TAG, "Error creating fragment from class", e);
            throw new RuntimeException(e.getMessage());
        }
        Bundle args = new Bundle();
        args.putParcelable(ARG_TIMERS, dmTimers);
        args.putInt(ARG_HISTORY_FILTER_ID, historyFilterId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDMTimers = getArguments().getParcelable(ARG_TIMERS);
            mHistoryFilterId = getArguments().getInt(ARG_HISTORY_FILTER_ID);
        }
    }
	
	public void setHistoryFilterId(int historyFilterId) {
		mHistoryFilterId = historyFilterId;
	}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.history_options, menu);

                MenuItem item = menu.findItem(R.id.spinner);
                Spinner spinner = (Spinner) item.getActionView();
                ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                        requireContext(), R.array.history_filter, android.R.layout.simple_spinner_item);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }
}
