package com.robodoot.roboapp;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by John on 11/12/2015.
 */
public class CompTestOpenHelper extends SQLiteOpenHelper {

    public static final String LOGTAG="RoboApp";

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "compTest.db";
    public static final String TABLE_COMPTEST = "compTests";
    public static final String COLUMN_ID = "testId";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESC = "description";

    //TODO add all colunms

    private static final String COMPTESTS_TABLE_CREATE =
            "CREATE TABLE " + TABLE_COMPTEST + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_DESC + " TEXT, " +
                    ");";


    CompTestOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(COMPTESTS_TABLE_CREATE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int old_ver, int new_ver) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_COMPTEST);
        db.execSQL(COMPTESTS_TABLE_CREATE);
    }
}
