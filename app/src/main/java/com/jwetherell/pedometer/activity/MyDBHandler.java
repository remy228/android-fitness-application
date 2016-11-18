package com.jwetherell.pedometer.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by remya on 11/16/2016.
 */
public class MyDBHandler extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "AlphaFitness.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "UserProfile";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_GENDER = "Gender";
    public static final String COLUMN_WEIGHT = "Weight";

    private static final String CREATE_QUERY = "CREATE TABLE "+ TABLE_NAME + "(" + COLUMN_NAME + " TEXT, " + COLUMN_GENDER + " TEXT, " +
            COLUMN_WEIGHT + " INT);" ;

    public MyDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.e("DATABASE OPERATIONS:" , "Db has been created");
    }

    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_QUERY);
        Log.e("DATABASE OPERATIONS:", "Table UserProfile has been created");
        Log.i("Tag", CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addUserData(String name, String gender, int weight, SQLiteDatabase db)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME,name);
        contentValues.put(COLUMN_GENDER,gender);
        contentValues.put(COLUMN_WEIGHT,weight);
        db.insert(TABLE_NAME,null,contentValues);
        Log.e("DATABASE OPERATIONS:" , "User data has been inserted into the database");
    }
}
