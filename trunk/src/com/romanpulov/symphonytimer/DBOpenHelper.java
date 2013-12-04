package com.romanpulov.symphonytimer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBOpenHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 3;
	private static final String DATABASE_NAME = "symphonytimerdb";
    public static final String TIMER_TABLE_NAME = "timer";
    public static final String[] TIMER_TABLE_COLS = new String[] {
    	"_id", "title", "time_sec", "sound_file", "image_name", "order_id" 
    };
    public static final String MAX_ORDER_ID_COL = "max_order_id";
    
    private static final String DB_TABLES_CREATE =
                "CREATE TABLE " + TIMER_TABLE_NAME + " (" +
                TIMER_TABLE_COLS[0] + " INTEGER PRIMARY KEY," +
                TIMER_TABLE_COLS[1] + " TEXT UNIQUE NOT NULL,"  +
                TIMER_TABLE_COLS[2] + " INTEGER NOT NULL,"  +
                TIMER_TABLE_COLS[3] + " TEXT,"  +
                TIMER_TABLE_COLS[4] + " TEXT, "  +
                TIMER_TABLE_COLS[5] + " INTEGER"  +
                 ");";
    
    
	DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_TABLES_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		if ((2 == oldVersion) && (3 == newVersion)) {
			db.execSQL("ALTER TABLE " + DBOpenHelper.TIMER_TABLE_NAME + " ADD order_id INTEGER");
			db.execSQL("UPDATE " + DBOpenHelper.TIMER_TABLE_NAME + " SET order_id = _id");
		} else {		
			db.execSQL("DROP TABLE IF EXISTS " + DBOpenHelper.TIMER_TABLE_NAME);
			onCreate(db);
		}		
	}
}

