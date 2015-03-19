package com.romanpulov.symphonytimer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

public class MediaPlayerHelper {
	private static MediaPlayerHelper mediaPlayerHelperInstance = null;
	private Context context;
	private MediaPlayer mediaPlayer;
	private int originalVolume;
	private AudioManager audioManager;
	
	private MediaPlayerHelper(Context context){
		this.context = context;
	}
	
	public static MediaPlayerHelper getInstance(Context context) {
		if (null == mediaPlayerHelperInstance) {
			mediaPlayerHelperInstance = new MediaPlayerHelper(context);
			mediaPlayerHelperInstance.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		}		
		return mediaPlayerHelperInstance;
	}	
	
	public void stop() {
		if (null != mediaPlayer) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();				
			}
			mediaPlayer.reset();
			mediaPlayer = null;
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
		}
	}
	
	public void release() {
		if (null != mediaPlayer) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	public void startSoundFile(String soundFile) {
		// TODO Auto-generated method stub
		stop();
		if (null == soundFile) {
			mediaPlayer = MediaPlayer.create(context, R.raw.default_sound);
		} else {
			Uri uri = Uri.parse(soundFile);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				mediaPlayer.setDataSource(context, uri);
				mediaPlayer.prepare();
			} catch (Exception e) {
				startSoundFile(null);
			}
		}
		
		originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		
		mediaPlayer.setLooping(true);
		mediaPlayer.start();
	}	
	
}
