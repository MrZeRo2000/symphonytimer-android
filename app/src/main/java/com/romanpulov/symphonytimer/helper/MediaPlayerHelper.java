package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.romanpulov.symphonytimer.R;

public class MediaPlayerHelper {
	private static MediaPlayerHelper mMediaPlayerHelperInstance = null;
	private Context mContext;
	private MediaPlayer mMediaPlayer;
	private int mOriginalVolume;
	private AudioManager mAudioManager;
	
	private MediaPlayerHelper(Context context){
		this.mContext = context;
	}
	
	public static MediaPlayerHelper getInstance(Context context) {
		if (null == mMediaPlayerHelperInstance) {
			mMediaPlayerHelperInstance = new MediaPlayerHelper(context);
			mMediaPlayerHelperInstance.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		}		
		return mMediaPlayerHelperInstance;
	}	
	
	public void stop() {
		if (null != mMediaPlayer) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();				
			}
			mMediaPlayer.reset();
			mMediaPlayer = null;
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
		}
	}
	
	public void release() {
		if (null != mMediaPlayer) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	public void startSoundFile(String soundFile) {
		stop();
		if (null == soundFile) {
			mMediaPlayer = MediaPlayer.create(mContext, R.raw.default_sound);
		} else {
			Uri uri = UriHelper.fileNameToUri(mContext, soundFile);
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				mMediaPlayer.setDataSource(mContext, uri);
				mMediaPlayer.prepare();
			} catch (Exception e) {
				startSoundFile(null);
			}
		}
		
		mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		
		mMediaPlayer.setLooping(true);
		mMediaPlayer.start();
	}	
}
