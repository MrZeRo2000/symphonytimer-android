package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.romanpulov.symphonytimer.R;

public class MediaPlayerHelper {
	private static void log(String message) {
		LoggerHelper.log("MediaPlayerHelper", message);
	}

	private final Context mContext;
	private MediaPlayer mMediaPlayer;
	private int mOriginalVolume;
	private AudioManager mAudioManager;

    private AudioManager getAudioManager() {
        if (mAudioManager == null)
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager;
    }
	
	public MediaPlayerHelper(Context context){
		mContext = context;
	}
	
	public void stop() {
        log("stop");
		if (null != mMediaPlayer) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();				
			}
			mMediaPlayer.reset();
            mMediaPlayer.release();
			mMediaPlayer = null;
			getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
		}
	}
	
	public void release() {
        log("release");
		if (null != mMediaPlayer) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

    public void toggleSound(String soundFile) {
        if ((mMediaPlayer !=null) && (mMediaPlayer.isPlaying()))
            stop();
        else
            startSoundFile(soundFile);
    }

	public void startSoundFile(String soundFile) {
		stop();
        log("startSoundFile");
		if (null == soundFile) {
			mMediaPlayer = MediaPlayer.create(mContext, R.raw.default_sound);
		} else {
			Uri uri = UriHelper.fileNameToUri(mContext, soundFile);
            if (uri == null)
                mMediaPlayer = MediaPlayer.create(mContext, R.raw.default_sound);
            else {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mMediaPlayer.setDataSource(mContext, uri);
                    mMediaPlayer.prepare();
                } catch (Exception e) {
                    startSoundFile(null);
                }
            }
		}
		
		mOriginalVolume = getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC);
		getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		
		mMediaPlayer.setLooping(true);
		mMediaPlayer.start();
	}	
}
