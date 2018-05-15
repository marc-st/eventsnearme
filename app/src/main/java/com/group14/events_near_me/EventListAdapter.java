package com.group14.events_near_me;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ben on 04/03/2018.
 *
 * this adapter is used for displaying all the events that a user can view in list form
 * It acts on a list of event ID strings, referring to a hashmap with the ID string to get the event class
 * This makes it simpler to reorder and filter what events are shown
 */

public class EventListAdapter extends ArrayAdapter<String> {
    private Context context;
    private HashMap<String, Event> events;
    private ArrayList<String> eventNames;
    private int layoutResourceId;
    private Handler handlerBackground;
    private Handler handlerUI;

    public EventListAdapter(Context context, int layoutResourceId, ArrayList<String> eventNames, HashMap<String, Event> events, Handler handler) {
        super(context, layoutResourceId, eventNames);
        this.context = context;
        this.events = events;
        this.eventNames = eventNames;
        this.layoutResourceId = layoutResourceId;
        this.handlerBackground = handler;
        handlerUI = new Handler(Looper.myLooper());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        View row;
        // if we're being given an existing row to repopulate use that, otherwise inflate a new row
        if (convertView != null) {
            row = convertView;
        } else {
            row = inflater.inflate(layoutResourceId, parent, false);
        }

        // get the event by querying the hashmap for the ID
        final Event e = events.get(eventNames.get(position));

        ((TextView)row.findViewById(R.id.eventListName)).setText(e.name);

        // convert the times from milliseconds into a human readable form
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm d/M/yy", Locale.UK);

        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(e.startTime);
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(e.endTime);

        StringBuilder sb = new StringBuilder();
        // if the start and end is on the same day, condense the time display
        if (start.get(Calendar.YEAR) == end.get(Calendar.YEAR) &&
                start.get(Calendar.MONTH) == end.get(Calendar.MONTH) &&
                start.get(Calendar.DAY_OF_MONTH) == end.get(Calendar.DAY_OF_MONTH)) {
            // display the start time of day only
            SimpleDateFormat sdfTimeOnly = new SimpleDateFormat("h:mm-", Locale.UK);
            sb.append(sdfTimeOnly.format(start.getTime()));
            // display full date for the end only
            sb.append(sdf.format(end.getTime()));
        } else {
            // display full date for both start and end
            sb.append(sdf.format(start.getTime()));
            sb.append(" to ");
            sb.append(sdf.format(end.getTime()));
        }
        ((TextView)row.findViewById(R.id.eventListTime)).setText(sb.toString());

        // declare the variables as final so they can be accessed by the background task
        final TextView eventLocation = row.findViewById(R.id.eventListLocation);

        // find the name of the event location as a background task
        handlerBackground.post(new Runnable() {
            @Override
            public void run() {
                Geocoder geo = new Geocoder(getContext());
                try {
                    List<Address> matches = geo.getFromLocation(e.lat, e.lng, 1);
                    final Address address = (matches.isEmpty() ? null : matches.get(0));

                    handlerUI.post(new Runnable() {
                        @Override
                        public void run() {
                            eventLocation.setText(address.getLocality());
                        }
                    });
                } catch (IOException | NullPointerException e1) {
                    handlerUI.post(new Runnable() {
                        @Override
                        public void run() {
                            eventLocation.setText(getContext().getString(R.string.location_missing));
                        }
                    });
                    e1.printStackTrace();
                }
            }
        });
        return row;
    }
}