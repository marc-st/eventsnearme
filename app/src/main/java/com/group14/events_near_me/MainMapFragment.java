package com.group14.events_near_me;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.group14.events_near_me.event_view.EventViewFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Ben on 26/02/2018.
 *
 * displays events on a map
 */

public class MainMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {
    private GoogleMap map;
    private boolean addingEvent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateMarkers();
            }
        }, new IntentFilter("com.group14.events_near_me.EVENTS_UPDATE"));
        addingEvent = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_map, null);

        // start the background task of getting a google map
        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        view.findViewById(R.id.mapAddFAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addingEvent) {
                    addingEvent = false;
                    Toast.makeText(getActivity().getApplicationContext(), "No longer adding an event", Toast.LENGTH_SHORT).show();
                } else {
                    addingEvent = true;
                    Toast.makeText(getActivity().getApplicationContext(), "Tap on the map where to add an event", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    public void updateMarkers() {
        if (map == null) {
            return;
        }
        map.clear();

        ArrayList<String> eventNames = ((EventsApplication)getActivity().getApplication()).getEventsController().getEventNames();
        HashMap<String, Event> events = ((EventsApplication)getActivity().getApplication()).getEventsController().getEvents();

        for (String s : eventNames) {
            Event e = events.get(s);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(e.lat, e.lng));
            markerOptions.title(s);

            if (e.endTime < Calendar.getInstance().getTimeInMillis()) {
                // event is expired
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else if (e.isPrivate) {
                // event is private
                if (e.startTime < Calendar.getInstance().getTimeInMillis()) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                }
            } else if (e.startTime < Calendar.getInstance().getTimeInMillis()) {
                // event is in progress
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            } else {
                // event hasn't happened yet
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }


            map.addMarker(markerOptions);
        }

        Location location = ((MainActivity)getActivity()).getLocation();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
        markerOptions.title("Your location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        try {
            markerOptions.rotation(((MainActivity) getActivity()).getRotation() + 180);
        } catch (NullPointerException e) {

        }
        map.addMarker(markerOptions);
    }

    public void moveCameraToEvent(String eventID) {
        if (map == null) {
            return;
        }

        Event event = ((EventsApplication)getActivity().getApplication()).getEventsController().getEvents().get(eventID);
        LatLng latLng = new LatLng(event.lat, event.lng);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);

        Location location = ((MainActivity)getActivity()).getLocation();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12));

        updateMarkers();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // when the user clicks on the map check if they're trying to add an event, if so launch event creation activity
        // TODO change this, it's just for testing
        Log.d("MyDebug", "clicked on map");
        if (addingEvent) {
            Intent intent = new Intent(getActivity(), AddEventActivity.class);
            intent.putExtra("lat", latLng.latitude);
            intent.putExtra("lng", latLng.longitude);
            startActivity(intent);
            addingEvent = false;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // search for if the marker is an event marker, indicated by its name being stored in eventnames
        // this check is done to ignore clicks on the user's position marker
        ArrayList<String> eventNames = ((EventsApplication)getActivity().getApplication()).getEventsController().getEventNames();
        for (String s : eventNames) {
            if (marker.getTitle().equals(s)) {
                ((MainActivity)getActivity()).displayEventView(marker.getTitle());
                addingEvent = false;
                return true;
            }
        }
        // if not perform don't consume the event, allowing default action to occur
        return false;
    }
}