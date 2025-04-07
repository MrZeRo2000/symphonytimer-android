package com.romanpulov.symphonytimer.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.databinding.ActivityAppHostBinding;
import com.romanpulov.symphonytimer.helper.AssetsHelper;
import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.helper.PermissionRequestHelper;
import com.romanpulov.symphonytimer.model.*;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    public final static int PERMISSION_REQUEST_COPY_ASSETS = 101;
    public final static int PERMISSION_REQUEST_NOTIFICATIONS = 102;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityAppHostBinding binding;

    //interactions
    ActivityResultLauncher<Intent> mStartForOverlaysResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, R.string.notification_overlay_permission_granted, Toast.LENGTH_SHORT).show();
                    }
                }
            });

    //assets
    private AssetsHelper mAssetsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAppHostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

		//setup toolbar
        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = binding.mainNavContent.getFragment();
        NavController navController = navHostFragment.getNavController();

        mAppBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        TimerViewModel model = TimerViewModel.getInstance(getApplication());
        model.getTaskStatusChange().observe(this, taskStatus -> {
           if ((taskStatus.second == TimerViewModel.TASKS_STATUS_COMPLETED) ||
                   (taskStatus.second == TimerViewModel.TASKS_STATUS_UPDATE_COMPLETED)) {
               navController.navigate(R.id.nav_main);
           }
        });

        mAssetsHelper = new AssetsHelper(this);
        if (mAssetsHelper.isFirstRun()) {
            mAssetsHelper.clearFirstRun();
            mAssetsHelper.fillAssets();
            List<String> assets;
            if (!(assets = mAssetsHelper.getAssets()).isEmpty()) {
                //for Android 10+ permissions are not required
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mAssetsHelper.copyAssetsContent(assets);
                } else {
                    PermissionRequestHelper writeStorageRequestHelper =
                            new PermissionRequestHelper(
                                    this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (writeStorageRequestHelper.isPermissionGranted()) {
                        mAssetsHelper.copyAssetsContent(assets);
                    } else {
                        writeStorageRequestHelper.requestPermission(PERMISSION_REQUEST_COPY_ASSETS);
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                mStartForOverlaysResult.launch(intent);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if ((alarmManager != null) && (!alarmManager.canScheduleExactAlarms())) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionRequestHelper notificationRequestHelper =
                        new PermissionRequestHelper(
                                this,
                                Manifest.permission.POST_NOTIFICATIONS);
                if (!notificationRequestHelper.isPermissionGranted()) {
                    notificationRequestHelper.requestPermission(PERMISSION_REQUEST_NOTIFICATIONS);
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getOnBackPressedDispatcher().hasEnabledCallbacks()) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else {
            NavController navController = Navigation.findNavController(this, binding.mainNavContent.getId());
            return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                    || super.onSupportNavigateUp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionRequestHelper.isGrantResultSuccessful(grantResults)) {
            switch (requestCode) {
                case PERMISSION_REQUEST_COPY_ASSETS:
                    List<String> assets;
                    if ((mAssetsHelper != null) && ((assets = mAssetsHelper.getAssets()) != null)) {
                        mAssetsHelper.copyAssets(assets);
                    }
                    break;
                case PERMISSION_REQUEST_NOTIFICATIONS:
                    Toast.makeText(this, R.string.notification_notification_permission_granted, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        LoggerHelper.clearInstance();
        super.onDestroy();
    }
}
