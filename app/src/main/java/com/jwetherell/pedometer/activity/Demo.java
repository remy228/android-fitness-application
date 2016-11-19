package com.jwetherell.pedometer.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import com.jwetherell.pedometer.service.IStepService;
import com.jwetherell.pedometer.service.IStepServiceCallback;
import com.jwetherell.pedometer.service.StepDetector;
import com.jwetherell.pedometer.service.StepService;
import com.jwetherell.pedometer.utilities.MessageUtilities;
import com.jwetherell.pedometer.R;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.jwetherell.pedometer.activity.UserProfile;


/**
 * This class extends Activity to handle starting and stopping the pedometer
 * service and displaying the steps.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class Demo extends Activity {

    private static final Logger logger = Logger.getLogger(Demo.class.getSimpleName());

    private static ToggleButton startStopButton = null;
    private static ArrayList<String> sensArrayList = null;
    private static ArrayAdapter<CharSequence> modesAdapter = null;
    private static TextView text = null;

    private static PowerManager powerManager = null;
    private static WakeLock wakeLock = null;

    public static IStepService mService = null;
    public static Intent stepServiceIntent = null;

    ToggleButton tB;
    private static int sensitivity = 50;
    static int Steps;
    String get_name;
    String get_gender;
    int get_weight;
    Intent recieveIntent = getIntent();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Demo");

        if (stepServiceIntent == null) {
            Bundle extras = new Bundle();
            extras.putInt("int", 1);
            stepServiceIntent = new Intent(Demo.this, StepService.class);
            stepServiceIntent.putExtras(extras);
        }

        startStopButton = (ToggleButton) this.findViewById(R.id.StartStopButton);
        startStopButton.setOnCheckedChangeListener(startStopListener);

        String sensStr = String.valueOf(sensitivity);
        int idx = 0;
        text = (TextView) this.findViewById(R.id.text);

        // Code for implementing Fragments
        Configuration config = getResources().getConfiguration();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LandscapeFragment landscapeFragment = new LandscapeFragment();
            fragmentTransaction.replace(android.R.id.content, landscapeFragment);

        } else {
            PortraitFragment portraitFragment = new PortraitFragment();
            fragmentTransaction.replace(android.R.id.content, portraitFragment);

        }
        fragmentTransaction.commit();
        //Ends here

        //Start Workout button onclick listener
        tB = (ToggleButton) findViewById(R.id.StartStopButton);
        tB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (tB.isChecked()) {
                    //Button is ON
                    Log.i("Toggle button test", "Started");
                } else {    //Button is OFF
                    Log.i("Toggle button test", "Stopped");
                    storeData();
                }

            }
        });

        //Intent for recieving the UserProfile data
        recieveIntent = new Intent(this, UserProfile.class);
         Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            get_name = extras.getString("Username");
            get_gender = extras.getString("Usergender");
            get_weight = extras.getInt("Userweight");
            System.out.println("Intent test: " + get_name + " " + get_gender + " " + get_weight);

        }
        else
        {
            System.out.println("Intent test failed");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();

        if (!wakeLock.isHeld()) wakeLock.acquire();

        // Bind without starting the service
        try {
            bindService(stepServiceIntent, mConnection, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        if (wakeLock.isHeld()) wakeLock.release();

        unbindStepService();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            try {
                if (mService != null && mService.isRunning()) {
                    MessageUtilities.confirmUser(Demo.this, "Exit App without stopping workout?", yesExitClick, null);
                } else {
                    stop();

                    finish();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private OnCheckedChangeListener startStopListener = new OnCheckedChangeListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            boolean serviceIsRunning = false;
            try {
                if (mService != null) serviceIsRunning = mService.isRunning();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (isChecked && !serviceIsRunning) {
                start();
            } else if (!isChecked && serviceIsRunning) {
                MessageUtilities.confirmUser(Demo.this, "Stop the workout?", yesStopClick, noStopClick);
            }
        }
    };

    private DialogInterface.OnClickListener yesStopClick = new DialogInterface.OnClickListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            stop();
        }
    };

    private static final DialogInterface.OnClickListener noStopClick = new DialogInterface.OnClickListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mService != null) try {
                startStopButton.setChecked(mService.isRunning());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private DialogInterface.OnClickListener yesExitClick = new DialogInterface.OnClickListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            unbindStepService();

            finish();
        }
    };

    private void start() {
        logger.info("start");

        startStepService();
        bindStepService();
    }

    private void stop() {
        logger.info("stop");

        unbindStepService();
        stopStepService();
    }

    private void startStepService() {
        try {
            startService(stepServiceIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopStepService() {
        try {
            stopService(stepServiceIntent);
        } catch (Exception e) {
            // Ignore
        }
    }

    private void bindStepService() {
        try {
            bindService(stepServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unbindStepService() {
        try {
            unbindService(mConnection);
        } catch (Exception e) {
            // Ignore
        }
    }


    private static final Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            int current = msg.arg1;
            text.setText("Steps = " + current);

        }
    };

    private static final IStepServiceCallback.Stub mCallback = new IStepServiceCallback.Stub() {

        @Override
        public IBinder asBinder() {
            return mCallback;
        }

        @Override

        public void stepsChanged(int value) throws RemoteException {
            logger.info("Steps=" + value);
            Message msg = handler.obtainMessage();
            msg.arg1 = value;
            handler.sendMessage(msg);
            Steps=value;


        }

    };


    private static final ServiceConnection mConnection = new ServiceConnection() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            logger.info("onServiceConnected()");
            mService = IStepService.Stub.asInterface(service);
            try {
                mService.registerCallback(mCallback);
                mService.setSensitivity(sensitivity);
                startStopButton.setChecked(mService.isRunning());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceDisconnected(ComponentName className) {
            logger.info("onServiceDisconnected()");
            try {
                startStopButton.setChecked(mService.isRunning());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mService = null;
        }
    };

    public void userProfileview(View view) {
        Intent intent = new Intent(this, UserProfile.class);
        Log.i("User Profile: ", "Testing activity 2");
        startActivity(intent);
    }

    public void storeData() {

        float Cal_per_km = (float) (get_weight * 0.3125);
        System.out.println("Calories burnt in a kilometer: " + Cal_per_km + "Weight: " + get_weight );
        System.out.println("Steps: " + Steps);
       if (get_gender == "Female") {

           double X = Cal_per_km / 1491;
           System.out.println("Calories per step:" + Cal_per_km);
            double Cal_burnt = Steps * X;
            System.out.println("Calories burnt:" + Cal_burnt);
            // 1 step = 1/1491 km
            double distance =  (0.00067 * Steps);
            System.out.println("Distance covered: " + distance);
        } else {
            double X = Cal_per_km / 1312;
            System.out.println("Calories per step:" + Cal_per_km);
            double Cal_burnt = Steps * X;
            System.out.println("Calories burnt:" + Cal_burnt);
            // 1 step = 1/1312.4 km
            double distance = (float) (0.00076 * Steps);
            System.out.println("Distance covered: " + distance);
        }
    }


}