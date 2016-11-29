package com.jwetherell.pedometer.activity;

import java.util.ArrayList;
import java.util.logging.Logger;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jwetherell.pedometer.service.IStepService;
import com.jwetherell.pedometer.service.IStepServiceCallback;
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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;


/**
 * This class extends Activity to handle starting and stopping the pedometer
 * service and displaying the steps.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class Demo extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

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
    static String get_name;
    static String get_gender;
    static int get_weight;
    Intent recieveIntent = getIntent();
    static TextView distance;
    static TextView duration;
    static double dist;
    static int calories_burnt;


    MyDBHandler myDBHandler;
    SQLiteDatabase sqLiteDatabase;

    GoogleMap mMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    LatLng latLng;
    GoogleMap mGoogleMap;
    SupportMapFragment mFragment;
    Marker currLocationMarker;
    private ArrayList<LatLng> points; //added
    Polyline line;
    private static final String TAG = "MainActivity";
    private static final long INTERVAL = 1000 * 60 * 1; //1 minute
    private static final long FASTEST_INTERVAL = 1000 * 60 * 1; // 1 minute
    private static final float SMALLEST_DISPLACEMENT = 0.25F; //quarter of a meter



    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        points = new ArrayList<LatLng>();
        distance = (TextView)findViewById(R.id.textView16);
        duration = (TextView)findViewById(R.id.textView19);
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
                    //Update distance textView dynamically



                    //Button is ON
                    Log.i("Toggle button test", "Started");
                 //   startTimer();
                    t.start();
                   } else {    //Button is OFF
                    Log.i("Toggle button test", "Stopped");

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

     //Maps Stuff
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
       /* SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap()*/
        mapFragment.getMapAsync(this);


    }

    int hr= 0;
    int min = 0;
    int sec = 0;
    final Thread t = new Thread() {

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            updatedistance();
                            sec+=1;
                            if (sec>59) {
                                min+=1;
                                sec=0;
                            }
                            if (min>59) {
                                hr+=1;
                                min=0;
                            }
                            String hr_new =  String.format("%02d", hr);
                            String min_new = String.format("%02d", min);
                            String sec_new = String.format("%02d", sec);
                            duration.setText(hr_new + ":" + min_new + ":" + sec_new);


                            // Toast.makeText(Demo.this, "Thread running!", Toast.LENGTH_LONG).show();

                        }
                    });
                }
            } catch (InterruptedException e) {
            }
        }
    };


    // Function to update the distance covered in Realtime

    private static void updatedistance() {

        float Cal_per_km = (float) (get_weight * 0.3125);

        String a;
        double X;
        if (get_gender == "Female") {

            // 1 step = 1/1491 km
            dist = 0.00067 * Steps;
            X = Cal_per_km / 1312;
            a = String.format("%.2f", dist);

        } else {
            // 1 step = 1/1312.4 km
            dist = 0.00076 * Steps;
             X = Cal_per_km / 1491;
            a = String.format("%.2f", dist);
         }
       calories_burnt=  (int) (Steps * X);
       distance.setText(String.valueOf(a));

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
                    t.interrupt();
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
            t.interrupt();
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
        storeData();
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
        Intent intent = new Intent(Demo.this, UserProfile.class);
        Log.i("User Profile: ", "Testing activity 2");
        intent.putExtra("Username", get_name);
        intent.putExtra("Usergender", get_gender);
        intent.putExtra("Userweight", get_weight);
        System.out.println("Sending intent values for persisting in userprofile: " + get_name + " " + get_gender + " " + get_weight);
        startActivity(intent);
    }

    int Cal_burnt;
    static float distance_covered;
    public void storeData() {

        float Cal_per_km = (float) (get_weight * 0.3125);
        System.out.println("Calories burnt in a kilometer: " + Cal_per_km + "Weight: " + get_weight );
        System.out.println("Steps: " + Steps);

       if (get_gender == "Female") {

           double X = Cal_per_km / 1491;
           System.out.println("Calories per step:" + X);
             Cal_burnt= (int) (Steps * X);
            System.out.println("Calories burnt:" + Cal_burnt);
            // 1 step = 1/1491 km
            distance_covered = (float) (0.00067 * Steps);
            System.out.println("Distance covered: " + distance_covered);
        } else {
            double X = Cal_per_km / 1312;
            System.out.println("Calories per step:" + X);
            Cal_burnt = (int) (Steps * X);
            System.out.println("Calories burnt:" + Cal_burnt);
            // 1 step = 1/1312.4 km
            distance_covered = (float) (0.00076 * Steps);
            System.out.println("Distance covered: " + distance_covered);
        }
        myDBHandler = new MyDBHandler(getApplicationContext());
        sqLiteDatabase = myDBHandler.getWritableDatabase();
        myDBHandler.addWorkoutDetails(get_name,Steps,distance_covered,Cal_burnt, sqLiteDatabase);


    }

    @Override
    public void onMapReady(GoogleMap gMap) {
       /* mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        mGoogleMap = gMap;
        mGoogleMap.setMyLocationEnabled(true);

        buildGoogleApiClient();

        mGoogleApiClient.connect();

    }

    protected synchronized void buildGoogleApiClient() {
     //   Toast.makeText(this,"buildGoogleApiClient",Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
    //    Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            //place marker at current position
            //mGoogleMap.clear();
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            currLocationMarker = mGoogleMap.addMarker(markerOptions);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) {
            Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        //place marker at current position
        //mGoogleMap.clear();
        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currLocationMarker = mGoogleMap.addMarker(markerOptions);

       // Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();

        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(400).build();

        try {

            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

            //If you only need one location, unregister the listener
            //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        } catch (NullPointerException e) {
                System.out.println("Location error:" + e);
        }

        points.add(latLng); //added

        redrawLine();
    }

    private void redrawLine(){

        mGoogleMap.clear();  //clears all Markers and Polylines

        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("New Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mGoogleMap.addMarker(markerOptions); //add Marker in current position
        line = mGoogleMap.addPolyline(options); //add Polyline
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
            Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }
}