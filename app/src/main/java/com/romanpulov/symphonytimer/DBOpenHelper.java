package com.romanpulov.symphonytimer;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBOpenHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 4;
	public static final String DATABASE_NAME = "symphonytimerdb";
    public static final String TIMER_TABLE_NAME = "timer";
    public static final String TIMER_HISTORY_TABLE_NAME = "timer_history";
    public static final String[] TIMER_TABLE_COLS = new String[] {
    	"_id", 
    	"title", 
    	"time_sec", 
    	"sound_file", 
    	"image_name", 
    	"order_id" 
    };
    public static final String[] TIMER_HISTORY_TABLE_COLS = new String[] {
    	"_id", 
    	"timer_id", 
    	"start_time", 
    	"end_time" 
    };
    public static final String TIMER_HISTORY_SELECTION_CRITERIA = "start_time>? - ?";
    public static final String[] TIMER_HISTORY_SELECTION_VALUES = new String[] {
    	"2592000000", //"1000 * 60 * 60 * 24 * 30",
    	"7776000000", //"1000 * 60 * 60 * 24 * 30 * 3",
    	"31536000000" //1000 * 60 * 60 * 24 * 365",    	
    };
    
    public static final String MAX_ORDER_ID_COL = "max_order_id";
    
    public static final String TIMER_HISTORY_TOP_QUERY =
    		"SELECT timer_id, COUNT(timer_id) exec_cnt, COUNT(timer_id) * 100 / (SELECT COUNT(timer_id) FROM timer_history) exec_perc " +
    		"FROM timer_history " +
    		"GROUP BY timer_id " +
    		"ORDER BY 2 DESC";
    public static final String TIMER_HISTORY_TOP_QUERY_FILTER =
    		"SELECT timer_id, COUNT(timer_id) exec_cnt " +
    		"FROM timer_history " +
    		"WHERE start_time>? - ?" +		
    		"GROUP BY timer_id " +
    		"ORDER BY 2 DESC";
    
    public static final String TIMER_BACKUP_GET_QUERY = 
    		"SELECT _id, title, time_sec, order_id FROM " + TIMER_TABLE_NAME;
    
    public static final String TIMER_HISTORY_BACKUP_GET_QUERY = 
    		"SELECT _id, timer_id, start_time, end_time FROM " + TIMER_HISTORY_TABLE_NAME;
    
    public static final Map<String, String> TABLE_BACKUP_QUERIES;
    static {
    	TABLE_BACKUP_QUERIES = new HashMap<String, String> ();
    	TABLE_BACKUP_QUERIES.put(TIMER_TABLE_NAME, TIMER_BACKUP_GET_QUERY);
    	TABLE_BACKUP_QUERIES.put(TIMER_HISTORY_TABLE_NAME, TIMER_HISTORY_BACKUP_GET_QUERY);
    }

    
    private static final String TIMER_TABLE_CREATE =
                "CREATE TABLE " + TIMER_TABLE_NAME + " (" +
                TIMER_TABLE_COLS[0] + " INTEGER PRIMARY KEY," +
                TIMER_TABLE_COLS[1] + " TEXT UNIQUE NOT NULL,"  +
                TIMER_TABLE_COLS[2] + " INTEGER NOT NULL,"  +
                TIMER_TABLE_COLS[3] + " TEXT,"  +
                TIMER_TABLE_COLS[4] + " TEXT, "  +
                TIMER_TABLE_COLS[5] + " INTEGER"  +
                 ");";

    private static final String TIMER_HISTORY_TABLE_CREATE =
            "CREATE TABLE " + TIMER_HISTORY_TABLE_NAME + " (" +
            TIMER_HISTORY_TABLE_COLS[0] + " INTEGER PRIMARY KEY," +
            TIMER_HISTORY_TABLE_COLS[1] + " INTEGER NOT NULL,"  +
            TIMER_HISTORY_TABLE_COLS[2] + " INTEGER NOT NULL,"  +
            TIMER_HISTORY_TABLE_COLS[3] + " INTEGER NOT NULL" +
             ");";

    
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
		// TODO Auto-generated method stub
		if ((2 == oldVersion) && (3 == newVersion)) {
			db.execSQL("ALTER TABLE " + DBOpenHelper.TIMER_TABLE_NAME + " ADD order_id INTEGER");
			db.execSQL("UPDATE " + DBOpenHelper.TIMER_TABLE_NAME + " SET order_id = _id");
		} else if ((4 > oldVersion) && (4 == newVersion)) {
			db.execSQL(TIMER_HISTORY_TABLE_CREATE);
		}  else	{		
			db.execSQL("DROP TABLE IF EXISTS " + DBOpenHelper.TIMER_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + DBOpenHelper.TIMER_HISTORY_TABLE_NAME);
			onCreate(db);
		}		
	}	
	
}

