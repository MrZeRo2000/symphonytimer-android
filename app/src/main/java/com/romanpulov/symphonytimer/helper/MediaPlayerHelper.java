package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.romanpulov.symphonytimer.R;

public class MediaPlayerHelper {
	private void log(String message) {
		LoggerHelper.getInstance(mContext).log("MediaPlayerHelper", message);
	}

	private final Context mContext;
    private final boolean mIsMute;
	private MediaPlayer mMediaPlayer;
	private int mOriginalVolume = -1;
	private AudioManager mAudioManager;

    private AudioManager getAudioManager() {
        if (mAudioManager == null)
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager;
    }

    private String mSoundFileName;

    public void setSoundFileName(String soundFileName) {
        mSoundFileName = soundFileName;
    }

	public MediaPlayerHelper(Context context){
		mContext = context;
		mIsMute = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_mute", false);
	}

	public void start() {
	    startSoundFile(mSoundFileName);
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

			if (mOriginalVolume != -1) {
				log ("restore original volume");
				getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
			}
		}
	}
	
    public void toggleSound(String soundFileName) {
        if ((mMediaPlayer !=null) && (mMediaPlayer.isPlaying()))
            stop();
        else
            startSoundFile(soundFileName);
    }

	private void startSoundFile(String soundFileName) {
        if (mIsMute)
            return;

		stop();
        log("startSoundFile");
		if (null == soundFileName) {
			mMediaPlayer = MediaPlayer.create(mContext, R.raw.default_sound);
		} else {
			Uri uri = UriHelper.fileNameToUri(mContext, soundFileName);
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
		log ("set max volume");
		getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		
		mMediaPlayer.setLooping(true);
		mMediaPlayer.start();
	}	
}
