package com.example.sindhuja.locationtracker;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.sindhuja.locationtracker.Database.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
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
import com.google.maps.android.ui.IconGenerator;

import java.text.DateFormat;
import java.util.Date;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "LocationActivity";
    public static final int DEFAULT_FREQUENCY = 1;
    public static final int DEFAULT_RESULT = 3;
    private int frequencyValue;
    private int resultValue;
    private long interval;
    private long fastestInterval;
    private GoogleMap mMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    SimpleCursorAdapter mAdapter;
    GoogleMap googleMap;
    DatabaseHelper mHelper;
    SQLiteDatabase mDb;
    Cursor mCursor;
    UserPreferences userPreferences;
    private AddressResultReceiver mResultReceiver;
    int zoomLevel = 30;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ...............................");

        userPreferences = new UserPreferences(this);
        mHelper = new DatabaseHelper(this);
        mLocationRequest = new LocationRequest();
        updateLocationRequest();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates(interval, fastestInterval);
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }


    protected void updateLocationRequest() {

        frequencyValue = userPreferences.userSharedPreferences.getInt("frequencyValue",DEFAULT_FREQUENCY);
        resultValue = userPreferences.userSharedPreferences.getInt("resultValue",DEFAULT_RESULT);
        interval = 1000 * 60 * frequencyValue;
        fastestInterval = 1000 * 60 * frequencyValue;

        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        }
        else if (mMap != null) {
            mMap.clear();
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            getDbData();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Tracking your location", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates(interval, fastestInterval);
    }

    protected void startLocationUpdates(long interval, long fastestInterval) {

        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.d(TAG, "Location update started ..............: ");
        }
        else{
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.d(TAG, "Location update started ..............: ");
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    public void onLocationChanged(Location location) {

        Log.d(TAG, "Firing onLocationChanged..............................................");
        mMap.clear();
        getDbData();
        mCurrentLocation = location;
        double longitude;
        double latitude;
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        if (location != null) {
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_TIME, mLastUpdateTime);
            values.put(DatabaseHelper.COLUMN_LATITUDE, latitude);
            values.put(DatabaseHelper.COLUMN_LONGITUDE, longitude);
            mDb.insert(DatabaseHelper.TABLE_NAME, null, values);
            addMarker();

        }

    }

    private void addMarker() {

        MarkerOptions options = new MarkerOptions();
        // following four lines requires 'Google Maps Android API Utility Library'
        // https://developers.google.com/maps/documentation/android/utility/
        // I have used this to display the time as title for location markers
        // you can safely comment the following four lines but for this info
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_RED);
        options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(mLastUpdateTime)));
        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        options.position(currentLatLng);
        Marker mapMarker = mMap.addMarker(options);
        long atTime = mCurrentLocation.getTime();
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(atTime));
        mapMarker.setTitle(mLastUpdateTime);
        mapMarker.showInfoWindow();
        Log.d(TAG, "Marker added.............................");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
                zoomLevel));
        Log.d(TAG, "Zoom done.............................");
    }
    protected void startIntentService(LatLng location) {
        mResultReceiver = new AddressResultReceiver(new Handler());
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }
    private void previousMarker(GoogleMap mMap,double latitude,double longitude,String time) {

        MarkerOptions options = new MarkerOptions();
        // following four lines requires 'Google Maps Android API Utility Library'
        // https://developers.google.com/maps/documentation/android/utility/
        // I have used this to display the time as title for location markers

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_RED);
        options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(time)));
        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        LatLng currentLatLng = new LatLng(latitude, longitude);
        options.position(currentLatLng);
        Marker mapMarker = mMap.addMarker(options);
        mapMarker.setTitle(time);
        mapMarker.showInfoWindow();
        Log.d(TAG, "Marker added.............................");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
                zoomLevel));
        Log.d(TAG, "Zoom done.............................");
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Fetching Address..", Toast.LENGTH_SHORT).show();
        startIntentService(marker.getPosition());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

    //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.FREQUENCY, frequencyValue);
            intent.putExtra(SettingsActivity.RESULT, resultValue);
            startActivityForResult(intent,1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 &&  resultCode==RESULT_OK)
        {
            frequencyValue = data.getIntExtra("Frequency",DEFAULT_FREQUENCY);
            resultValue = data.getIntExtra("Result",DEFAULT_RESULT);
            Data userData = new Data(frequencyValue,resultValue);
            userPreferences.storeUserPreferences(userData);
            mMap.clear();
            updateLocationRequest();
            getDbData();
        }
    }

    private void getDbData(){

        mDb = mHelper.getWritableDatabase();
        String[] columns = new String[] { DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_TIME,
                DatabaseHelper.COLUMN_LONGITUDE, DatabaseHelper.COLUMN_LATITUDE };
        // Fields on the UI to which we map
        mDb = mHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_TIME,
                DatabaseHelper.COLUMN_LONGITUDE, DatabaseHelper.COLUMN_LATITUDE

        };
        String sortOrder = DatabaseHelper.COLUMN_ID + " DESC";
        Cursor cursor = mDb.query(DatabaseHelper.TABLE_NAME, projection, null ,
                null, null, null, sortOrder);
        int iterator=1;
        if(cursor!=null){
            cursor.moveToFirst();
            do {
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE));
                previousMarker(mMap,latitude,longitude,time);
                iterator++;
            } while(cursor.moveToNext() && iterator<resultValue);


        }
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d("AddressResultReceiver", mAddressOutput);
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Address")
                        .setMessage(mAddressOutput)
                        .setCancelable(true)
                        .create().show();
            }

        }
    }
}
