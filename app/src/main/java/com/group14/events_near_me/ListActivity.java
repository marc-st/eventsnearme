package com.group14.events_near_me;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

/**
 * Created by Ben on 04/03/2018.
 */

public class ListActivity extends AppCompatActivity implements ChildEventListener, GestureDetector.OnGestureListener {
    private ArrayList<Event> events = new ArrayList<>();
    private ListView list;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);

        list = findViewById(R.id.eventListView);
        list.setAdapter(new EventListAdapter(this, R.layout.events_list_line, events));
        ((EventsApplication)getApplication()).getDatabase().getRoot().child("events").addChildEventListener(this);

        gestureDetector = new GestureDetector(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((EventsApplication)getApplication()).getDatabase().getRoot().child("events").removeEventListener(this);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        Log.d("MyDebug", "EventsList: onChildAdded:" + dataSnapshot.getKey());

        Event event = dataSnapshot.getValue(Event.class);
        events.add(event);
        list.invalidateViews();
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
        Log.d("MyDebug", "EventsList: onChildChanged:" + dataSnapshot.getKey());
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d("MyDebug", "onChildRemoved:" + dataSnapshot.getKey());

        Event event = dataSnapshot.getValue(Event.class);
        for (int x = 0; x < events.size(); x++) {
            if (events.get(x).equals(event)) {
                events.remove(x);
            }
        }
        list.invalidateViews();
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
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        if (motionEvent.getRawX() < motionEvent1.getRawX()) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
            finish();

            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return gestureDetector.onTouchEvent(e);
    }
}