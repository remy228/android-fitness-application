package com.jwetherell.pedometer.activity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jwetherell.pedometer.R;

public class UserProfile extends Activity {

    MyDBHandler myDBHandler;
    SQLiteDatabase sqLiteDatabase;
    EditText Name;
    Spinner Gender;
    EditText Weight;
    Button Save;
    TextView AvgDist;
    TextView AvgTimeHr;
    TextView AvgTimeMin;
    TextView AvgTimeSec;
    TextView AvgWorkouts;
    TextView AvgCalories;
    TextView AllDist;
    TextView AllTimeDay;
    TextView AllTimeHr;
    TextView AllTimeMin;
    TextView AllTimeSec;
    TextView AllWorkouts;
    TextView AllCalories;
    String get_name = null;
    static int get_weight;
    static String get_gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        myDBHandler = new MyDBHandler(getApplicationContext());
        Name = (EditText)findViewById(R.id.editText2);
        Gender = (Spinner)findViewById(R.id.spinner);
        Weight = (EditText)findViewById(R.id.editText);
        Save = (Button)findViewById(R.id.button);
        AvgDist = (TextView)findViewById(R.id.textView21);
        AvgTimeHr = (TextView)findViewById(R.id.textView28);
        AvgTimeMin = (TextView)findViewById(R.id.textView29);
        AvgTimeSec = (TextView)findViewById(R.id.textView30);
        AvgWorkouts = (TextView)findViewById(R.id.textView31);
        AvgCalories = (TextView)findViewById(R.id.textView32);
        AllDist = (TextView)findViewById(R.id.textView33);
        AllTimeDay = (TextView)findViewById(R.id.textView37);
        AllTimeHr = (TextView)findViewById(R.id.textView42);
        AllTimeMin = (TextView)findViewById(R.id.textView38);
        AllTimeSec = (TextView)findViewById(R.id.textView40);
        AllWorkouts = (TextView)findViewById(R.id.textView43);
        AllCalories = (TextView)findViewById(R.id.textView45);


        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // TODO Auto-generated method stub

                get_name = Name.getText().toString();
                Log.i("Testing name: ", get_name);
                get_weight = Integer.parseInt( Weight.getText().toString() );
                System.out.println("Testing weight: "+ get_weight);
                get_gender = Gender.getSelectedItem().toString();
                System.out.println("Testing gender: "+ get_gender);


                //Adding the UserProfile data to the database
                myDBHandler = new MyDBHandler(getApplicationContext());
                sqLiteDatabase = myDBHandler.getWritableDatabase();
                myDBHandler.addUserData(get_name,get_gender,get_weight, sqLiteDatabase);
                Toast.makeText(v.getContext(), "User Profile Data saved!", Toast.LENGTH_LONG).show();
                myDBHandler.close();


            }
        });


    }

}