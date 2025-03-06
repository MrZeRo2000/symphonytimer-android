package com.romanpulov.symphonytimer.activity;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.adapter.AutoTimerDisableAdapter;
import com.romanpulov.symphonytimer.helper.MediaPlayerHelper;
import com.romanpulov.symphonytimer.helper.MediaStorageHelper;
import com.romanpulov.symphonytimer.helper.UriHelper;
import com.romanpulov.symphonytimer.model.DMTimerRec;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.romanpulov.library.view.SlideNumberPicker;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;


public class AddItemActivity extends AppCompatActivity implements OnSoundFileInfoDataListener {
    public static final String EDIT_REC_NAME = "rec";

    private static final String EDIT_SOUND_DATA = "EDIT_SOUND_DATA";
    private static final String EDIT_IMAGE_DATA = "EDIT_IMAGE_DATA";

    private final static int SOUND_REQ_CODE = 1;
    private final static int IMAGE_REQ_CODE = 2;

    private DMTimerRec editRec;

    private long mEditId;
    private File mEditImageFile;
    private File mEditSoundFile;

    private EditText mTitleEditText;
    private TextView mSoundFileTextView;
    private TextView mTimeTextView;
    private ImageButton mImageFileButton;
    private SlideNumberPicker mHoursNumberPicker;
    private SlideNumberPicker mMinutesNumberPicker;
    private SlideNumberPicker mSecondsNumberPicker;

    private Spinner mAutoTimerDisableSpinner;
    private AutoTimerDisableAdapter mAutoTimerDisableAdapter;

    private MediaPlayerHelper mMediaPlayerHelper;

    @Override
    public void onSoundFileInfoData(File file) {
        mEditSoundFile = file;
        mSoundFileTextView.setText(getMediaFileTitle(file));
    }

    private class AddItemInputException extends Exception {

        private static final long serialVersionUID = -6523044324262630252L;

        private final TextView mItem;

        public TextView getItem() {
            return mItem;
        }

        AddItemInputException(TextView item, String message) {
            super(message);
            mItem = item;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
        }

        mMediaPlayerHelper = new MediaPlayerHelper(this);

        mTitleEditText = findViewById(R.id.title_edit_text);
        mTimeTextView = findViewById(R.id.time_text_view);
        mSoundFileTextView = findViewById(R.id.sound_file_text);
        mImageFileButton = findViewById(R.id.image_file_image_button);
        mHoursNumberPicker = findViewById(R.id.hours_number_picker);
        mMinutesNumberPicker = findViewById(R.id.minutes_number_picker);
        mSecondsNumberPicker = findViewById(R.id.seconds_number_picker);

        mAutoTimerDisableSpinner = findViewById(R.id.auto_timer_disable_spinner);

        mAutoTimerDisableAdapter = new AutoTimerDisableAdapter(this, android.R.layout.simple_spinner_item);
        mAutoTimerDisableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mAutoTimerDisableSpinner.setAdapter(mAutoTimerDisableAdapter);

        editRec = getIntent().getExtras().getParcelable(EDIT_REC_NAME);
        updateEditRec();

        //don't auto show keyboard if Title is not empty
        if (editRec.getTitle() != null)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void updateEditRec(){
        mEditId = editRec.getId();
        mTitleEditText.setText(editRec.getTitle());
        long hours =  editRec.getTimeSec() / 3600;
        long minutes = editRec.getTimeSec() % 3600 / 60;
        long seconds = editRec.getTimeSec() % 60;
        mHoursNumberPicker.setValue((int) hours);
        mMinutesNumberPicker.setValue((int) minutes);
        mSecondsNumberPicker.setValue((int) seconds);
        mAutoTimerDisableSpinner.setSelection(mAutoTimerDisableAdapter.getPositionByValue(editRec.getAutoTimerDisableInterval()));

        // update sound and image controls
        updateSoundImageFromFile(editRec.getSoundFile(), editRec.getImageName());
    }

    private void updateSoundImageFromFile(String soundFile, String imageFile) {
        if (null != soundFile) {
            mEditSoundFile = new File(soundFile);
            if (mEditSoundFile.exists())
                mSoundFileTextView.setText(getMediaFileTitle(mEditSoundFile));
            else
                mEditSoundFile = null;
        } else {
            mEditSoundFile = null;
        }
        mSoundFileTextView.setText(getMediaFileTitle(mEditSoundFile));

        if (null != imageFile) {
            mEditImageFile = new File(imageFile);
            if (mEditImageFile.exists())
                mImageFileButton.setImageURI(UriHelper.fileNameToUri(getApplicationContext(), mEditImageFile.getPath()));
            else
                mEditImageFile = null;
        } else
            mEditImageFile = null;
    }

    private DMTimerRec getEditRec() throws AddItemInputException {
        /*
        DMTimerRec rec = new DMTimerRec();
        rec.mId = mEditId;
        rec.mTitle = mTitleEditText.getText().toString().trim();

        if (rec.mTitle.isEmpty()) {
            throw new AddItemInputException(mTitleEditText, getResources().getString(R.string.error_title_not_assigned));
        }

        String hoursString =  String.valueOf(mHoursNumberPicker.getValue());
        String minutesString =  String.valueOf(mMinutesNumberPicker.getValue());
        String secondsString =  String.valueOf(mSecondsNumberPicker.getValue());
        long hours = Long.valueOf(hoursString);
        long minutes = Long.valueOf(minutesString);
        long seconds = Long.valueOf(secondsString);
        rec.mTimeSec = hours * 3600 + minutes * 60 + seconds;
        rec.mAutoTimerDisableInterval = mAutoTimerDisableAdapter.getValueBySelection(mAutoTimerDisableSpinner.getSelectedItem().toString());

        if (0 == rec.mTimeSec) {
            throw new AddItemInputException(mTimeTextView, getResources().getString(R.string.error_time_zero));
        }

        rec.mSoundFile = null != mEditSoundFile ? mEditSoundFile.getPath() : null;
        rec.mImageName = null != mEditImageFile ? mEditImageFile.getPath() : null;

        return rec;

         */
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.add_item, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaPlayerHelper.stop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mEditSoundFile) {
            outState.putString(EDIT_SOUND_DATA, mEditSoundFile.getPath());
        }
        if (null != mEditImageFile) {
            outState.putString(EDIT_IMAGE_DATA, mEditImageFile.getPath());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final String soundFile = savedInstanceState.getString(EDIT_SOUND_DATA);
        final String imageFile = savedInstanceState.getString(EDIT_IMAGE_DATA);

        updateSoundImageFromFile(soundFile, imageFile);
    }

    public void onSoundFileButtonClick(View v){
        mMediaPlayerHelper.stop();
        Intent soundIntent = new Intent();
        soundIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "audio/*");
        soundIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(soundIntent, SOUND_REQ_CODE);
    }

    public void onPreviewSoundFileButtonClick(View v){
        mMediaPlayerHelper.toggleSound(mEditSoundFile == null ? null : mEditSoundFile.getPath());
    }

    public void onClearSoundFileButtonClick(View v){
        mMediaPlayerHelper.stop();
        mEditSoundFile = null;
        mSoundFileTextView.setText(R.string.default_sound);
    }

    public void onClearImageFileButtonClick(View v){
        mEditImageFile = null;
        mImageFileButton.setImageResource(R.drawable.btn_check_off);
    }

    public void onImageFileImageButtonClick(View v) {
        Intent imageIntent = new Intent();
        imageIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        imageIntent.setAction(Intent.ACTION_PICK);
        startActivityForResult(imageIntent, IMAGE_REQ_CODE);
    }

    static class ProcessSoundUri extends AsyncTask<Uri, Void, File> {
        private final WeakReference<Context> mContext;
        private final int mMediaId;

        ProcessSoundUri(Context context, int mediaId) {
            mContext = new WeakReference<>(context);
            mMediaId = mediaId;
        }

        @Override
        protected File doInBackground(Uri... params) {
            File file = MediaStorageHelper.getInstance(mContext.get().getApplicationContext()).createMediaFile(MediaStorageHelper.MEDIA_TYPE_SOUND, mMediaId);
            UriHelper.uriSaveToFile(mContext.get().getApplicationContext(), params[0], file);
            return file;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (mContext.get() instanceof OnSoundFileInfoDataListener) {
                ((OnSoundFileInfoDataListener)(mContext.get())).onSoundFileInfoData(file);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if ((requestCode == SOUND_REQ_CODE) && (resultCode == RESULT_OK)){
            mSoundFileTextView.setText(R.string.caption_loading);

            new ProcessSoundUri(this, (int) mEditId).execute(data.getData());
        }

        if ((requestCode == IMAGE_REQ_CODE) && (resultCode == RESULT_OK)){
            //the selected image.
            Uri uri = data.getData();

            //load and save to media storage
            mEditImageFile = MediaStorageHelper.getInstance(getApplicationContext()).createMediaFile(MediaStorageHelper.MEDIA_TYPE_IMAGE, (int)mEditId);
            if (UriHelper.uriSaveToFile(getApplicationContext(), uri, mEditImageFile)) {
                //update image from file to ensure it was loaded correctly
                mImageFileButton.setImageURI(UriHelper.fileNameToUri(getApplicationContext(), mEditImageFile.getPath()));
            } else {
                if (mEditImageFile.exists()) {
                    mEditImageFile.delete();
                }
            }
       }

      super.onActivityResult(requestCode, resultCode, data);
    }

    private String getMediaFileTitle(File file) {
        if ((file == null) || (!file.exists()))
            return getString(R.string.default_sound);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getPath());
        String mediaData = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        try {
            mediaMetadataRetriever.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mediaData == null)
            return getString(R.string.unknown_sound);
        else
            return mediaData;
    }

    public void onCommandClick(View v) {
        if (findViewById(R.id.ok_button) == v) {
            Intent resultIntent = new Intent();
            DMTimerRec resultRec;
            try {
                resultRec = getEditRec();
            } catch (AddItemInputException e) {
                if (!(e.getItem() instanceof EditText))
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                else
                    e.getItem().setError(e.getMessage());
                return;
            }
            resultIntent.putExtra(EDIT_REC_NAME, resultRec);
            setResult(RESULT_OK, resultIntent);
        } else if (findViewById(R.id.cancel_button) == v)
            setResult(RESULT_CANCELED);
        mMediaPlayerHelper.stop();
        finish();
    }
}
