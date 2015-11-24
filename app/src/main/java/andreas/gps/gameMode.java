package andreas.gps;

// insert here the main game activity
//holoholo

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.Calendar;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.EmptyStackException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.Math;

import andreas.gps.sensoren.SensorCollector;
import andreas.gps.sensoren.Sensor_SAVE;
import andreas.gps.sensoren.SoundAct;

public class gameMode extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, Servercomm.ServercommEventListener {

    //    variables

    private Circle circleLoc;
    private Circle circleTarget;
    private MyLocations locations = new MyLocations();
    private double r = 5.0;
    private static final String TAG = "abcd";
    private LatLng CURRENT_TARGET;
    private Marker markerTarget;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    public Marker mymarker;
    boolean gps_connected = false;
    boolean network_connected = false;
    boolean connections_working = false;
    public float zoomlevel = 18;
    public boolean zoomed = false;
    public LatLng loc;
    static final String STATE_SCORE = "playerScore";
    Calendar c = Calendar.getInstance();
    private double mySpeed = 0;
    private int kill_button_counter = 0;
    //text kkillmoves
    public String TAG2 = "abcdef";
    private String killedText = "kill confirmed";
    private String killedPointsAddedText = "point added!";
    private String killedNotText = "You missed try again!";
    private String killmoveAcellorText="accelerate!";
    private String killmoveGyroText="Shoot him down!";
    private String killmoveSoundText="Scream him to dead!";
    private String killmoveSpeedText="get to your highest speed!";
    private String killmovelightText="Remove al light!";
    private String killmovePressButtonText="Press him to dead!";
    private double killmoveAcellorValue = 0.5;
    private double killmoveGyroValue = 40;
    private double killmoveSoundValue = 25000;
    private double killmoveSpeedValue = 6.4;
    private double killmovelightValue = 2;
    private double killmovePressButtonValue = 5;
    public String myusername = "wout";
    Servercomm mServercomm = new Servercomm();
    public String NotifyOffline = "NotifyOffline";
    public String TargetID = "";
    public String getPriorities = "getPriorities";
    public String eliminated = "eliminated";
    public String pickedTarget = "pickedTarget";
    public int huntedby = 0;
    public String droppedTarget = "droppedTarget";
    public String locationUpdate = "locationUpdate";
    public String priorityID = "";
    private Handler mHandler = new Handler();
    public double prioritylevel = 0;
    public LatLng targetLocation;
    public String priorityCategory = "priorityCategory";
    public String getNewLocation = "getNewLocation";
    public String giveNewLocation = "giveNewLocation";
    public ProgressDialog progressDialog;
    public int missedLocationUpdates = 0;
    ConnectivityManager connectivityManager;
    LocationManager locationManager;
    NetworkInfo activeNetwork;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    TextView points_score;
    TextView hunting;
    TextView hunted;
    public int mypoints=0;
    public int targetpoints;
    public int mymoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Got into oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //login
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_low_in_rank);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Servercomm.registerCallback(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        editor = preferences.edit();
        points_score = (TextView) findViewById(R.id.points_score);
        hunting = (TextView) findViewById(R.id.hunting);
        hunted = (TextView) findViewById(R.id.hunted);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)        // 5 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds
        Log.i(TAG, "Oncreate success");


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);




    }

    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        if (network_connected && gps_connected){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            while (location == null){
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            Log.i(TAG, "Handle New Location.");
            handleNewLocation(location);


        } else if (!network_connected) {
            Log.i(TAG, "No network.");
            show_alertdialog_network();
        } else {
            Log.i(TAG, "No GPS.");
            show_alertdialog_gps();



        }}

    public void show_alertdialog_network() {
        Log.i(TAG, "show_alertdialog_network");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No network!");
        builder.setMessage("Please turn on wifi or network data.");
        builder.setPositiveButton("To network data", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings$DataUsageSummaryActivity"));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("To wifi", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNeutralButton("Nahh", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(gameMode.this, "No game for you!", Toast.LENGTH_SHORT).show();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.show();
    }

    public void show_alertdialog_gps() {
        Log.i(TAG, "show_alertdialog_gps");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No gps!");
        builder.setMessage("Please turn on location services.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Nahh", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(gameMode.this, "No game for you!", Toast.LENGTH_SHORT).show();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.show();
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, "handling New Location");
        this.loc = new LatLng(location.getLatitude(), location.getLongitude());
        Log.i(TAG, String.valueOf(loc));
        if (!zoomed){
            Log.i(TAG,"zooming.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomlevel));
            zoomed = true;
        }
        locations.addMyLocation(loc);

        if (circleLoc == null){
            circleLoc = mMap.addCircle(new CircleOptions()
                    .center(loc)
                    .radius(r)
                    .strokeColor(Color.BLUE));
        } else {
            circleLoc.remove();

            if (mMap != null) {
                circleLoc = mMap.addCircle(new CircleOptions()
                        .center(loc)
                        .radius(r)
                        .strokeColor(Color.BLUE));
            }
        }


        if (mymarker != null) {
            mymarker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .position(loc)
                .title("I am here!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mymarker = mMap.addMarker(options);
        Log.i(TAG, "Marker placed.");

        if (!TargetID.equals("")){
            addPoints(loc,CURRENT_TARGET);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onconnectionfailed");
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location Changed.");
        mySpeed=location.getSpeed();
        handleNewLocation(location);



    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Paused.");
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            mGoogleApiClient.disconnect();
        }

        mymoney += mypoints;
        editor.putInt("mymoney", mymoney);
        editor.apply();
        notifyOffline("");
        notifyOffline(TargetID);
    }

    public void notifyOffline(String receiver){
        JSONObject data = new JSONObject();
        try {
            data.put("sender", myusername);
            data.put("receiver", receiver);
            data.put("category", "");
            data.put("message", NotifyOffline);
            data.put("points", mypoints);
            data.put("latitude", loc.latitude);
            data.put("longitude", loc.longitude);
        } catch (JSONException e){
            Log.e(TAG,e.toString());
        }
        mServercomm.sendMessage(data);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Onresume");
        zoomed = false;
        super.onResume();
        if (activeNetwork != null && activeNetwork.isConnected()) network_connected = true;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) gps_connected = true;
        Log.i(TAG, "Connecting apiclient");
        mGoogleApiClient.connect();
        Log.i(TAG, "getting score");
        mypoints = 0;
        mymoney = preferences.getInt("mypoints",0);
        mypoints += mymoney;
        points_score.setText(Integer.toString(mypoints));
        hunted.setText("You are not being hunted");
        hunting.setText("You are not hunting");

        Log.i(TAG, "got score");

    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        Log.i(TAG,"CalculationByDistance");
        int Radius = 6371000;// radius of earth in Km
        try {
            double lat1 = StartP.latitude;
            double lat2 = EndP.latitude;
            double lon1 = StartP.longitude;
            double lon2 = EndP.longitude;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                    * Math.sin(dLon / 2);
            double c = 2 * Math.asin(Math.sqrt(a));
            return Radius * c;
        } catch(NullPointerException e) {
            return 20.0;
        }
    }

    public void addPoints(LatLng location, LatLng target) {
        if (CalculationByDistance(location, target) <= r*2) {
            killMovegenerator(null);
            changeTarget();
        }
    }

    public void killMovegenerator(View view){
        int seconds = c.get(Calendar.SECOND);
        if (seconds<10){
            killMoveAccelor(null);
        }
        else if (seconds>=10 && seconds<20){
            killMoveGyroscoop(null);
        }
        else if (seconds>=20 && seconds<30){
            killMoveSound(null);
        }
        else if (seconds>=40 && seconds<50){
            killMoveSpeed(null);
        }
        else if (seconds>=50&& seconds<55){
            killMovePressButton(null);
        }
        else if (seconds>=55){
            killMovelight(null);
        }
    }

    public void killMoveAccelor(View view) {

        new CountDownTimer(5000, 200) {
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);
            public Sensor_SAVE sensorsave = new Sensor_SAVE();
            SensorCollector sensorcol = new SensorCollector(sensorsave);

            public void onTick(long millisUntilFinished) {
                killMoveText.setVisibility(View.VISIBLE);
                killMoveText.setText(killmoveAcellorText + millisUntilFinished / 1000);
                sensorcol.start(getApplicationContext());
                try {

                    if (sensorsave.getAccelerox() > killmoveAcellorValue) {
                        killMoveText.setText(killedText);


                    }
                } catch (EmptyStackException e){
                    Log.i(TAG,e.toString());
                }
            }

            public void onFinish() {
                sensorcol.stop();
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    mypoints += 100;
                    points_score.setText(Integer.toString(mypoints));
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();

    }

    public void killMoveSound(View view) {

        new CountDownTimer(5000, 200) {
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);
            SoundAct soundact = new SoundAct(0);
            public void onTick(long millisUntilFinished) {
                killMoveText.setVisibility(View.VISIBLE);
                killMoveText.setText(killmoveSoundText + millisUntilFinished / 1000);
                soundact.getMaxsound();
                if (soundact.getMaxsound() > killmoveSoundValue) {
                        killMoveText.setText(killedText);

                }
            }

            public void onFinish() {
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    mypoints += 100;
                    points_score.setText(Integer.toString(mypoints));
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();

    }

    public void killMoveGyroscoop(View view) {

        new CountDownTimer(5000, 200) {
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);
            public Sensor_SAVE sensorsave = new Sensor_SAVE();
            SensorCollector sensorcol = new SensorCollector(sensorsave);

            public void onTick(long millisUntilFinished) {
                killMoveText.setVisibility(View.VISIBLE);
                killMoveText.setText(killmoveGyroText + millisUntilFinished / 1000);
                sensorcol.start(getApplicationContext());
                try {
                    if (sensorsave.getGyroscoopx() > killmoveGyroValue) {
                        killMoveText.setText(killedText);


                    }
                } catch (EmptyStackException e){
                    Log.i(TAG,e.toString());
                }
            }
            public void onFinish() {
                sensorcol.stop();
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    mypoints += 100;
                    points_score.setText(Integer.toString(mypoints));
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();

    }

    public void killMovelight(View view) {

        new CountDownTimer(5000, 200) {
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);
            public Sensor_SAVE sensorsave = new Sensor_SAVE();
            SensorCollector sensorcol = new SensorCollector(sensorsave);

            public void onTick(long millisUntilFinished) {
                killMoveText.setVisibility(View.VISIBLE);
                killMoveText.setText(killmovelightText + millisUntilFinished / 1000);
                sensorcol.start(getApplicationContext());
                try {

                    if (sensorsave.getLicht() < killmovelightValue) {
                        killMoveText.setText(killedText);


                    }
                } catch (EmptyStackException e) {
                    Log.i(TAG, e.toString());
                }
            }



            public void onFinish() {
                sensorcol.stop();
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    mypoints += 100;
                    points_score.setText(Integer.toString(mypoints));
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();

    }

    public void changeTarget() {
        Log.i(TAG, "changeTarget");
        prioritylevel = 0;
        priorityID = "";
        JSONObject data = new JSONObject();
        try {
            data.put("sender", myusername);
            data.put("receiver", "");
            data.put("category", "");
            data.put("message", getPriorities);
            data.put("points",mypoints);
            data.put("latitude",loc.latitude);
            data.put("longitude",loc.longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mServercomm.sendMessage(data);
        Log.i(TAG, "starting progressdialog");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Picking target..");
        progressDialog.show();
        Runnable changeMessage = new Runnable() {

            @Override
            public void run() {
                if (!priorityID.equals("")) {
                    Log.i(TAG, "progressdialog changing text");
                    TargetID = priorityID;
                    notifyHunting();
                    progressDialog.setMessage("Tracking target..");

                } else {
                    Log.i(TAG, "no target found yet");
                    progressDialog.dismiss();
                    show_alertdialog_target();
                }
                targetLocationRequest.run();
            }
        };

        new Handler().postDelayed(changeMessage, 4000);
    }

    public void notifyHunting(){
        JSONObject data = new JSONObject();
        try {
            data.put("sender", myusername);
            data.put("receiver", TargetID);
            data.put("category", pickedTarget);
            data.put("message", "");
            data.put("points", mypoints);
            data.put("latitude", loc.latitude);
            data.put("longitude", loc.longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void show_alertdialog_target(){
        Log.i(TAG, "show_alertdialog_target");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No target found :(");
        builder.setMessage("Don't quit. We will try again in a minute.");
        builder.setPositiveButton("Wait for another player", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG, "Continue playing");
            }
        });
        builder.setNegativeButton("Quit gamemode", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG, "Quit gamemode");
                finish();
            }
        });

        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.show();
    }

    public void killMoveSpeed(View view) {

        new CountDownTimer(5000, 200) {
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);

            public void onTick(long millisUntilFinished) {
                killMoveText.setVisibility(View.VISIBLE);
                killMoveText.setText(killmoveSpeedText + millisUntilFinished / 1000);
                if (mySpeed > killmoveSpeedValue) {
                    killMoveText.setText(killedText);
                }
            }

            public void onFinish() {
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    mypoints += 100;
                    points_score.setText(Integer.toString(mypoints));
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();
    }

    public void killMoveCounter(View view){
        kill_button_counter += 1;
    }

    public void killMovePressButton(View view) {
        new CountDownTimer(5000, 200) {
            Button kill_button = (Button) findViewById(R.id.kill_button);
            TextView killMoveText = (TextView) findViewById(R.id.killMoveText);
            TextView points_score = (TextView) findViewById(R.id.points_score);

            public void onTick(long millisUntilFinished) {
                killMoveText.setText(killmovePressButtonText + millisUntilFinished / 1000);
                kill_button.setVisibility(View.VISIBLE);
                if (kill_button_counter > killmovePressButtonValue) {
                    kill_button.setText(killedText);
                    killMoveText.setText(killedText);


                }
            }

            public void onFinish() {
                kill_button_counter = 0;
                if (killMoveText.getText() == killedText) {
                    killMoveText.setText(killedPointsAddedText);
                    killMoveText.setVisibility(View.GONE);
                    mypoints += 100;
                    points_score.setText(Integer.toString(mypoints));
                } else {
                    killMoveText.setText(killedNotText);
                    killMoveText.setVisibility(View.GONE);
                }

            }
        }.start();

    }

    public void zoombutton(View view) {
        Log.i(TAG, "clicked!");
//        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//        Log.i(TAG, String.valueOf(location));
        if (loc != null) {
            Log.i(TAG, "moving camera");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomlevel));
        }
    }

    public void targetbutton(View view) {
        Log.i(TAG, "Targetbutton pressed.");
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(loc);
        boundsBuilder.include(CURRENT_TARGET);
// pan to see all markers on map:
        LatLngBounds bounds = boundsBuilder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void requestLocationUpdate(){
        Log.i(TAG,"requestLocationUpdate");
        JSONObject data = new JSONObject();
        try{
            data.put("sender", myusername);
            data.put("receiver",TargetID);
            data.put("category",locationUpdate);
            data.put("message",getNewLocation);
            data.put("points",mypoints);
            data.put("latitude",loc.latitude);
            data.put("longitude",loc.longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mServercomm.sendMessage(data);
    }

    public void sendPriority(String sender, LatLng targetLocation) {
        if (CalculationByDistance(loc, targetLocation) < 5000) {
            Log.i(TAG, "Send priority");
            //TextView points_score = (TextView) findViewById(R.id.points_score);
            //String points_str = (String) points_score.getText();
            //int points_int = Integer.parseInt(points_str);
            int points_int = 1000;
            double priority = 2 * Math.log10(points_int);
            priority -= huntedby;
            JSONObject data = new JSONObject();
            try {
                data.put("sender", myusername);
                data.put("receiver", sender);
                data.put("category", priorityCategory);
                data.put("message", Double.toString(priority));
                data.put("points", mypoints);
                data.put("latitude", loc.latitude);
                data.put("longitude", loc.longitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mServercomm.sendMessage(data);
        }
    }
    public void gotEliminated(String sender, String message){
        Log.i(TAG,"gotEliminated");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Aww.");
        builder.setMessage("You got killed by +"+sender+" for "+message+" points.");
        builder.setPositiveButton("Continue playing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

                // halveer punten ofzo
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.show();
        }

    public void sendLocation(String sender){
        Log.i(TAG,"sending location");
        JSONObject data = new JSONObject();
        try{
            data.put("sender", myusername);
            data.put("receiver",sender);
            data.put("category",locationUpdate);
            data.put("message",giveNewLocation);
            data.put("latitude",loc.latitude);
            data.put("longitude",loc.longitude);
            data.put("points",mypoints);
        } catch (JSONException e) {
            Log.i(TAG,"sendLocation exception");
            e.printStackTrace();
        }
        mServercomm.sendMessage(data);
    }

    public void updateTargetLocation(LatLng location) {
        if (CalculationByDistance(loc, location) > 10000) {
            hunting.setText("You are hunting " + TargetID + " with " + Integer.toString(targetpoints)+ " points");
            Log.i(TAG, "updating target location");
            CURRENT_TARGET = location;
            if (CURRENT_TARGET == null) {
                Log.i(TAG, "nullexception");
            }
            Log.i(TAG, "exception");
            if (markerTarget == null && circleTarget == null) {
                markerTarget = mMap.addMarker(new MarkerOptions().position(CURRENT_TARGET).title("TARGET"));
                circleTarget = mMap.addCircle(new CircleOptions()
                        .center(CURRENT_TARGET)
                        .radius(5)
                        .strokeColor(Color.RED));
            } else {
                assert markerTarget != null;
                markerTarget.remove();
                circleTarget.remove();

                markerTarget = mMap.addMarker(new MarkerOptions().position(CURRENT_TARGET).title("TARGET"));
                circleTarget = mMap.addCircle(new CircleOptions()
                        .center(CURRENT_TARGET)
                        .radius(5)
                        .strokeColor(Color.RED));

            }

            locations.addTargetLocation(CURRENT_TARGET);
        } else {
            changeTarget();
        }
    }

    public void respondToMessage() {
        Log.i(TAG2,"starting respondtomessage");
        List receivedmessage = mServercomm.getLastMessage();
        try {
            String receiver = (String) receivedmessage.get(0);
            String sender = (String) receivedmessage.get(1);
            String category = (String) receivedmessage.get(2);
            String message = (String) receivedmessage.get(3);
            Integer points = (Integer) receivedmessage.get(4);
            Double Latitude = (Double) receivedmessage.get(4);
            Double Longitude = (Double) receivedmessage.get(5);
            targetLocation = new LatLng(Latitude, Longitude);

            Log.i(TAG2, "message");
            Log.i(TAG2, message);
            Log.i(TAG2, "category");
            Log.i(TAG2, category);
            Log.i(TAG2, "receiver");
            Log.i(TAG2, receiver);
            Log.i(TAG2, "sender");
            Log.i(TAG2, sender);
            Log.i(TAG2, "points");
            Log.i(TAG2, points.toString());
            if (receiver.equals("") && !(sender.equals(myusername))) {
                Log.i(TAG2, "Condition 1");
                if (message.equals(NotifyOffline) && sender.equals(TargetID)) {
                    Log.i(TAG2, "Condition 1.1");
                    changeTarget();
                } else if (message.equals(getPriorities)) {
                    Log.i(TAG2, "Condition 1.2");
                    sendPriority(sender,targetLocation);
                }
            } else if (receiver.equals(myusername)) {
                Log.i(TAG2, "Condition 2");
                if (category.equals(eliminated)) {
                    Log.i(TAG2, "Condition 2.1");
                    gotEliminated(sender, message);
                } else if (category.equals(pickedTarget)) {
                    Log.i(TAG2, "Condition 2.2");
                    huntedby += 1;
                    hunted.setText("You are being hunted by "+Integer.toString(huntedby)+" people");
                } else if (category.equals(NotifyOffline)) {
                    Log.i(TAG2, "Condition 2.3");
                    huntedby -= 1;
                    if (huntedby == 0){
                        hunted.setText("You are not being hunted");
                    }else{
                        hunted.setText("You are being hunted by "+Integer.toString(huntedby)+" people");
                    }
                } else if (category.equals(locationUpdate)) {
                    Log.i(TAG2, "Condition 2.4");
                    if (message.equals(getNewLocation)) {
                        Log.i(TAG2, "Condition 2.4.1");
                        sendLocation(sender);
                    } else if (message.equals(giveNewLocation)) {
                        Log.i(TAG2, "Condition 2.4.2");
                        progressDialog.dismiss();
                        missedLocationUpdates = 0;
                        targetpoints = points;
                        updateTargetLocation(targetLocation);
                    }
                } else if (category.equals(priorityCategory)) {
                    Log.i(TAG2, "Condition 2.5");
                    Log.i(TAG2, "Condition 2.5 part 2");
                    if (Double.parseDouble(message) > prioritylevel) {
                        Log.i(TAG2, "Condition 2.5.1");
                        prioritylevel = Double.parseDouble(message);
                        priorityID = sender;
                    }
                }
            } else {
                Log.i(TAG2, "Condition 3");
            }
        } catch (IndexOutOfBoundsException e){
            Log.i(TAG,e.toString());
        }
    }

    private Runnable targetLocationRequest = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "running targetLocationRequest");
            if (!TargetID.equals("") && missedLocationUpdates < 2){
                Log.i(TAG, "request LocationUpdate");
                requestLocationUpdate();
                mHandler.postDelayed(targetLocationRequest,20000);
            } else if (missedLocationUpdates < 2){
                Log.i(TAG, "Track new target");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changeTarget();
                    }
                },60000);
            } else {
                Toast.makeText(gameMode.this, "Target not responding, getting new target", Toast.LENGTH_SHORT).show();
                changeTarget();
            }
        }};}




