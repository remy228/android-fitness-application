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
    public static final String COLUMN_ID = "Id";
    public static final String COLUMN_STEPS = "Steps_taken";
    public static final String COLUMN_DISTANCE = "Distance_in_km";
    public static final String COLUMN_CALORIES = "Calories_burnt";


    private static final String CREATE_QUERY = "CREATE TABLE "+ TABLE_NAME + "(" + COLUMN_NAME + " TEXT, " + COLUMN_GENDER + " TEXT, " +
            COLUMN_WEIGHT + " INT);" ;
    //private static final String CREATE_QUERY = "DROP DATABASE DATABASE_NAME";

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

    public void createWorkoutTable(String name,SQLiteDatabase db)
    {
        String TABLE_NAME2 = name;
        final String CREATE_QUERY2 = "CREATE TABLE "+ TABLE_NAME2 + "(" + COLUMN_ID + " INT, " + COLUMN_STEPS + " INT, " +
                COLUMN_DISTANCE + " FLOAT, " + COLUMN_CALORIES + " INT);";
        db.execSQL(CREATE_QUERY2);
        Log.e("DATABASE OPERATIONS:", "Table for workout details has been created");
        Log.i("Tag", CREATE_QUERY2);
    }

    public void addWorkoutDetails(String name,int steps,float distance, int calories,SQLiteDatabase db)
    {
        String TABLE_NAME3 = name;
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID,1);
        contentValues.put(COLUMN_STEPS,steps);
        contentValues.put(COLUMN_DISTANCE,distance);
        contentValues.put(COLUMN_CALORIES,calories);
        db.insert(TABLE_NAME3,null,contentValues);
        Log.e("DATABASE OPERATIONS:" , "Workout data has been inserted into the database");
    }
}

