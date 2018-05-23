package com.group14.events_near_me;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.group14.events_near_me.event_view.EventViewFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends FragmentActivity implements LocationListener, SensorEventListener {
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private Location location;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] accelerometerData;
    private float[] magnetometerData;
    private String viewedEventID;
    private float rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add each of the three fragments to the adapter
        /*fragments.add(getSupportFragmentManager().findFragmentById(R.id.mainMapFragmentContainer));
        fragments.add(getSupportFragmentManager().findFragmentById(R.id.mainListFragmentContainer));
        fragments.add(getSupportFragmentManager().findFragmentById(R.id.mainFilterFragmentContainer));*/
        fragments.add(new MainMapFragment());
        fragments.add(new MainListFragment());
        fragments.add(new MainFilterFragment());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainMapFragmentContainer, fragments.get(0));
        transaction.add(R.id.mainListFragmentContainer, fragments.get(2));
        transaction.add(R.id.mainListFragmentContainer, fragments.get(1));
        transaction.commit();

        // TODO make this less obnoxious
        while (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        try {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // if we couldn't get a location, generate one with that'll have default coordinates, to avoid a null pointer exception
        if (location == null) {
            location = new Location(LocationManager.PASSIVE_PROVIDER);
        }

        // start receiving event updates
        ((EventsApplication)getApplication()).getEventsController().startListeners();
    }

    @Override
    public void onResume() {
        super.onResume();

        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // start receiving location updates
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this, magnetometer);
        sensorManager.unregisterListener(this, accelerometer);

        // stop receiving location updates
        locationManager.removeUpdates(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // stop receiving event updates
        ((EventsApplication)getApplication()).getEventsController().stopListeners();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == magnetometer) {
            magnetometerData = event.values.clone();
        } else {
            accelerometerData = event.values.clone();
        }
        if (accelerometerData != null && magnetometerData != null) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerData, magnetometerData);
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);
            // get orientation in x dimension
            this.rotation = (float)(Math.toDegrees(orientation[0]));

            // ((MainMapFragment)fragments.get(0)).updateMarkers();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void displayEventView(String eventID) {
        viewedEventID = eventID;
        ((MainMapFragment)fragments.get(0)).moveCameraToEvent(eventID);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_below, R.anim.shrink_and_fade_out, R.anim.grow_and_fade_in, R.anim.exit_to_below);
        transaction.replace(R.id.mainListFragmentContainer, new EventViewFragment());
        transaction.addToBackStack("displayEvent");
        transaction.commit();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // remove the old event being displayed in the main activity, since a new one is appearing
        getSupportFragmentManager().popBackStack("displayEvent", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        String s = intent.getStringExtra("EventID");
        if (s != null) {
            displayEventView(s);
        }
    }

    public Location getLocation() {
        return location;
    }

    public String getViewedEventID() {
        return viewedEventID;
    }

    public float getRotation() {
        return rotation;
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.d("MyDebug", "location updated: " + location.getLatitude() + ", " + location.getLongitude());
        this.location = location;
        ((EventsApplication)getApplication()).getEventsController().setUserLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }
}
