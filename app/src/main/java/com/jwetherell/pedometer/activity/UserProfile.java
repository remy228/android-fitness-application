package com.jwetherell.pedometer.activity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;

import com.jwetherell.pedometer.R;

public class UserProfile extends Activity {

    MyDBHandler myDBHandler;
    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

    }


}
