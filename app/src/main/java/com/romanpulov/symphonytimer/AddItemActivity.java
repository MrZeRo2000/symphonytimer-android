package com.romanpulov.symphonytimer;

import com.romanpulov.symphonytimer.R;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.EditText;


public class AddItemActivity extends ActionBarActivity {
	public static final String EDIT_REC_NAME = "rec";
	
	private static final String EDIT_SOUND_URI = "EDIT_SOUND_URI";
	private static final String EDIT_IMAGE_URI = "EDIT_IMAGE_URI";
	
	private static int SOUND_REQ_CODE = 1;
	private static int IMAGE_REQ_CODE = 2;
	
	private DMTimerRec editRec;
	
	private long mEditId;
	private Uri mEditSoundURI;
	private Uri mEditImageURI;
	
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

		
		editRec = getIntent().getExtras().getParcelable(EDIT_REC_NAME);
		updateEditRec();
		//Toast.makeText(this, tr.title, Toast.LENGTH_SHORT).show();
	}
	
	private final void updateEditRec(){
		mEditId = editRec.mId;
		((EditText)findViewById(R.id.title_edit_text)).setText(editRec.mTitle);
		long hours =  (long) editRec.mTimeSec / 3600;
		long minutes = (long) editRec.mTimeSec % 3600 / 60;
		long seconds = (long) editRec.mTimeSec % 60;
		((EditText)findViewById(R.id.hours_edit_text)).setText(String.valueOf(hours));
		((EditText)findViewById(R.id.minutes_edit_text)).setText(String.valueOf(minutes));
		((EditText)findViewById(R.id.seconds_edit_text)).setText(String.valueOf(seconds));
		
		// update sound and image controls
		updateSoundImageFromFile(editRec.mSoundFile, editRec.mImageName);
		
	}
	
	private void updateSoundImageFromFile(String soundFile, String imageFile) {
		
		String soundFileTitle; 
		if (null != soundFile) {
			mEditSoundURI = UriHelper.fileNameToUri(getApplicationContext(), soundFile);
			soundFileTitle = (null == mEditSoundURI) ? getString(R.string.default_sound) : UriHelper.getSoundTitleFromFileName(getApplicationContext(), soundFile); 
		} else {
			soundFileTitle = getString(R.string.default_sound);
		}
		
		((Button)findViewById(R.id.sound_file_button)).setText(soundFileTitle);
		
		if (null != imageFile) {
			//editImageURI = Uri.parse(editRec.image_name);
			mEditImageURI = UriHelper.fileNameToUri(getApplicationContext(), imageFile);
			((ImageButton)findViewById(R.id.image_file_image_button)).setImageURI(mEditImageURI);
		}
		
	}
	
	private final DMTimerRec getEditRec() throws AddItemInputException {
		DMTimerRec rec = new DMTimerRec();
		rec.mId = mEditId;
		rec.mTitle = ((EditText)findViewById(R.id.title_edit_text)).getText().toString().trim();
		
		if (null == rec.mTitle) {
			throw new AddItemInputException(getResources().getString(R.string.error_title_not_assigned));
		}
		
		if (0 == rec.mTitle.length()) {
			throw new AddItemInputException(getResources().getString(R.string.error_title_not_assigned));
		}
		
		String hoursString = ((EditText)findViewById(R.id.hours_edit_text)).getText().toString().trim();
		String minutesString = ((EditText)findViewById(R.id.minutes_edit_text)).getText().toString().trim();
		String secondsString = ((EditText)findViewById(R.id.seconds_edit_text)).getText().toString().trim();
		long hours = Long.valueOf(hoursString);
		long minutes = Long.valueOf(minutesString); 
		long seconds = Long.valueOf(secondsString);
		rec.mTimeSec = hours * 3600 + minutes * 60 + seconds;
		
		if (0 == rec.mTimeSec) {
			throw new AddItemInputException(getResources().getString(R.string.error_time_zero));
		}
		
		rec.mSoundFile = null != mEditSoundURI ? UriHelper.uriMediaToFileName(getApplicationContext(), mEditSoundURI) : null;
		rec.mImageName = null != mEditImageURI ? UriHelper.uriMediaToFileName(getApplicationContext(), mEditImageURI) : null;
		
		return rec;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.add_item, menu);
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		if (null != mEditSoundURI) {
			outState.putString(EDIT_SOUND_URI, UriHelper.uriMediaToFileName(getApplicationContext(), mEditSoundURI));
		}
		if (null != mEditImageURI) {
			outState.putString(EDIT_IMAGE_URI, UriHelper.uriMediaToFileName(getApplicationContext(), mEditImageURI));
		}

	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		final String soundFile = savedInstanceState.getString(EDIT_SOUND_URI);		
		final String imageFile = savedInstanceState.getString(EDIT_IMAGE_URI);
		
		updateSoundImageFromFile(soundFile, imageFile);
	}
	
	public void saveButtonClick(View v) {
		finish();
	}

	
	public void onSoundFileButtonClick(View v){
		Intent soundIntent = new Intent();
		soundIntent.setType("audio/*");
		soundIntent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(soundIntent, SOUND_REQ_CODE);
	}
	
	public void onClearSoundFileButtonClick(View v){
		mEditSoundURI = null;
		((Button)findViewById(R.id.sound_file_button)).setText(R.string.default_sound);		
	}
	
	public void onClearImageFileButtonClick(View v){
		mEditImageURI = null;
		((ImageButton)findViewById(R.id.image_file_image_button)).setImageResource(R.drawable.btn_check_off);
	}	
	
	
	public void onImageFileImageButtonClick(View v) {
		Intent imageIntent = new Intent();
		imageIntent.setType("image/*");
		imageIntent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(imageIntent, IMAGE_REQ_CODE);		
	}
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data){

	  if(requestCode == SOUND_REQ_CODE){

	    if(resultCode == RESULT_OK){
	        //the selected audio.   	    		    	
	        mEditSoundURI = data.getData();
	        ((Button)findViewById(R.id.sound_file_button)).setText(UriHelper.getSoundTitleFromUri(getApplicationContext(), mEditSoundURI));	        
	    }
	  }
	  
	  if(requestCode == IMAGE_REQ_CODE){
		    if(resultCode == RESULT_OK){
		        //the selected image.
		    	mEditImageURI = data.getData();
		        ImageButton ib = (ImageButton)findViewById(R.id.image_file_image_button);
        		ib.setImageURI(mEditImageURI);        		
		    }
	  }

	  super.onActivityResult(requestCode, resultCode, data);
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
		} else if (findViewById(R.id.cancel_button) == v) {
			setResult(RESULT_CANCELED);
		};
		finish();
	}
}
