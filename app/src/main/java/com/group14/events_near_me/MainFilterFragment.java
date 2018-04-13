package com.group14.events_near_me;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Ben on 12/03/2018.
 */
public class MainFilterFragment extends Fragment implements View.OnClickListener {
    private HashMap<String, Event> events;
    private ArrayList<String> eventNames;
    // true for sort by distance, false for sort by start time
    private boolean sortByDistance = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        events = ((MainActivity)getActivity()).getEvents();
        eventNames = ((MainActivity)getActivity()).getEventNames();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_filter, null);
        view.findViewById(R.id.MainFilterDistance).setOnClickListener(this);
        view.findViewById(R.id.MainFilterStart).setOnClickListener(this);
        return view;
    }

    public void sort() {
        final Location userLocation;
        try {
            userLocation = ((MainActivity) getActivity()).getLocation();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }

        // perform task of sorting the list in the background
        ((EventsApplication)getActivity().getApplication()).getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (sortByDistance) {
                    Collections.sort(eventNames, new Comparator<String>() {
                        @Override
                        public int compare(String s, String t1) {
                            Event eventFirst = events.get(s);
                            Event eventSecond = events.get(t1);

                            float[] resultFirst = new float[1];
                            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                                    eventFirst.lat, eventFirst.lng, resultFirst);
                            float[] resultSecond = new float[1];
                            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                                    eventSecond.lat, eventSecond.lng, resultSecond);
                            return (int)(resultFirst[0] - resultSecond[0]);
                        }
                    });
                } else {
                    Collections.sort(eventNames, new Comparator<String>() {
                        @Override
                        public int compare(String s, String t1) {
                            Event eventFirst = events.get(s);
                            Event eventSecond = events.get(t1);

                            return (int)(eventFirst.startTime - eventSecond.startTime);
                        }
                    });
                }

                // now that the list is sorted update the list display fragment
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity)getActivity()).updateList();
                    }
                });
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.MainFilterDistance:
                sortByDistance = true;
                sort();
                break;
            case R.id.MainFilterStart:
                sortByDistance = false;
                sort();
                break;
        }
    }
}
