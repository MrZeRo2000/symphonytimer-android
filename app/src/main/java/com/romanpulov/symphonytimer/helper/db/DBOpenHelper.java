package com.romanpulov.symphonytimer.helper.db;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBOpenHelper extends SQLiteOpenHelper {
	static final int DATABASE_VERSION = 6;
	static final String DATABASE_NAME = "symphonytimerdb";
    static final String TIMER_TABLE_NAME = "timer";
    static final String TIMER_HISTORY_TABLE_NAME = "timer_history";
    static final String TIMER_HISTORY_SELECTION_CRITERIA = "start_time>? - ?";
    static final Long[] TIMER_HISTORY_SELECTION_VALUES = new Long[] {
    	2592000000L, //"1000 * 60 * 60 * 24 * 30",
    	7776000000L, //"1000 * 60 * 60 * 24 * 30 * 3",
    	31536000000L //1000 * 60 * 60 * 24 * 365",
    };
    
    static final String MAX_ORDER_ID_COL = "max_order_id";
    
    static final String TIMER_HISTORY_TOP_QUERY =
    		"SELECT timer_id, COUNT(timer_id) exec_cnt, COUNT(timer_id) * 100 / (SELECT COUNT(timer_id) FROM timer_history) exec_perc " +
    		"FROM timer_history " +
    		"GROUP BY timer_id " +
    		"ORDER BY 2 DESC";
    static final String TIMER_HISTORY_TOP_QUERY_FILTER =
    		"SELECT timer_id, COUNT(timer_id) exec_cnt " +
    		"FROM timer_history " +
    		"WHERE start_time>? - ?" +		
    		"GROUP BY timer_id " +
    		"ORDER BY 2 DESC";

	static final String TIMER_HISTORY_QUERY_FILTER =
			"SELECT timer_id, COUNT(timer_id) exec_cnt " +
					"FROM timer_history " +
					"WHERE start_time>? " +
                    "  AND end_time<?" +
					"GROUP BY timer_id " +
					"ORDER BY 2 DESC";
    
    private static final String TIMER_BACKUP_GET_QUERY =
    		"SELECT _id, title, time_sec, order_id, auto_timer_disable FROM " + TIMER_TABLE_NAME;
    
    private static final String TIMER_HISTORY_BACKUP_GET_QUERY =
    		"SELECT _id, timer_id, start_time, end_time, real_time FROM " + TIMER_HISTORY_TABLE_NAME;
    
    static final Map<String, String> TABLE_BACKUP_QUERIES;
    static {
    	TABLE_BACKUP_QUERIES = new HashMap<> ();
    	TABLE_BACKUP_QUERIES.put(TIMER_TABLE_NAME, TIMER_BACKUP_GET_QUERY);
    	TABLE_BACKUP_QUERIES.put(TIMER_HISTORY_TABLE_NAME, TIMER_HISTORY_BACKUP_GET_QUERY);
    }

    static final String[] TIMER_TABLE_COLS;
    private static final String TIMER_TABLE_CREATE;
    static
    {
        TIMER_TABLE_COLS = new String[] {
            "_id",
            "title",
            "time_sec",
            "sound_file",
            "image_name",
            "order_id",
            "auto_timer_disable"
        };

        TIMER_TABLE_CREATE =
            "CREATE TABLE " + TIMER_TABLE_NAME + " (" +
            TIMER_TABLE_COLS[0] + " INTEGER PRIMARY KEY," +
            TIMER_TABLE_COLS[1] + " TEXT UNIQUE NOT NULL,"  +
            TIMER_TABLE_COLS[2] + " INTEGER NOT NULL,"  +
            TIMER_TABLE_COLS[3] + " TEXT,"  +
            TIMER_TABLE_COLS[4] + " TEXT, "  +
            TIMER_TABLE_COLS[5] + " INTEGER, "  +
            TIMER_TABLE_COLS[6] + " INTEGER"  +
             ");";
    }

    static final String[] TIMER_HISTORY_TABLE_COLS;
    private static final String TIMER_HISTORY_TABLE_CREATE;

    static {
        TIMER_HISTORY_TABLE_COLS = new String[] {
            "_id",
            "timer_id",
            "start_time",
            "end_time",
            "real_time"
        };

        TIMER_HISTORY_TABLE_CREATE =
            "CREATE TABLE " + TIMER_HISTORY_TABLE_NAME + " (" +
            TIMER_HISTORY_TABLE_COLS[0] + " INTEGER PRIMARY KEY," +
            TIMER_HISTORY_TABLE_COLS[1] + " INTEGER NOT NULL,"  +
            TIMER_HISTORY_TABLE_COLS[2] + " INTEGER NOT NULL,"  +
            TIMER_HISTORY_TABLE_COLS[3] + " INTEGER NOT NULL," +
            TIMER_HISTORY_TABLE_COLS[4] + " INTEGER NOT NULL" +
            ");";
    }
    
	DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TIMER_TABLE_CREATE);
        db.execSQL(TIMER_HISTORY_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch(oldVersion) {
            case 2:
                db.execSQL("ALTER TABLE " + DBOpenHelper.TIMER_TABLE_NAME + " ADD order_id INTEGER");
                db.execSQL("UPDATE " + DBOpenHelper.TIMER_TABLE_NAME + " SET order_id = _id");
            case 3:
                db.execSQL(TIMER_HISTORY_TABLE_CREATE);
            case 4:
                db.execSQL("ALTER TABLE " + DBOpenHelper.TIMER_HISTORY_TABLE_NAME + " ADD " + TIMER_HISTORY_TABLE_COLS[4] + " INTEGER");
            case 5:
                db.execSQL("ALTER TABLE " + DBOpenHelper.TIMER_TABLE_NAME + " ADD " + DBOpenHelper.TIMER_TABLE_COLS[6] + " INTEGER");
                db.execSQL("UPDATE " + DBOpenHelper.TIMER_TABLE_NAME + " SET " + DBOpenHelper.TIMER_TABLE_COLS[6] + " = 0");
            case 100:
                break;
            default:
                db.execSQL("DROP TABLE IF EXISTS " + DBOpenHelper.TIMER_TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + DBOpenHelper.TIMER_HISTORY_TABLE_NAME);
                onCreate(db);
        }
	}
	
}

