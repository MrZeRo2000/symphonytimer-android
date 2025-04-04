package com.romanpulov.symphonytimer.helper.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.util.Log;
import com.romanpulov.library.common.db.DBController;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.DMTimerHistRec;
import com.romanpulov.symphonytimer.model.DMTimerExecutionList;
import com.romanpulov.symphonytimer.model.DMTimerExecutionRec;
import com.romanpulov.symphonytimer.model.DMTimerRec;
import com.romanpulov.symphonytimer.model.DMTimers;

public class DBHelper implements DBController {
	private static final String TAG = DBHelper.class.getSimpleName();
	private static DBHelper mDBHelperInstance = null;

	private SQLiteDatabase mDB;
	private final DBOpenHelper mDBOpenHelper;
	private boolean mDBDataChanged = false;
	
	public static class RawRecItem {
		
		private final Map<String, String> fields = new HashMap<>();
		
		public Map<String, String> getFields () {
            return fields;
		}			
		
		public void putFieldNameValue(String fieldName, String fieldValue) {
			fields.put(fieldName, fieldValue);
		}
	}
	
	private DBHelper(Context context) {
		mDBOpenHelper = new DBOpenHelper(context);
		openDB();
	}

	@Override
	public void openDB() {
		if (null == mDB) {
			Log.d(TAG, "Opening DB");
			mDB = mDBOpenHelper.getWritableDatabase();
		}
	}

	@Override
	public void closeDB() {
		if (null != mDB) {
			Log.d(TAG, "Closing DB");
			mDB.close();
			mDB = null;
		}
	}

	@Override
	public void dbDataChanged() {
		mDBDataChanged = true;
	}

	@Override
	public String getDBName() {
		return DBOpenHelper.DATABASE_NAME;
	}

	public static DBHelper getInstance(Context context) {
		if (null == mDBHelperInstance) {
			mDBHelperInstance = new DBHelper(context.getApplicationContext());
		}		
		return mDBHelperInstance;
	}

	public static void clearInstance() {
		if (null != mDBHelperInstance) {
			mDBHelperInstance.closeDB();
            mDBHelperInstance = null;
		}
	}
	
	public boolean getDBDataChanged() {
        return this.mDBDataChanged;
	}
	
	public void resetDBDataChanged() {
        this.mDBDataChanged = false;
	}

	public long insertTimer(DMTimerRec dmTimerRec) {
		ContentValues cv = new ContentValues();

		cv.put(DBOpenHelper.TIMER_TABLE_COLS[1], dmTimerRec.getTitle());
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[2], dmTimerRec.getTimeSec());
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[3], dmTimerRec.getSoundFile());
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[4], dmTimerRec.getImageName());
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[5], getMaxOrderId() + 1);
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[6], dmTimerRec.getAutoTimerDisableInterval());

		return mDB.insert(DBOpenHelper.TIMER_TABLE_NAME, null, cv);
	}
	
	public long updateTimer(DMTimerRec dmTimerRec) {
		ContentValues cv = new ContentValues();

		cv.put(DBOpenHelper.TIMER_TABLE_COLS[1], dmTimerRec.getTitle());
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[2], dmTimerRec.getTimeSec());
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[3], dmTimerRec.getSoundFile());
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[4], dmTimerRec.getImageName());
		cv.put(DBOpenHelper.TIMER_TABLE_COLS[6], dmTimerRec.getAutoTimerDisableInterval());

		return mDB.update(DBOpenHelper.TIMER_TABLE_NAME, cv, "_id=" + dmTimerRec.getId(), null);
	}
	
	public long insertTimerHistory(DMTaskItem dmTaskItem) {
		ContentValues cv = new ContentValues();

		cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[1], dmTaskItem.getId());
		cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[2], dmTaskItem.getStartTime());
		cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[3], dmTaskItem.getCurrentTime());
		cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[4], System.currentTimeMillis());

		return mDB.insert(DBOpenHelper.TIMER_HISTORY_TABLE_NAME, null, cv);		
	}
	
	public long getLongSQL(String sql) {
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

    public void executeSQL(String sql) {
        mDB.execSQL(sql);
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
		String sql = "SELECT MAX(order_id) FROM " + DBOpenHelper.TIMER_TABLE_NAME + " WHERE order_id<" + orderId;
		return getLongSQL(sql);
	}
	
	private long getNextOrderId(long orderId) {
		String sql = "SELECT MIN(order_id) FROM " + DBOpenHelper.TIMER_TABLE_NAME + " WHERE order_id>" + orderId;
		return getLongSQL(sql);
	}
	
	private void exchangeOrderId(long orderId_1, long orderId_2) {

		String sql = "UPDATE " + DBOpenHelper.TIMER_TABLE_NAME + " SET order_id = " +
				"CASE WHEN order_id = " + orderId_1 + " THEN " + orderId_2 + " " +
				"WHEN order_id = " + orderId_2 + " THEN " + orderId_1 + " " +
				"ELSE order_id END";

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
		mDB.delete(DBOpenHelper.TIMER_HISTORY_TABLE_NAME, DBOpenHelper.TIMER_HISTORY_TABLE_COLS[1] + "=" + id, null);
		return mDB.delete(DBOpenHelper.TIMER_TABLE_NAME, DBOpenHelper.TIMER_TABLE_COLS[0] + "=" + id, null);
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

	public List<DMTimerRec> getTimers() {
		List<DMTimerRec> dmTimers = new ArrayList<>();

		Cursor c = null;

		try {
			c = mDB.query(DBOpenHelper.TIMER_TABLE_NAME, DBOpenHelper.TIMER_TABLE_COLS, null, null, null, null, "order_id");

			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())	{
				DMTimerRec dmTimerRec = new DMTimerRec(
						c.getLong(0),
						c.getString(1),
						c.getLong(2),
						c.getString(3),
						c.getString(4),
						c.getLong(5),
						c.getInt(6));

				dmTimers.add(dmTimerRec);
			}
		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}
		}

		return dmTimers;
	}

	public void fillTimers(DMTimers dmTimers) {
		// no need ...
		openDB();
		
		dmTimers.clear();

		Cursor c = null;
		
		try {
			c = mDB.query(DBOpenHelper.TIMER_TABLE_NAME, DBOpenHelper.TIMER_TABLE_COLS, null, null, null, null, "order_id");
			
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())	{
                DMTimerRec dmTimerRec = new DMTimerRec(
						c.getLong(0),
						c.getString(1),
						c.getLong(2),
						c.getString(3),
						c.getString(4),
						c.getLong(5),
						c.getInt(6));

				dmTimers.add(dmTimerRec);
			}
		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}
		}	
	}
	
	public List<DMTimerHistRec> getHistList(int filterId) {
		List<DMTimerHistRec> result = new ArrayList<>();
		Cursor c = null;
		
		try {
			if (filterId < (DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES.length - 1))
				c = mDB.query(
						DBOpenHelper.TIMER_HISTORY_TABLE_NAME, 
						DBOpenHelper.TIMER_HISTORY_TABLE_COLS, 
						DBOpenHelper.TIMER_HISTORY_SELECTION_CRITERIA, 
						new String[] {
                                String.valueOf(System.currentTimeMillis()),
                                String.valueOf(DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES[filterId])
                        },
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
                DMTimerHistRec dmRec = new DMTimerHistRec(
						c.getLong(0),
						c.getLong(1),
						c.getLong(2),
						c.getLong(3),
						c.getLong(4));

				result.add(dmRec);
			}
		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}
		}
		return result;
	}	
	
	public DMTimerExecutionList getHistTopList(int filterId) {
        DMTimerExecutionList res = new DMTimerExecutionList();
		Cursor c = null;
		
		try {
			if (filterId < (DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES.length - 1))
				c = mDB.rawQuery(
                        DBOpenHelper.TIMER_HISTORY_TOP_QUERY_FILTER,
                        new String[]{
                                String.valueOf(System.currentTimeMillis()),
                                String.valueOf(DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES[filterId])
                        });
			else
				c = mDB.rawQuery(DBOpenHelper.TIMER_HISTORY_TOP_QUERY, null);

			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())	{
                DMTimerExecutionRec dmRec = new DMTimerExecutionRec();
				dmRec.mTimerId = c.getLong(0);
				dmRec.mExecCnt = c.getLong(1);
				res.add(dmRec);
			}

		} finally {
			if (null != c && !c.isClosed()) {
				c.close();
			}
		}
        return res;
	}

	public List<LinkedHashMap<Long, Long>> getHistList(int filterId, int histCount) {

        List<LinkedHashMap<Long, Long>> executions = new ArrayList<>(histCount);

		Cursor c;
		long timeOffset;
		if (filterId < DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES.length)
        	timeOffset = DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES[filterId];
		else
			timeOffset = System.currentTimeMillis();

        long startTimeFilter = System.currentTimeMillis() - histCount * timeOffset;
        long endTimeFilter = startTimeFilter + timeOffset;
        boolean isHist = filterId < DBOpenHelper.TIMER_HISTORY_SELECTION_VALUES.length;

        for (int i = 0; i < histCount; i++) {
            c = null;
            LinkedHashMap<Long, Long> executionItem = new LinkedHashMap<>();

            try {
                if (isHist)
                    c = mDB.rawQuery(
                            DBOpenHelper.TIMER_HISTORY_QUERY_FILTER,
                            new String[]{
                                    String.valueOf(startTimeFilter),
                                    String.valueOf(endTimeFilter)
                            });
                else
                    c = mDB.rawQuery(DBOpenHelper.TIMER_HISTORY_TOP_QUERY, null);

                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    DMTimerExecutionRec dmRec = new DMTimerExecutionRec();
                    dmRec.mTimerId = c.getLong(0);
                    dmRec.mExecCnt = c.getLong(1);
                    executionItem.put(c.getLong(0), c.getLong(1));
                }

            } finally {
                if (null != c && !c.isClosed()) {
                    c.close();
                }
            }
            executions.add(executionItem);
            startTimeFilter += timeOffset;
            endTimeFilter += timeOffset;

            if (!isHist)
                break;
        }
        return executions;
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
			cv.put(DBOpenHelper.TIMER_TABLE_COLS[6], recItem.fields.get(DBOpenHelper.TIMER_TABLE_COLS[6]));

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
			cv.put(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[4], recItem.fields.get(DBOpenHelper.TIMER_HISTORY_TABLE_COLS[4]));

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
