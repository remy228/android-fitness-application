package com.jwetherell.pedometer.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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
    Intent editintent=getIntent();

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


        Toast.makeText(UserProfile.this, "Please Enter/Verify your profile details!", Toast.LENGTH_LONG).show();
        //Recieving Intent values to set in the edittext
        editintent = new Intent(UserProfile.this, UserProfile.class);
        Bundle extras=getIntent().getExtras();
        if(extras!=null) {
            String name = extras.getString("Username");
            Name.setText(name);
            int weight = extras.getInt("Userweight");
            Weight.setText(""+weight);
            String gender = extras.getString("Usergender");
            ArrayAdapter myAdap = (ArrayAdapter) Gender.getAdapter(); //cast to an ArrayAdapter
            int spinnerPosition = myAdap.getPosition(gender);
            Gender.setSelection(spinnerPosition);
        }


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

                //Check if UserProfile already exists. If it does not exist, add it to UserProfiles table and
                // create a new table to input the workout details with name of table as the user's name.
                myDBHandler = new MyDBHandler(getApplicationContext());
                sqLiteDatabase = myDBHandler.getReadableDatabase();


                //Adding the UserProfile data to the database
                myDBHandler = new MyDBHandler(getApplicationContext());
                sqLiteDatabase = myDBHandler.getWritableDatabase();
                myDBHandler.addUserData(get_name,get_gender,get_weight, sqLiteDatabase);
                Toast.makeText(v.getContext(), "User Profile Data saved!", Toast.LENGTH_LONG).show();
                myDBHandler.close();

                //Passing the values to the MainActivity through Intents
                Intent intent = new Intent(UserProfile.this, Demo.class);
                Log.i("User Profile: ", "Testing activity 2");
                intent.putExtra("Username", get_name);
                intent.putExtra("Usergender", get_gender);
                intent.putExtra("Userweight", get_weight);
                startActivity(intent);

                isTableExists(get_name,true);

            }
        });


    }

    //Check if table exists in database
    public boolean isTableExists(String get_name, boolean openDb) {
        if(openDb) {
            if(sqLiteDatabase == null || !sqLiteDatabase.isOpen()) {
                sqLiteDatabase = myDBHandler.getReadableDatabase();
            }

            if(!sqLiteDatabase.isReadOnly()) {
                sqLiteDatabase.close();
                sqLiteDatabase = myDBHandler.getReadableDatabase();
            }
        }

        Cursor cursor = sqLiteDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+get_name+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                System.out.println("User exists!");

            }
            else {
                System.out.println("User does not exist!");
                myDBHandler = new MyDBHandler(getApplicationContext());
                sqLiteDatabase = myDBHandler.getWritableDatabase();
                myDBHandler.createWorkoutTable(get_name,sqLiteDatabase);
            }
        }
        return false;
    }

}