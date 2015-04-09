package com.romanpulov.symphonytimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHelper {
	private static DBHelper dbHelperInstance = null;	
	private Context context;
	private SQLiteDatabase db;
	private final DBOpenHelper dbOpenHelper;
	
	public static class RawRecItem {
		
		private Map<String, String> fields = new HashMap<String, String>();
		
		public Map<String, String> getFields () {
			return fields;
		}			
		
		public void setFieldNameValue(String fieldName, String fieldValue) {
			fields.put(fieldName, fieldValue);
		}
		
		
	}	
	
	private DBHelper(Context context) {
		this.context = context;
		dbOpenHelper = new DBOpenHelper(context);
		openDB();
	}
	
	public void openDB() {
		if (null == db) {
			db = dbOpenHelper.getWritableDatabase();
		}
	}
	
	public void closeDB() {
		if (null != db) {
			db.close();
			db = null;
		}
	}
	
	public static DBHelper getInstance(Context context) {
		if (null == dbHelperInstance) {
			dbHelperInstance = new DBHelper(context);			
		}		
		return dbHelperInstance;
	}
	
	public String getDatabasePathName() {
		return context.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString();
	}
	
	public void Init(){
		
	}
	
	public long insertTimer(DMTimerRec dmTimerRec) {
		ContentValues cv = new ContentValues();
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[1], dmTimerRec.title);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[2], dmTimerRec.time_sec);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[3], dmTimerRec.sound_file);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[4], dmTimerRec.image_name);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[5], getMaxOrderId() + 1);
		return db.insert(DBOpenHelper.TIMER_TABLE_NAME, null, cv);
	}
	
	public long updateTimer(DMTimerRec dmTimerRec) {
		ContentValues cv = new ContentValues();
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[1], dmTimerRec.title);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[2], dmTimerRec.time_sec);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[3], dmTimerRec.sound_file);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[4], dmTimerRec.image_name);
		return db.update(DBOpenHelper.TIMER_TABLE_NAME, cv, "_id=" + dmTimerRec.id, null);
	}
	
	public long insertTimerHistory(DMTaskItem dmTaskItem) {
		ContentValues cv = new ContentValues();
		cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[1], dmTaskItem.getId());
		cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[2], dmTaskItem.getStartTime());
		cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[3], dmTaskItem.getCurrentTime());
		return db.insert(DBOpenHelper.TIMER_HISTORY_TABLE_NAME, null, cv);		
	}
	
	private long getLongSQL(String sql) {
		Cursor c = db.rawQuery(sql, null);
		
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
		Log.d("SQL", sql);		
		
		db.execSQL(sql);
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
		db.delete(DBOpenHelper.TIMER_HISTORY_TABLE_NAME, DBOpenHelper.TIMER_HISTORY_TABLE_COLS[1] + "=" + String.valueOf(id), null);
		return db.delete(DBOpenHelper.TIMER_TABLE_NAME, DBOpenHelper.TIMER_TABLE_COLS[0] + "=" + String.valueOf(id), null);
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
		DMTimerRec dmTimerRec = null;
		Cursor c = null;		
		
		try {
			c = db.query(DBOpenHelper.TIMER_TABLE_NAME, DBOpenHelper.TIMER_TABLE_COLS, null, null, null, null, "order_id");
			
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())	{
				dmTimerRec = new DMTimerRec();
				dmTimerRec.id = c.getLong(0);
				dmTimerRec.title = c.getString(1);
				dmTimerRec.time_sec = c.getLong(2);
				dmTimerRec.sound_file = c.getString(3);
				dmTimerRec.image_name = c.getString(4);
				dmTimerRec.order_id = c.getLong(5);
				
				dmTimers.add(dmTimerRec);
			}
		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}
		}	
		
	}
	
	public void fillHistList(DMTimerHistList dmList, int filterId) {
		
		dmList.clear();
		DMTimerHistRec dmRec = null;
		Cursor c = null;
		
		try {
			if (filterId < (DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES.length - 1))
				c = db.query(
						DBOpenHelper.TIMER_HISTORY_TABLE_NAME, 
						DBOpenHelper.TIMER_HISTORY_TABLE_COLS, 
						DBOpenHelper.TIMER_HISTORY_SELECTION_CRITERIA, 
						new String[] {String.valueOf(System.currentTimeMillis()), DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES[filterId]}, 
						null, 
						null, 
						"start_time DESC");
			else
				c = db.query(
						DBOpenHelper.TIMER_HISTORY_TABLE_NAME, 
						DBOpenHelper.TIMER_HISTORY_TABLE_COLS, 
						null, 
						null, 
						null, 
						null, 
						"start_time DESC");
			
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())	{
				dmRec = new DMTimerHistRec();
				dmRec.id = c.getLong(0);
				dmRec.timerId = c.getLong(1);
				dmRec.startTime = c.getLong(2);
				dmRec.endTime = c.getLong(3);
				
				dmList.add(dmRec);
			}
		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}
		}
	}	
	
	public void fillHistTopList(DMTimerHistTopList dmList, int filterId) {
		
		dmList.clear();
		DMTimerHistTopRec dmRec = null;
		Cursor c = null;
		
		try {
			if (filterId < (DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES.length - 1))
				c = db.rawQuery(
						DBOpenHelper.TIMER_HISTORY_TOP_QUERY_FILTER, 
						new String[] {String.valueOf(System.currentTimeMillis()), DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES[filterId]});
			else
				c = db.rawQuery(DBOpenHelper.TIMER_HISTORY_TOP_QUERY, null);
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())	{
				dmRec = new DMTimerHistTopRec();
				dmRec.timerId = c.getLong(0);
				dmRec.execCnt = c.getLong(1);								
				dmList.add(dmRec);
			};

		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}
		}
	}
	
	public List<RawRecItem> getBackupTable(String tableName) {
		
		List<RawRecItem> res = new ArrayList<RawRecItem>();
		
		Cursor c = null;
		
		try {
			c = db.rawQuery(DBOpenHelper.TABLE_BACKUP_QUERIES.get(tableName), null);
						
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				
				RawRecItem recItem = new RawRecItem();
				
				for (int columnIndex = 0; columnIndex < c.getColumnCount(); columnIndex ++ ) {					
					recItem.setFieldNameValue(c.getColumnName(columnIndex), c.getString(columnIndex));										
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

	public void restoreBackupData (Map<String, List<DBHelper.RawRecItem>> tableData) {
		
		
		
	}
	
}
