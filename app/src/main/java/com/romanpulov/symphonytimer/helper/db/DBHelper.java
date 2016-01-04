package com.romanpulov.symphonytimer.helper.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.DMTimerHistRec;
import com.romanpulov.symphonytimer.model.DMTimerExecutionList;
import com.romanpulov.symphonytimer.model.DMTimerExecutionRec;
import com.romanpulov.symphonytimer.model.DMTimerRec;
import com.romanpulov.symphonytimer.model.DMTimers;

public class DBHelper {
	private static DBHelper mDBHelperInstance = null;	
	private Context mContext;
	private SQLiteDatabase mDB;
	private final DBOpenHelper mDBOpenHelper;
	private boolean mDBDataChanged = false;
	
	public static class RawRecItem {
		
		private Map<String, String> fields = new HashMap<>();
		
		public Map<String, String> getFields () {
            return fields;
		}			
		
		public void putFieldNameValue(String fieldName, String fieldValue) {
			fields.put(fieldName, fieldValue);
		}
	}
	
	private DBHelper(Context context) {
		this.mContext = context;
		mDBOpenHelper = new DBOpenHelper(context);
		openDB();
	}
	
	public void openDB() {
		if (null == mDB) {
			mDB = mDBOpenHelper.getWritableDatabase();
		}
	}
	
	public void closeDB() {
		if (null != mDB) {
			mDB.close();
			mDB = null;
		}
	}
	
	public static DBHelper getInstance(Context context) {
		if (null == mDBHelperInstance) {
			mDBHelperInstance = new DBHelper(context);			
		}		
		return mDBHelperInstance;
	}
	
	public boolean getDBDataChanged() {
        return this.mDBDataChanged;
	}
	
	public void resetDBDataChanged() {
        this.mDBDataChanged = false;
	}
	
	public String getDatabasePathName() {
		return mContext.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString();
	}
	
	public long insertTimer(DMTimerRec dmTimerRec) {
		ContentValues cv = new ContentValues();

		cv.put(DBOpenHelper.TIMER_TABLE_COLS[1], dmTimerRec.mTitle);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[2], dmTimerRec.mTimeSec);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[3], dmTimerRec.mSoundFile);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[4], dmTimerRec.mImageName);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[5], getMaxOrderId() + 1);

		return mDB.insert(DBOpenHelper.TIMER_TABLE_NAME, null, cv);
	}
	
	public long updateTimer(DMTimerRec dmTimerRec) {
		ContentValues cv = new ContentValues();

		cv.put(DBOpenHelper.TIMER_TABLE_COLS[1], dmTimerRec.mTitle);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[2], dmTimerRec.mTimeSec);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[3], dmTimerRec.mSoundFile);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[4], dmTimerRec.mImageName);

		return mDB.update(DBOpenHelper.TIMER_TABLE_NAME, cv, "_id=" + dmTimerRec.mId, null);
	}
	
	public long insertTimerHistory(DMTaskItem dmTaskItem) {
		ContentValues cv = new ContentValues();

		cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[1], dmTaskItem.getId());
		cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[2], dmTaskItem.getStartTime());
		cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[3], dmTaskItem.getCurrentTime());

		return mDB.insert(DBOpenHelper.TIMER_HISTORY_TABLE_NAME, null, cv);		
	}
	
	private long getLongSQL(String sql) {
		Cursor c = mDB.rawQuery(sql, null);
		
		try {
			if (1 == c.getCount()) {
				c.moveToFirst();
				return c.isNull(0) ? 0 : c.getLong(0);
			}
			 else 
				return 0;
		}
		finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}	
		}		
	}

    private List<String> getStringListSQL(String sql) {
        List<String> result = new ArrayList<>();
        Cursor c = mDB.rawQuery(sql, null);
        try {
            for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                result.add(c.getString(0));
            }
        }
        finally {
            if (null != c && !c.isClosed()) {
                c.close();
            }
        }
        return result;
    }

    public List<String> getMediaFileNameList() {
        return getStringListSQL(
                "SELECT " + DBOpenHelper.TIMER_TABLE_COLS[3] + " FROM " + DBOpenHelper.TIMER_TABLE_NAME + " UNION " +
                        "SELECT " + DBOpenHelper.TIMER_TABLE_COLS[4] + " FROM " + DBOpenHelper.TIMER_TABLE_NAME
        );
    }
	
	public long getMaxOrderId() {
		return getLongSQL("SELECT MAX(order_id) AS " + DBOpenHelper.MAX_ORDER_ID_COL + " FROM " + DBOpenHelper.TIMER_TABLE_NAME);
	} 
	
	private long getPrevOrderId(long orderId) {
		String sql = "SELECT MAX(order_id) FROM " + DBOpenHelper.TIMER_TABLE_NAME + " WHERE order_id<" + String.valueOf(orderId);
		return getLongSQL(sql);
	}
	
	private long getNextOrderId(long orderId) {
		String sql = "SELECT MAX(order_id) FROM " + DBOpenHelper.TIMER_TABLE_NAME + " WHERE order_id>" + String.valueOf(orderId);
		return getLongSQL(sql);
	}
	
	private void exchangeOrderId(long orderId_1, long orderId_2) {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("UPDATE ").append(DBOpenHelper.TIMER_TABLE_NAME).append(" SET order_id = ");
		sqlBuilder.append("CASE WHEN order_id = ").append(orderId_1).append(" THEN ").append(orderId_2).append(" ");
		sqlBuilder.append("WHEN order_id = ").append(orderId_2).append(" THEN ").append(orderId_1).append(" ");
		sqlBuilder.append("ELSE order_id END");
		
		String sql = sqlBuilder.toString();

		mDB.execSQL(sql);
	}	
	
	public boolean moveTimerUp(long orderId) {
		long prevOrderId = getPrevOrderId(orderId);
		if (prevOrderId > 0) {
			exchangeOrderId(orderId, prevOrderId);
			return true;
		} else
			return false;
	}
	
	public boolean moveTimerDown(long orderId) {
		long nextOrderId = getNextOrderId(orderId);
		if (nextOrderId > 0) {
			exchangeOrderId(orderId, nextOrderId);
			return true;
		} else
			return false;
	}
	
	
	public long deleteTimer(long id) {
		mDB.delete(DBOpenHelper.TIMER_HISTORY_TABLE_NAME, DBOpenHelper.TIMER_HISTORY_TABLE_COLS[1] + "=" + String.valueOf(id), null);
		return mDB.delete(DBOpenHelper.TIMER_TABLE_NAME, DBOpenHelper.TIMER_TABLE_COLS[0] + "=" + String.valueOf(id), null);
	}

	/* No longer needed
	private DMTimerRec getTimerRecById(long id) {
		Cursor c = null;
		DMTimerRec dmTimerRec = null;
		
		try {
			c = db.query(DBOpenHelper.TIMER_TABLE_NAME, DBOpenHelper.TIMER_TABLE_COLS, "_id=" + id, null, null, null, null);
			if (1 == c.getCount()) {
				c.moveToFirst();
				dmTimerRec = new DMTimerRec();
				dmTimerRec.id = c.getLong(0);
				dmTimerRec.title = c.getString(1);
				dmTimerRec.time_sec = c.getLong(2);
				dmTimerRec.sound_file = c.getString(3);
				dmTimerRec.image_name = c.getString(4);
			}
		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}
		}
		
		return dmTimerRec;
	}
	*/
	
	public void fillTimers(DMTimers dmTimers) {
		// no need ...
		openDB();
		
		dmTimers.clear();
		Cursor c = null;
		
		try {
			c = mDB.query(DBOpenHelper.TIMER_TABLE_NAME, DBOpenHelper.TIMER_TABLE_COLS, null, null, null, null, "order_id");
			
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())	{
                DMTimerRec dmTimerRec = new DMTimerRec();
				dmTimerRec.mId = c.getLong(0);
				dmTimerRec.mTitle = c.getString(1);
				dmTimerRec.mTimeSec = c.getLong(2);
				dmTimerRec.mSoundFile = c.getString(3);
				dmTimerRec.mImageName = c.getString(4);
				dmTimerRec.mOrderId = c.getLong(5);
				
				dmTimers.add(dmTimerRec);
			}
		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}
		}	
	}
	
	public void fillHistList(List<DMTimerHistRec> dmList, int filterId) {
		dmList.clear();
		Cursor c = null;
		
		try {
			if (filterId < (DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES.length - 1))
				c = mDB.query(
						DBOpenHelper.TIMER_HISTORY_TABLE_NAME, 
						DBOpenHelper.TIMER_HISTORY_TABLE_COLS, 
						DBOpenHelper.TIMER_HISTORY_SELECTION_CRITERIA, 
						new String[] {String.valueOf(System.currentTimeMillis()), DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES[filterId]}, 
						null, 
						null, 
						"start_time DESC");
			else
				c = mDB.query(
						DBOpenHelper.TIMER_HISTORY_TABLE_NAME, 
						DBOpenHelper.TIMER_HISTORY_TABLE_COLS, 
						null, 
						null, 
						null, 
						null, 
						"start_time DESC");
			
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())	{
                DMTimerHistRec dmRec = new DMTimerHistRec();
				dmRec.mId = c.getLong(0);
				dmRec.mTimerId = c.getLong(1);
				dmRec.mStartTime = c.getLong(2);
				dmRec.mEndTime = c.getLong(3);
				
				dmList.add(dmRec);
			}
		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}
		}
	}	
	
	public void fillHistTopList(DMTimerExecutionList dmList, int filterId) {
		dmList.clear();
		Cursor c = null;
		
		try {
			if (filterId < (DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES.length - 1))
				c = mDB.rawQuery(
						DBOpenHelper.TIMER_HISTORY_TOP_QUERY_FILTER, 
						new String[] {String.valueOf(System.currentTimeMillis()), DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES[filterId]});
			else
				c = mDB.rawQuery(DBOpenHelper.TIMER_HISTORY_TOP_QUERY, null);

			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())	{
                DMTimerExecutionRec dmRec = new DMTimerExecutionRec();
				dmRec.mTimerId = c.getLong(0);
				dmRec.mExecCnt = c.getLong(1);								
				dmList.add(dmRec);
			};

		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}
		}
	}
	
	public List<RawRecItem> getBackupTable(String tableName) {
		List<RawRecItem> res = new ArrayList<>();
		Cursor c = null;
		
		try {
			c = mDB.rawQuery(DBOpenHelper.TABLE_BACKUP_QUERIES.get(tableName), null);
						
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				RawRecItem recItem = new RawRecItem();
				
				for (int columnIndex = 0; columnIndex < c.getColumnCount(); columnIndex ++ ) {					
					recItem.putFieldNameValue(c.getColumnName(columnIndex), c.getString(columnIndex));										
				}

				res.add(recItem);
			}
		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}			
		}		
		return res;
	}
	
	private void loadBackupTimer(List<DBHelper.RawRecItem> recList) {
		for (RawRecItem recItem : recList) {
			ContentValues cv = new ContentValues();

			cv.put(DBOpenHelper.TIMER_TABLE_COLS[0], recItem.fields.get(DBOpenHelper.TIMER_TABLE_COLS[0]));
			cv.put(DBOpenHelper.TIMER_TABLE_COLS[1], recItem.fields.get(DBOpenHelper.TIMER_TABLE_COLS[1]));
			cv.put(DBOpenHelper.TIMER_TABLE_COLS[2], recItem.fields.get(DBOpenHelper.TIMER_TABLE_COLS[2]));
			cv.put(DBOpenHelper.TIMER_TABLE_COLS[5], recItem.fields.get(DBOpenHelper.TIMER_TABLE_COLS[5]));

			mDB.insert(DBOpenHelper.TIMER_TABLE_NAME, null, cv);						
		}
	}
	
	private void loadBackupTimerHistory(List<DBHelper.RawRecItem> recList) {
		for (RawRecItem recItem : recList) {
			ContentValues cv = new ContentValues();

			cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[0], recItem.fields.get(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[0]));
			cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[1], recItem.fields.get(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[1]));
			cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[2], recItem.fields.get(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[2]));
			cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[3], recItem.fields.get(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[3]));

			mDB.insert(DBOpenHelper.TIMER_HISTORY_TABLE_NAME, null, cv);						
		}
	}
	
	public void clearData() {
		mDB.delete(DBOpenHelper.TIMER_TABLE_NAME, null, null);
		mDB.delete(DBOpenHelper.TIMER_HISTORY_TABLE_NAME, null, null);
		
		mDBDataChanged = true;
	}

	public void restoreBackupData (Map<String, List<DBHelper.RawRecItem>> tableData) {
		//delete old data
		clearData();
		
		//load timer
        List<DBHelper.RawRecItem> tableItemData = tableData.get(DBOpenHelper.TIMER_TABLE_NAME);
		if (null != tableItemData) {
			loadBackupTimer(tableItemData);
		}
		
		//load timer history
		tableItemData = tableData.get(DBOpenHelper.TIMER_HISTORY_TABLE_NAME);
		if (null != tableItemData) {
			loadBackupTimerHistory(tableItemData);
		}
	}
}
