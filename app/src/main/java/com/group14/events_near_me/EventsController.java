package com.group14.events_near_me;

import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Ben on 14/04/2018.
 * a class for reading event information from firebase and storing it in a hashmap for the app to use
 */
public class EventsController implements ChildEventListener {
    EventsApplication app;
    FirebaseController fbc;
    private HashMap<String, Event> events = new HashMap<>();
    private ArrayList<String> eventNames = new ArrayList<>();
    private ArrayList<String> privateEventNames = new ArrayList<>();
    private Location userLocation;
    private boolean sortByDistance = true;
    private InvitationListener invitationListener;
    private boolean isListening = false;

    public EventsController(EventsApplication app) {
        this.app = app;
        fbc = app.getFirebaseController();
    }

    public void startListeners() {
        // get all public events
        fbc.getRoot().child("events").orderByChild("isPrivate").equalTo(false).addChildEventListener(this);

        // get private events that the user is allowed to see
        // when the app first starts this is called before the userID is available, so a null check is needed
        String userID = fbc.getCurrentUserId();
        if (userID != null && !isListening) {
            invitationListener = new InvitationListener();
            fbc.getRoot().child("invitations").orderByChild("userID").equalTo(fbc.getCurrentUserId()).addChildEventListener(invitationListener);
            isListening = true;
        }
    }

    public void stopListeners() {
        // stop listener for public events
        fbc.getRoot().child("events")
                .orderByChild("isPrivate").equalTo(false).removeEventListener(this);

        // stop listener for private events
        for (String s : privateEventNames) {
            fbc.getRoot().child("events").child(s).removeEventListener(invitationListener);
        }
    }

    public void sort() {
        if (userLocation == null) {
            return;
        }

        // perform task of sorting the list in the background
        app.getHandler().post(new Runnable() {
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

                // now that the list is sorted update displays
                Intent intent = new Intent();
                intent.setAction("com.group14.events_near_me.EVENTS_UPDATE");
                app.sendBroadcast(intent);
            }
        });

    }

    public void setUserLocation(Location location) {
        userLocation = location;
        sort();
    }

    public void setSortByDistance(boolean sortByDistance) {
        this.sortByDistance = sortByDistance;
        sort();
    }

    public void notifyNameAvailable() {
        if (!isListening) {
            invitationListener = new InvitationListener();
            fbc.getRoot().child("invitations").orderByChild("userID").equalTo(fbc.getCurrentUserId()).addChildEventListener(invitationListener);
            isListening = true;
        }
    }

    public HashMap<String, Event> getEvents() {
        return events;
    }

    public ArrayList<String> getEventNames() {
        return eventNames;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        Log.d("MyDebug", "EventsList: onChildAdded:" + dataSnapshot.getKey());

        // add the new event to both the hashmap and its ID to the arraylist
        Event event = dataSnapshot.getValue(Event.class);
        events.put(dataSnapshot.getKey(), event);
        eventNames.add(dataSnapshot.getKey());

        // broadcast that the data set has changed
        Intent intent = new Intent();
        intent.setAction("com.group14.events_near_me.EVENTS_UPDATE");
        app.sendBroadcast(intent);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, final String previousChildName) {
        Log.d("MyDebug", "EventsList: onChildChanged:" + dataSnapshot.getKey());

        // remove the old event
        events.remove(previousChildName);
        Iterator<String> iterator = eventNames.iterator();
        while(iterator.hasNext()) {
            String s = iterator.next();
            if (s.equals(previousChildName)) {
                iterator.remove();
                break;
            }
        }

        // add the new event to both the hashmap and its ID to the arraylist
        Event event = dataSnapshot.getValue(Event.class);
        events.put(dataSnapshot.getKey(), event);
        eventNames.add(dataSnapshot.getKey());

        // broadcast that the data set has changed
        Intent intent = new Intent();
        intent.setAction("com.group14.events_near_me.EVENTS_UPDATE");
        app.sendBroadcast(intent);
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

        // broadcast that the data set has changed
        Intent intent = new Intent();
        intent.setAction("com.group14.events_near_me.EVENTS_UPDATE");
        app.sendBroadcast(intent);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
        Log.d("MyDebug", "EventsList: onChildMoved:" + dataSnapshot.getKey());
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w("MyDebug", "postComments:onCancelled", databaseError.toException());
    }

    private class InvitationListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d("MyDebug", "InvitationsList: onChildAdded:" + dataSnapshot.getKey());
            Invitation invitation = dataSnapshot.getValue(Invitation.class);
            privateEventNames.add(invitation.eventID);
            fbc.getRoot().child("events").orderByKey().equalTo(invitation.eventID).addChildEventListener(EventsController.this);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w("MyDebug", "InvitationListener:onCancelled", databaseError.toException());
        }
    }
}
