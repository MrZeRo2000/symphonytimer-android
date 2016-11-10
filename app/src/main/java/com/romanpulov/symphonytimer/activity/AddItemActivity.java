package com.romanpulov.symphonytimer.activity;

import com.romanpulov.symphonytimer.R;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import com.romanpulov.library.view.SlideNumberPicker;

import java.io.File;


public class AddItemActivity extends ActionBarActivity {
    public static final String EDIT_REC_NAME = "rec";

    private static final String EDIT_SOUND_DATA = "EDIT_SOUND_DATA";
    private static final String EDIT_IMAGE_DATA = "EDIT_IMAGE_DATA";

    private static int SOUND_REQ_CODE = 1;
    private static int IMAGE_REQ_CODE = 2;

    private DMTimerRec editRec;

    private long mEditId;
    private File mEditImageFile;
    private File mEditSoundFile;

    private TextView mSoundFileTextView;
    private ImageButton mImageFileButton;

    private class AddItemInputException extends Exception {

        private static final long serialVersionUID = -6523044324262630252L;

        public AddItemInputException(String message) {
            super(message);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setIcon(R.drawable.tuba);

        mSoundFileTextView = (TextView) findViewById(R.id.sound_file_text);
        mImageFileButton = (ImageButton)findViewById(R.id.image_file_image_button);

        editRec = getIntent().getExtras().getParcelable(EDIT_REC_NAME);
        updateEditRec();
    }

    private void updateEditRec(){
        mEditId = editRec.mId;
        ((EditText)findViewById(R.id.title_edit_text)).setText(editRec.mTitle);
        long hours =  editRec.mTimeSec / 3600;
        long minutes = editRec.mTimeSec % 3600 / 60;
        long seconds = editRec.mTimeSec % 60;
        ((SlideNumberPicker)findViewById(R.id.hours_number_picker)).setValue((int) hours);
        ((SlideNumberPicker)findViewById(R.id.minutes_number_picker)).setValue((int) minutes);
        ((SlideNumberPicker)findViewById(R.id.seconds_number_picker)).setValue((int) seconds);

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
        rec.mTitle = ((EditText)findViewById(R.id.title_edit_text)).getText().toString().trim();

        if (rec.mTitle.isEmpty()) {
            throw new AddItemInputException(getResources().getString(R.string.error_title_not_assigned));
        }

        String hoursString =  String.valueOf(((SlideNumberPicker)findViewById(R.id.hours_number_picker)).getValue());
        String minutesString =  String.valueOf(((SlideNumberPicker)findViewById(R.id.minutes_number_picker)).getValue());
        String secondsString =  String.valueOf(((SlideNumberPicker)findViewById(R.id.seconds_number_picker)).getValue());
        long hours = Long.valueOf(hoursString);
        long minutes = Long.valueOf(minutesString);
        long seconds = Long.valueOf(secondsString);
        rec.mTimeSec = hours * 3600 + minutes * 60 + seconds;

        if (0 == rec.mTimeSec) {
            throw new AddItemInputException(getResources().getString(R.string.error_time_zero));
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
        MediaPlayerHelper.getInstance(getApplicationContext()).stop();
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
        MediaPlayerHelper.getInstance(getApplicationContext()).stop();
        Intent soundIntent = new Intent();
        soundIntent.setType("audio/*");
        soundIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(soundIntent, SOUND_REQ_CODE);
    }

    public void onPreviewSoundFileButtonClick(View v){
        MediaPlayerHelper.getInstance(getApplicationContext()).toggleSound(mEditSoundFile == null ? null : mEditSoundFile.getPath());
    }

    public void onClearSoundFileButtonClick(View v){
        MediaPlayerHelper.getInstance(getApplicationContext()).stop();
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
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            resultIntent.putExtra(EDIT_REC_NAME, resultRec);
            setResult(RESULT_OK, resultIntent);
        } else if (findViewById(R.id.cancel_button) == v)
            setResult(RESULT_CANCELED);
        MediaPlayerHelper.getInstance(getApplicationContext()).stop();
        finish();
    }
}
