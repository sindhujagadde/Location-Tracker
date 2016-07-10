package com.example.sindhuja.locationtracker.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sindhuja on 7/9/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DEBUG_TAG = "location table";
    private static final String DATABASE_NAME = "mydb";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "locdata";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LATITUDE = "latitude";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + " ("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TIME + " string, "
            + COLUMN_LONGITUDE + " double, "
            + COLUMN_LATITUDE + " double"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        ContentValues cv = new ContentValues(3);
        cv.put(COLUMN_TIME,0.00);
        cv.put(COLUMN_LATITUDE, 0.00);
        cv.put(COLUMN_LONGITUDE,0.00);
        database.insert(TABLE_NAME,null,cv);
        //LocTable.onCreate(database);
    }

    // Method is called during an upgrade of the database,
    // e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(database);
        //LocTable.onUpgrade(database, oldVersion, newVersion);
    }
}
