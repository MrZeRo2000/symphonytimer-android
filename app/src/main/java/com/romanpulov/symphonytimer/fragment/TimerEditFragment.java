package com.romanpulov.symphonytimer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.navigation.Navigation;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.adapter.AutoTimerDisableAdapter;
import com.romanpulov.symphonytimer.databinding.FragmentTimerEditBinding;
import com.romanpulov.symphonytimer.helper.MediaPlayerHelper;
import com.romanpulov.symphonytimer.helper.MediaStorageHelper;
import com.romanpulov.symphonytimer.helper.UriHelper;
import com.romanpulov.symphonytimer.model.DMTimerRec;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimerEditFragment extends Fragment {
    private static final String TAG = TimerEditFragment.class.getSimpleName();

    public static final String MEDIA_ID_KEY = TimerEditFragment.class.getName() + "_MEDIA_ID";
    public static final String RESULT_KEY = TimerEditFragment.class.getName() + "_RESULT_KEY";
    public static final String RESULT_VALUE_KEY = TimerEditFragment.class.getName() + "_RESULT_VALUE_KEY";

    private FragmentTimerEditBinding binding;

    private AutoTimerDisableAdapter mAutoTimerDisableAdapter;
    private MediaPlayerHelper mMediaPlayerHelper;

    private File mEditImageFile;
    private File mEditSoundFile;

    private ExecutorService mExecutor;

    private final ActivityResultLauncher<Intent> mMediaPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data;
                if ((data = result.getData()) != null) {
                    Uri uri = data.getData();
                    String mimeType = requireContext().getContentResolver().getType(uri);

                    Log.d(TAG, "onActivityResult: " + result.getResultCode() +
                            ", MIME type:" + mimeType);

                    int mediaType = MediaStorageHelper.mediaTypeFromMIMEType(mimeType);
                    if (mediaType > -1) {
                        int mediaId = data.getIntExtra(MEDIA_ID_KEY, 0);
                        File mediaFile =  MediaStorageHelper.getInstance(requireContext()).createMediaFile(mediaType, mediaId);

                        if (mExecutor != null) {
                            mExecutor.shutdown();
                            try {
                                if (!mExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                                    return;
                                }
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Interrupted exception: " + e.getMessage(), e);
                            }
                        } else {
                            mExecutor = Executors.newSingleThreadExecutor();
                        }
                        mExecutor.submit(() -> {
                            Log.d(TAG, "saving URI " + uri + " to file " + mediaFile.getAbsolutePath());
                            boolean saveResult = UriHelper.uriSaveToFile(requireContext(), uri, mediaFile);
                            if (!saveResult && mediaFile.exists()) {
                                mediaFile.delete();
                            } else {
                                Activity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(() -> {
                                       if (mediaType == MediaStorageHelper.MEDIA_TYPE_IMAGE) {
                                           mEditImageFile = mediaFile;
                                           binding.imageFileImageButton.setImageURI(UriHelper.fileNameToUri(activity, mEditImageFile.getPath()));
                                       } else if (mediaType == MediaStorageHelper.MEDIA_TYPE_SOUND) {
                                           mEditSoundFile = mediaFile;
                                           binding.soundFileText.setText(getMediaFileTitle(mEditSoundFile));
                                       }
                                    });
                                }
                            }
                        });
                    }
                }
            }
    );

    private static class AddItemInputException extends Exception {
        private final TextView mItem;
        public TextView getItem() {
            return mItem;
        }

        AddItemInputException(TextView item, String message) {
            super(message);
            mItem = item;
        }
    }


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

        mMediaPlayerHelper = new MediaPlayerHelper(requireContext());

        mAutoTimerDisableAdapter = new AutoTimerDisableAdapter(requireContext(), android.R.layout.simple_spinner_item);
        mAutoTimerDisableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.autoTimerDisableSpinner.setAdapter(mAutoTimerDisableAdapter);

        final DMTimerRec editItem = TimerEditFragmentArgs.fromBundle(getArguments()).getEditItem();
        if (editItem == null) {
            updateSoundImageFromFile(null, null);
        } else {
            binding.titleEditText.setText(editItem.mTitle);

            long hours =  editItem.mTimeSec / 3600;
            long minutes = editItem.mTimeSec % 3600 / 60;
            long seconds = editItem.mTimeSec % 60;
            binding.hoursNumberPicker.setValue((int) hours);
            binding.minutesNumberPicker.setValue((int) minutes);
            binding.secondsNumberPicker.setValue((int) seconds);

            binding.autoTimerDisableSpinner.setSelection(
                    mAutoTimerDisableAdapter.getPositionByValue(editItem.mAutoTimerDisableInterval));

            // update sound and image controls
            updateSoundImageFromFile(editItem.mSoundFile, editItem.mImageName);
        }

        binding.imageFileImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (editItem != null) {
                intent.putExtra(MEDIA_ID_KEY, editItem.mId);
            }
            mMediaPickerLauncher.launch(intent);
        });

        binding.okButton.setOnClickListener(v -> {
            DMTimerRec resultRec;
            try {
                resultRec = getEditRec();
            } catch (AddItemInputException e) {
                if (!(e.getItem() instanceof EditText))
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                else
                    e.getItem().setError(e.getMessage());
                return;
            }

            mMediaPlayerHelper.stop();

            Bundle result = new Bundle();

            if (editItem != null) {
                resultRec.mId = editItem.mId;
            }
            result.putParcelable(RESULT_VALUE_KEY, resultRec);
            getParentFragmentManager().setFragmentResult(RESULT_KEY, result);

            Navigation.findNavController(requireView()).navigateUp();
        });

    }

    private void updateSoundImageFromFile(String soundFile, String imageFile) {
        if (null != soundFile) {
            mEditSoundFile = new File(soundFile);
            if (!mEditSoundFile.exists()) {
                mEditSoundFile = null;
            }
        } else {
            mEditSoundFile = null;
        }
        binding.soundFileText.setText(getMediaFileTitle(mEditSoundFile));

        if (null != imageFile) {
            mEditImageFile = new File(imageFile);
            if (mEditImageFile.exists())
                binding.imageFileImageButton.setImageURI(UriHelper.fileNameToUri(requireContext(), mEditImageFile.getPath()));
            else
                mEditImageFile = null;
        } else
            mEditImageFile = null;
    }

    private String getMediaFileTitle(File file) {
        if ((file == null) || (!file.exists())) {
            return getString(R.string.default_sound);
        }

        try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
            mediaMetadataRetriever.setDataSource(file.getPath());
            String mediaData = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

            return Optional.ofNullable(mediaData).orElse(getString(R.string.unknown_sound));
        } catch (IOException e) {
            Log.e(TAG, "Error retrieving metadata for media file", e);
            return getString(R.string.unknown_sound);
        }
    }

    private DMTimerRec getEditRec() throws AddItemInputException {
        DMTimerRec rec = new DMTimerRec();
        //rec.mId = mEditId;
        rec.mTitle = binding.titleEditText.getText().toString().trim();

        if (rec.mTitle.isEmpty()) {
            throw new AddItemInputException(binding.titleEditText, getResources().getString(R.string.error_title_not_assigned));
        }

        int hours = binding.hoursNumberPicker.getValue();
        int minutes = binding.minutesNumberPicker.getValue();
        int seconds = binding.secondsNumberPicker.getValue();
        rec.mTimeSec = (long)hours * 3600 + (long)minutes * 60 + seconds;
        rec.mAutoTimerDisableInterval = mAutoTimerDisableAdapter.getValueBySelection(
                binding.autoTimerDisableSpinner.getSelectedItem().toString());

        if (0 == rec.mTimeSec) {
            throw new AddItemInputException(binding.timeTextView, getResources().getString(R.string.error_time_zero));
        }

        rec.mSoundFile = null != mEditSoundFile ? mEditSoundFile.getPath() : null;
        rec.mImageName = null != mEditImageFile ? mEditImageFile.getPath() : null;

        return rec;
    }
}