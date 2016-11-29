package com.jwetherell.pedometer.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jwetherell.pedometer.R;

import java.util.concurrent.TimeUnit;

public class UserProfile extends Activity {


    EditText Name;
    Spinner Gender;
    EditText Weight;
    Button Save;
    TextView AvgDist;
    TextView AvgTime;
    TextView AvgWorkouts;
    TextView AvgCalories;
    static TextView AllDist;
    static TextView AllTime;
    static TextView AllWorkouts;
    static TextView AllCalories;
    String get_name = null;
    static int get_weight;
    static String get_gender;
    Intent editintent=getIntent();
    MyDBHandler myDBHandler;
    SQLiteDatabase sqLiteDatabase;


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
        AvgTime = (TextView)findViewById(R.id.textView25);
        AvgWorkouts = (TextView)findViewById(R.id.textView26);
        AvgCalories = (TextView)findViewById(R.id.textView27);
        AllDist = (TextView)findViewById(R.id.textView34);
        AllTime = (TextView)findViewById(R.id.textView40);
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


           /* if(name!=null) {
                myDBHandler = new MyDBHandler(getApplicationContext());
                sqLiteDatabase = myDBHandler.getWritableDatabase();

            }*/

          //  updateData(name, sqLiteDatabase);
        }
        Editable name_get = Name.getText();
        String name_retrieve = name_get.toString();
        try {
            if (name_get != null) {

                myDBHandler = new MyDBHandler(getApplicationContext());
                sqLiteDatabase = myDBHandler.getReadableDatabase();
                System.out.println("Name currently placed: " + name_retrieve);
                updateData(name_retrieve, sqLiteDatabase);
                System.out.println("Testing");
            }
        }catch(NullPointerException e)
        {

        }

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // TODO Auto-generated method stub
                int flag1=1;
                int flag2=1;
                get_name = Name.getText().toString();
                if(get_name!=null) {
                    Log.i("Testing name: ", get_name);
                    flag1 = 1;
                }
                else
                {
                    Toast.makeText(v.getContext(), "Please enter a name!", Toast.LENGTH_LONG).show();
                    flag1 = 0;
                }

               try{
                    get_weight = Integer.parseInt(Weight.getText().toString());
                   flag2 = 1;
                }
               catch(NumberFormatException e){
                   Toast.makeText(v.getContext(), "Please enter weight value!", Toast.LENGTH_LONG).show();
                   flag2 = 0;
                }

                System.out.println("Testing weight: "+ get_weight);
                get_gender = Gender.getSelectedItem().toString();
                System.out.println("Testing gender: "+ get_gender);

                if(flag1==1 && flag2==1) {

                    //Adding the UserProfile data to the database
                    myDBHandler = new MyDBHandler(getApplicationContext());
                    sqLiteDatabase = myDBHandler.getWritableDatabase();
                    myDBHandler.addUserData(get_name, get_gender, get_weight, sqLiteDatabase);
                    Toast.makeText(v.getContext(), "User Profile Data saved!", Toast.LENGTH_LONG).show();
                    myDBHandler.close();

                    //Passing the values to the MainActivity through Intents
                    Intent intent = new Intent(UserProfile.this, Demo.class);
                    Log.i("User Profile: ", "Testing activity 2");
                    intent.putExtra("Username", get_name);
                    intent.putExtra("Usergender", get_gender);
                    intent.putExtra("Userweight", get_weight);
                    startActivity(intent);

                    isTableExists(get_name, true);

                }
                updateData(get_name, sqLiteDatabase);
            }
        });

        //Check if UserProfile already exists. If it does not exist, add it to UserProfiles table and
        // create a new table to input the workout details with name of table as the user's name.
        myDBHandler = new MyDBHandler(getApplicationContext());
        sqLiteDatabase = myDBHandler.getReadableDatabase();


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

    //Update the values in UserProfile
    public void updateData(String table_name, SQLiteDatabase db) {

        if((table_name != null && !table_name.isEmpty()))
        {
        Cursor cur = db.rawQuery("SELECT SUM(Distance_in_km) FROM " + table_name, null);
        if (cur.moveToFirst()) {
            System.out.println("Total Distance as retrieved:" + cur.getDouble(0));
        }
        Log.e("DATABASE OPERATIONS:", "Retrieving data from the database");


        Cursor cur1 = db.rawQuery("SELECT SUM(Duration_in_secs) FROM " + table_name, null);
        if (cur1.moveToFirst()) {
            System.out.println("Total Duration as retrieved:" + cur1.getLong(0));
        }
        Log.e("DATABASE OPERATIONS:", "Retrieving data from the database");


        Cursor cur2 = db.rawQuery("SELECT COUNT(Duration_in_secs) FROM " + table_name, null);
        if (cur2.moveToFirst()) {
            System.out.println("Total no of workouts as retrieved:" + cur2.getInt(0));
        }
        Log.e("DATABASE OPERATIONS:", "Retrieving data from the database");

        Cursor cur3 = db.rawQuery("SELECT SUM(Calories_burnt) FROM " + table_name, null);
        if (cur3.moveToFirst()) {
            System.out.println("Total calories burnt:" + cur3.getInt(0));
        }
        Log.e("DATABASE OPERATIONS:", "Retrieving data from the database");

        String dist = String.format("%.2f",cur.getDouble(0));
        int worktime = cur2.getInt(0);
        int cal = cur3.getInt(0);

        //Convert time to days,hours,mins,seconds
        int day = (int) TimeUnit.SECONDS.toDays(cur1.getLong(0));
        long hours = TimeUnit.SECONDS.toHours(cur1.getLong(0)) -
                TimeUnit.DAYS.toHours(day);
        long minute = TimeUnit.SECONDS.toMinutes(cur1.getLong(0)) -
                TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(cur1.getLong(0)));
        long second = TimeUnit.SECONDS.toSeconds(cur1.getLong(0)) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(cur1.getLong(0)));

        AllDist.setText(dist + " km");
        AllTime.setText(day + " day " + hours + " hour " + minute + " min " + second + " sec");
        AllWorkouts.setText(worktime + " times");
        AllCalories.setText(cal + " Cal");
        System.out.println("Works!!");

        if(worktime!=0) {
            double avgdist = cur.getDouble(0) / worktime;
            String avg_dist = String.format("%.2f", avgdist);
            int avgcal = cur3.getInt(0) / worktime;


            AvgDist.setText(avg_dist + " km");
            AvgTime.setText(day + " day " + hours + " hour " + minute + " min " + second + " sec");
            AvgWorkouts.setText(worktime + " times");
            AvgCalories.setText(avgcal + " Cal");
            System.out.println("Works!!");
        }

       /* dataintent = new Intent(UserProfile.this, Demo.class);
        Log.i("User Profile: ", "Testing activity 2");
        dataintent.putExtra("distance", dist);
        dataintent.putExtra("day", day);
        dataintent.putExtra("worktime", worktime);
        dataintent.putExtra("cal", cal);
        startActivity(dataintent);
*/
        }
    }


}