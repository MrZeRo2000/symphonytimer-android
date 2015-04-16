package com.romanpulov.symphonytimer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public final class URIHelper {	
	
	public static String uriAudioPathToFileName(Context context, String uriPath) {
		String res = null;
		Uri uri = Uri.parse(uriPath);
		String[] projection = {MediaStore.Audio.Media.DATA};
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		if (1 == cursor.getCount()) {
			cursor.moveToFirst();
			res = cursor.getString(0);			
		} 			
		return res;
	}
}
