package andreas.gps;


import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class mainInt extends AppCompatActivity
        implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //    variables

    public boolean zoomed = false;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = "abcd";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    public Marker marker;
    boolean gps_connected = false;
    boolean network_connected = false;
    boolean connections_working = false;
    public float zoomlevel = 15;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    public Menu mymenu;
    public MenuItem logout;
    public MenuItem login;
    public MenuItem user;


    // switch to other activity functions

    public void switchGameMode() {
        if (!preferences.getString("myusername","").equals("")) {
            Log.i(TAG,preferences.getString("myusername",""));
            Intent intent = new Intent(this, gameMode.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
        }
    }
    public void switchLogin() {
        Intent intent = new Intent(this, login.class);
        startActivity(intent);
    }
    public void switchLogout(){
        editor.putString("myusername", "");
        editor.apply();
        login.setEnabled(true);
        logout.setEnabled(false);
        user.setEnabled(false);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Not logged in");

        Toast.makeText(this, "You are now logged out.", Toast.LENGTH_SHORT).show();
    }
    public void switchUser(){}
    public void switchData1() {
        Intent intent = new Intent(this, data1.class);
        startActivity(intent);
    }
    public void switchData2() {
        Intent intent = new Intent(this, data2.class);
        startActivity(intent);
    }
    public void switchData3() {
        Intent intent = new Intent(this, data3.class);
        startActivity(intent);

    }
    public void switchMiniggame1() {
        Intent intent = new Intent(this, minigame1.class);
        startActivity(intent);
    }
    public void switchMiniggame2() {
        Intent intent = new Intent(this, minigame2.class);
        startActivity(intent);
    }
    public void switchMinigame3() {
        Intent intent = new Intent(this, minigame3.class);
        startActivity(intent);
    }



    // build in functions

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_buttons, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer);

        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Home");


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mymenu = navigationView.getMenu();

        //location
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Log.i(TAG, "APIclient created");
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)        // 5 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds


        login = mymenu.findItem(R.id.login_toolbar);
        logout = mymenu.findItem(R.id.logout_toolbar);
        user = mymenu.findItem(R.id.user_toolbar);

        preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        editor = getSharedPreferences("MyPreferences",Context.MODE_PRIVATE).edit();
    }


    @Override
    public void onDestroy(){
        editor.putString("myusername","");
        editor.apply();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady");
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        if (connections_working){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            while (location == null){
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
                Toast.makeText(mainInt.this, "No game for you!", Toast.LENGTH_SHORT).show();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.show();
    }


    public void show_alertdialog_gps(){
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
                Toast.makeText(mainInt.this, "No game for you!", Toast.LENGTH_SHORT).show();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.show();
    }


    private void handleNewLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        final LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        if (marker != null) {
            marker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker = mMap.addMarker(options);

        final Button zoombutton = (Button)findViewById(R.id.zoombutton);

        zoombutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "clicked!");
                Log.i(TAG, String.valueOf(latLng));
                Log.i(TAG, "moving camera");
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomlevel));
            }
        });
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
        handleNewLocation(location);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.gameMode:
                switchGameMode();
                break;

        }
        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        else if (id == R.id.gameMode) {
//            switchGameMode(null);
//        }
//        else if (id == R.id.login_toolbar) {
//            switchLogin(null);
//        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.data1) {
            switchData1();
        }
        else if (id == R.id.data2) {
            switchData2();
        }
        else if (id == R.id.data3) {
            switchData3();
        }
        else if (id == R.id.minigame1) {
            switchMiniggame1();
        }
        else if (id == R.id.minigame2) {
            switchMiniggame2();
        }
        else if (id == R.id.minigame3) {
            switchMinigame3();
        }
        else if (id == R.id.login_toolbar) {
            switchLogin();
        }
        else if (id == R.id.logout_toolbar){
            switchLogout();
        }
        else if (id == R.id.user_toolbar){
            switchUser();
        }
 
 
        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;*/
        return super.onOptionsItemSelected(menuItem);
    }

    public void LoggedIn(){

        if (preferences.getString("myusername", "").equals("")) {
            login.setEnabled(true);
            logout.setEnabled(false);
            user.setEnabled(false);
        } else {
            Log.i(TAG,"setinvisible");
            login.setEnabled(false);
            logout.setEnabled(true);
            user.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Onresume");
        zoomed = false;
        super.onResume();
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) network_connected = true;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) gps_connected = true;
        if (network_connected && gps_connected) connections_working = true;
        Log.i(TAG, "Connecting apiclient");
        mGoogleApiClient.connect();
        String username = preferences.getString("myusername","");
        if (!username.equals("")) {
            Log.i(TAG,username+" logged in");
            assert getSupportActionBar() != null;
            getSupportActionBar().setTitle("Welcome "+username);
        } else {
            Log.i(TAG,"not logged in");
            assert getSupportActionBar() != null;
            getSupportActionBar().setTitle("Not logged in");
        }
        LoggedIn();

    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Paused.");
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }
}