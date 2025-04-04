package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.databinding.FragmentHistoryBinding;
import com.romanpulov.symphonytimer.model.DMTimers;
import com.romanpulov.symphonytimer.model.TimerHistoryViewModel;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class HistoryFragment extends Fragment {
    private static final String TAG = HistoryFragment.class.getSimpleName();
    private static final String ARG_TIMERS = "timers";
    private static final String ARG_HISTORY_FILTER_ID = "history_filter_id";

    //underlying fragments for creation
    private final static List<Class<? extends Fragment>> HISTORY_FRAGMENT_CLASS_LIST =
            Arrays.asList(
                    HistoryListFragment.class,
                    HistoryTopChartFragment.class,
                    HistoryDynamicsChartFragment.class
            );

    private FragmentHistoryBinding binding;

    protected DMTimers mDMTimers;
    protected int mHistoryFilterId = -1;

    private static class HistoryFragmentStateAdapter extends FragmentStateAdapter {
        public HistoryFragmentStateAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return HistoryFragment.newInstance(HISTORY_FRAGMENT_CLASS_LIST.get(position));
        }

        @Override
        public int getItemCount() {
            return HISTORY_FRAGMENT_CLASS_LIST.size();
        }
    }

	@NonNull
    public static <T extends Fragment> T newInstance(Class<T> historyFragmentClass) {
        T fragment;
        try {
            fragment = historyFragmentClass.getDeclaredConstructor().newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            Log.e(TAG, "Error creating fragment from class", e);
            throw new RuntimeException(e.getMessage());
        }
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

        HistoryFragmentStateAdapter fragmentStateAdapter = new HistoryFragmentStateAdapter(this);
        binding.pager.setAdapter(fragmentStateAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.pager, (tab, position) -> {
            String tabText = position == 0 ? requireContext().getResources().getString(R.string.tab_history_list) :
                position == 1 ? requireContext().getResources().getString(R.string.tab_history_top) :
                position == 2 ? requireContext().getResources().getString(R.string.tab_history_dynamics) :
                "";
            tab.setText(tabText);
        }).attach();

        final TimerHistoryViewModel model = new ViewModelProvider(this).get(TimerHistoryViewModel.class);
        model.getFilterId().observe(this, filterId -> model.loadDMTimerHistList());

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
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        model.setFilterId(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }
}
