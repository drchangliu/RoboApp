package com.robodoot.roboapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by John on 11/12/2015.
 */
public class CompTestDataSource {

    public static final String LOGTAG="RoboApp";

    SQLiteOpenHelper dbhelper;
    SQLiteDatabase database;

    public CompTestDataSource(Context context){
        dbhelper = new CompTestOpenHelper(context);
        database = dbhelper.getWritableDatabase();
    }

    public void open(){
        Log.i(LOGTAG, "Database opened");
        database = dbhelper.getWritableDatabase();
    }

    public void close(){
        Log.i(LOGTAG, "Database closed");
        dbhelper.close();
    }

    public CompTest create(CompTest compTest){
        ContentValues values = new ContentValues();
        //TODO add all colunms
        values.put(CompTestOpenHelper.COLUMN_TITLE, compTest.getTitle());
        values.put(CompTestOpenHelper.COLUMN_DESC, compTest.getDesc());
        long insertId = database.insert(CompTestOpenHelper.TABLE_COMPTEST,null, values);
        compTest.setId(insertId);
        return compTest;
    }
}
