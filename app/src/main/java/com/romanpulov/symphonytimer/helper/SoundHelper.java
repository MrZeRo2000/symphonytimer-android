package com.romanpulov.symphonytimer.helper;

import android.media.AudioManager;
import android.media.SoundPool;

public class SoundHelper {
	
	private static SoundHelper mSoundHelperInstance = null;	
	private SoundPool mSoundPool;
	
	public static SoundHelper getInstance() {
		if (null == mSoundHelperInstance) {
			mSoundHelperInstance = new SoundHelper();
		}		
		return mSoundHelperInstance;
	}	
	
	private void checkSoundPool() {
		if (null == mSoundPool) {
			mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		}
	}
	
	private SoundHelper() {
		checkSoundPool();
	}

	public int loadTrack(String path) {
		if (null != path) {
			checkSoundPool();
			return mSoundPool.load(path, 1);
		} else {
			return 0;
		}
	}
	
	public int playTrack(int trackId) {
		if (0 != trackId) {
			return mSoundPool.play(trackId, 1f, 1f, 1, -1, 1f);
		} else {
			return 0;
		}
	}
	
	public void stopTrack(int trackId) {
		if (0 != trackId) {
			mSoundPool.stop(trackId);
		}
	}
	
	public void release(){
		mSoundPool.release();
		mSoundPool = null;
	}
}
