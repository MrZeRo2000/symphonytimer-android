package com.romanpulov.symphonytimer.activity;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.adapter.AutoTimerDisableAdapter;
import com.romanpulov.symphonytimer.helper.MediaPlayerHelper;
import com.romanpulov.symphonytimer.helper.MediaStorageHelper;
import com.romanpulov.symphonytimer.helper.UriHelper;
import com.romanpulov.symphonytimer.model.DMTimerRec;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.romanpulov.library.view.SlideNumberPicker;

import java.io.File;


public class AddItemActivity extends ActionBarActivity {
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

    private class AddItemInputException extends Exception {

        private static final long serialVersionUID = -6523044324262630252L;

        private final TextView mItem;

        public TextView getItem() {
            return mItem;
        }

        public AddItemInputException(TextView item, String message) {
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
            actionBar.setIcon(R.drawable.tuba);
        }

        mMediaPlayerHelper = new MediaPlayerHelper(this);

        mTitleEditText = (EditText)findViewById(R.id.title_edit_text);
        mTimeTextView = (TextView)findViewById(R.id.time_text_view);
        mSoundFileTextView = (TextView) findViewById(R.id.sound_file_text);
        mImageFileButton = (ImageButton)findViewById(R.id.image_file_image_button);
        mHoursNumberPicker = (SlideNumberPicker)findViewById(R.id.hours_number_picker);
        mMinutesNumberPicker = (SlideNumberPicker)findViewById(R.id.minutes_number_picker);
        mSecondsNumberPicker = (SlideNumberPicker)findViewById(R.id.seconds_number_picker);

        mAutoTimerDisableSpinner =(Spinner)findViewById(R.id.auto_timer_disable_spinner);

        mAutoTimerDisableAdapter = new AutoTimerDisableAdapter(this, android.R.layout.simple_spinner_item);
        mAutoTimerDisableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mAutoTimerDisableSpinner.setAdapter(mAutoTimerDisableAdapter);

        editRec = getIntent().getExtras().getParcelable(EDIT_REC_NAME);
        updateEditRec();

        //don't auto show keyboard if Title is not empty
        if (editRec.mTitle != null)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void updateEditRec(){
        mEditId = editRec.mId;
        mTitleEditText.setText(editRec.mTitle);
        long hours =  editRec.mTimeSec / 3600;
        long minutes = editRec.mTimeSec % 3600 / 60;
        long seconds = editRec.mTimeSec % 60;
        mHoursNumberPicker.setValue((int) hours);
        mMinutesNumberPicker.setValue((int) minutes);
        mSecondsNumberPicker.setValue((int) seconds);
        mAutoTimerDisableSpinner.setSelection(mAutoTimerDisableAdapter.getPositionByValue(editRec.mAutoTimerDisable));

        // update sound and image controls
        updateSoundImageFromFile(editRec.mSoundFile, editRec.mImageName);
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
        rec.mAutoTimerDisable = mAutoTimerDisableAdapter.getValueBySelection(mAutoTimerDisableSpinner.getSelectedItem().toString());

        if (0 == rec.mTimeSec) {
            throw new AddItemInputException(mTimeTextView, getResources().getString(R.string.error_time_zero));
        }

        rec.mSoundFile = null != mEditSoundFile ? mEditSoundFile.getPath() : null;
        rec.mImageName = null != mEditImageFile ? mEditImageFile.getPath() : null;

        return rec;
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mEditSoundFile) {
            outState.putString(EDIT_SOUND_DATA, mEditSoundFile.getPath());
        }
        if (null != mEditImageFile) {
            outState.putString(EDIT_IMAGE_DATA, mEditImageFile.getPath());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final String soundFile = savedInstanceState.getString(EDIT_SOUND_DATA);
        final String imageFile = savedInstanceState.getString(EDIT_IMAGE_DATA);

        updateSoundImageFromFile(soundFile, imageFile);
    }

    public void onSoundFileButtonClick(View v){
        mMediaPlayerHelper.stop();
        Intent soundIntent = new Intent();
        soundIntent.setType("audio/*");
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
        imageIntent.setType("image/*");
        imageIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(imageIntent, IMAGE_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if ((requestCode == SOUND_REQ_CODE) && (resultCode == RESULT_OK)){
            mSoundFileTextView.setText(R.string.caption_loading);

            class ProcessSoundUri extends AsyncTask<Uri, Void, Pair<File, String>> {
                @Override
                protected Pair<File, String> doInBackground(Uri... params) {
                    File file = MediaStorageHelper.getInstance(getApplicationContext()).createMediaFile(MediaStorageHelper.MEDIA_TYPE_SOUND, (int)mEditId);
                    UriHelper.uriSaveToFile(getApplicationContext(), params[0], file);
                    return new Pair<>(file, getMediaFileTitle(file));
                }

                @Override
                protected void onPostExecute(Pair<File, String> p) {
                    super.onPostExecute(p);
                    mEditSoundFile = p.first;
                    mSoundFileTextView.setText(p.second);
                }
            }
            new ProcessSoundUri().execute(data.getData());
        }

        if ((requestCode == IMAGE_REQ_CODE) && (resultCode == RESULT_OK)){
            //the selected image.
            Uri uri = data.getData();

            //load and save to media storage
            mEditImageFile = MediaStorageHelper.getInstance(getApplicationContext()).createMediaFile(MediaStorageHelper.MEDIA_TYPE_IMAGE, (int)mEditId);
            UriHelper.uriSaveToFile(getApplicationContext(), uri, mEditImageFile);

            //update image from file to ensure it was loaded correctly
            mImageFileButton.setImageURI(UriHelper.fileNameToUri(getApplicationContext(), mEditImageFile.getPath()));
       }

      super.onActivityResult(requestCode, resultCode, data);
    }

    private String getMediaFileTitle(File file) {
        if ((file == null) || (!file.exists()))
            return getString(R.string.default_sound);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getPath());
        String mediaData = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        mediaMetadataRetriever.release();
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
