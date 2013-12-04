package com.romanpulov.symphonytimer;

import android.media.AudioManager;
import android.media.SoundPool;

public class SoundHelper {
	private static SoundHelper soundHelperInstance = null;
	
	private SoundPool soundPool;
	
	public static SoundHelper getInstance() {
		if (null == soundHelperInstance) {
			soundHelperInstance = new SoundHelper();
		}		
		return soundHelperInstance;
	}	
	
	private void checkSoundPool() {
		if (null == soundPool) {
			soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		}
	}
	
	private SoundHelper() {
		checkSoundPool();
	}

	public int loadTrack(String path) {
		if (null != path) {
			checkSoundPool();
			return soundPool.load(path, 1);
		} else {
			return 0;
		}
	}
	
	public int playTrack(int trackId) {
		if (0 != trackId) {
			return soundPool.play(trackId, 1f, 1f, 1, -1, 1f);
		} else {
			return 0;
		}
	}
	
	public void stopTrack(int trackId) {
		if (0 != trackId) {
			soundPool.stop(trackId);
		}
	}
	
	public void release(){
		soundPool.release();
		soundPool = null;
	}
}
