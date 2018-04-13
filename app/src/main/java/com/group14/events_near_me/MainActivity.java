package com.group14.events_near_me;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.group14.events_near_me.event_view.EventViewFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends FragmentActivity implements ChildEventListener, LocationListener {
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private HashMap<String, Event> events = new HashMap<>();
    private ArrayList<String> eventNames = new ArrayList<>();
    private Location location;
    private LocationManager locationManager;
    private String viewedEventID;

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

        ((EventsApplication)getApplication()).getFirebaseController().getRoot().child("events").addChildEventListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    public void updateFragments() {
        ((MainFilterFragment)fragments.get(2)).sort();
        ((MainMapFragment)fragments.get(0)).updateMarkers();
    }

    public void updateList() {
        ((MainListFragment)fragments.get(1)).updateList();
    }

    public void displayEventView(String eventID) {
        viewedEventID = eventID;
        ((MainMapFragment)fragments.get(0)).moveCameraToEvent(eventID);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainListFragmentContainer, new EventViewFragment());
        transaction.addToBackStack("displayEvent");
        transaction.commit();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // remove the old event being displayed in the main activity, since a new one is appearing
        getSupportFragmentManager().popBackStack("displayEvent", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        displayEventView(intent.getStringExtra("EventID"));
    }

    public HashMap<String, Event> getEvents() {
        return events;
    }

    public ArrayList<String> getEventNames() {
        return eventNames;
    }

    public Location getLocation() {
        return location;
    }

    public String getViewedEventID() {
        return viewedEventID;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        Log.d("MyDebug", "EventsList: onChildAdded:" + dataSnapshot.getKey());

        // add the new event to both the hashmap and its ID to the arraylist
        Event event = dataSnapshot.getValue(Event.class);
        events.put(dataSnapshot.getKey(), event);
        eventNames.add(dataSnapshot.getKey());
        // force the list to redraw itself with the new event
        updateFragments();
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
        Log.d("MyDebug", "EventsList: onChildChanged:" + dataSnapshot.getKey());
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d("MyDebug", "onChildRemoved:" + dataSnapshot.getKey());

        Event event = dataSnapshot.getValue(Event.class);
        // find the event in the events hashmap and remove it
        events.remove(dataSnapshot.getKey());
        // find the event's ID in the eventNames and remove it
        for (int x = 0; x < eventNames.size(); x++) {
            if (eventNames.get(x).equals(dataSnapshot.getKey())) {
                eventNames.remove(x);
            }
        }
        // redraw the list without the event
        updateFragments();
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
        Log.d("MyDebug", "EventsList: onChildMoved:" + dataSnapshot.getKey());
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w("MyDebug", "postComments:onCancelled", databaseError.toException());
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.d("MyDebug", "location updated: " + location.getLatitude() + ", " + location.getLongitude());
        this.location = location;
        updateFragments();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }
}
