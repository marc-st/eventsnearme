package com.group14.events_near_me;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.group14.events_near_me.event_view.EventViewFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity implements ChildEventListener {
    private HashMap<String, Event> events = new HashMap<>();
    private ArrayList<String> eventNames = new ArrayList<>();
    private String userID;
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // get what event to work with from the intent
        userID = getIntent().getStringExtra("UserID");

        // set the adapter for the list
        list = findViewById(R.id.profileEvents);
        list.setAdapter(new EventListAdapter(this, R.layout.events_list_line, eventNames,
                events, ((EventsApplication)getApplication()).getHandler()));

        // when an event is clicked get the ID of that event and pass it to the EventViewFragment
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String eventID = eventNames.get(i);
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("EventID", eventID);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        DatabaseReference reference = ((EventsApplication)getApplication()).getFirebaseController()
                .getDatabase().getReference("/users/" + userID);

        // create single fire listeners to get the user information
        reference.child("firstName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.profileFirstName)).setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MyDebug", "Error reading first name of " + userID);
            }
        });
        reference.child("surname").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.profileSurname)).setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MyDebug", "Error reading surname of " + userID);
            }
        });
        reference.child("gender").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.profileGender)).setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MyDebug", "Error reading gender of " + userID);
            }
        });
        reference.child("dateOfBirth").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long millis = dataSnapshot.getValue(Long.class);
                Long today = Calendar.getInstance().getTimeInMillis();
                // thats the number of milliseconds in a year
                Long age = (today - millis)/31556952000L;
                ((TextView)findViewById(R.id.profileAge)).setText(age.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MyDebug", "Error reading date of birth of " + userID);
            }
        });

        // add a listener to get the signups for the user
        ((EventsApplication)getApplication()).getFirebaseController()
                .getRoot().child("signups").orderByChild("userID")
                .equalTo(userID).addChildEventListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // remove listener
        ((EventsApplication)getApplication()).getFirebaseController().getRoot()
                .child("signups").orderByChild("userID")
                .equalTo(userID).removeEventListener(this);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        Log.d("MyDebug", "ProfileList: onChildAdded:" + dataSnapshot.getKey());

        SignUp signUp = dataSnapshot.getValue(SignUp.class);

        // for the sign up create a single fire listener to get event information
        ((EventsApplication)getApplication()).getFirebaseController()
                .getDatabase().getReference("/events/" + signUp.eventID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // add the new event to both the hashmap and its ID to the arraylist
                Event event = dataSnapshot.getValue(Event.class);
                events.put(dataSnapshot.getKey(), event);
                eventNames.add(dataSnapshot.getKey());

                // notify the list that a new item has been added

                ((EventListAdapter)list.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("MyDebug", "ProfileList: GetEvent: onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
        Log.d("MyDebug", "ProfileList: onChildChanged:" + dataSnapshot.getKey());
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d("MyDebug", "ProfileList: onChildRemoved:" + dataSnapshot.getKey());

        SignUp signUp = dataSnapshot.getValue(SignUp.class);

        // for the signup create a single fire listener to get event information
        ((EventsApplication)getApplication()).getFirebaseController()
                .getDatabase().getReference("/events/" + signUp.eventID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Event event = dataSnapshot.getValue(Event.class);
                        // find the event in the events hashmap and remove it
                        events.remove(dataSnapshot.getKey());
                        // find the event's ID in the eventNames and remove it
                        for (int x = 0; x < eventNames.size(); x++) {
                            if (eventNames.get(x).equals(dataSnapshot.getKey())) {
                                eventNames.remove(x);
                            }
                        }

                        // notify the list that the item has been removed

                        ((EventListAdapter)list.getAdapter()).notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("MyDebug", "ProfileList: GetEvent: onCancelled", databaseError.toException());
                    }
                });
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
        Log.d("MyDebug", "ProfileList: onChildMoved:" + dataSnapshot.getKey());
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w("MyDebug", "ProfileList: onCancelled", databaseError.toException());
    }
}
