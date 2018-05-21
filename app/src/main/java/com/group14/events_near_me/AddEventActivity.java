package com.group14.events_near_me;

import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class AddEventActivity extends AppCompatActivity implements View.OnClickListener {
    private double lat;
    private double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        lat = getIntent().getDoubleExtra("lat", 0.0d);
        lng = getIntent().getDoubleExtra("lng", 0.0d);

        final TextView eventLocation = findViewById(R.id.addEventLocation);

        // find the name of the event location as a background task
        ((EventsApplication)getApplication()).getHandler().post(new Runnable() {
            @Override
            public void run() {
                Geocoder geo = new Geocoder(AddEventActivity.this);
                try {
                    List<Address> matches = geo.getFromLocation(lat, lng, 1);
                    final Address address = (matches.isEmpty() ? null : matches.get(0));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            eventLocation.setText(address.getAddressLine(0));
                        }
                    });

                } catch (IOException | NullPointerException e1) {
                    e1.printStackTrace();
                }
            }
        });

        findViewById(R.id.addEventConfirm).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // if somehow we got an onclick for a different view than the submit button don't submit event
        if (view.getId() != R.id.addEventConfirm) {
            return;
        }

        FirebaseController fbc = ((EventsApplication)getApplication()).getFirebaseController();

        // find all the views to read input from
        TextView eventNameEntry = findViewById(R.id.addEventNameEntry);
        DatePicker startDatePicker = findViewById(R.id.addEventStartDate);
        TimePicker startTimePicker = findViewById(R.id.addEventStartTime);
        DatePicker endDatePicker = findViewById(R.id.addEventEndDate);
        TimePicker endTimePicker = findViewById(R.id.addEventEndTime);

        Event e = new Event();
        e.name = eventNameEntry.getText().toString();
        e.lat = lat;
        e.lng = lng;
        e.ownerID = fbc.getCurrentUserId();
        e.isPrivate = ((CheckBox)findViewById(R.id.addEventPrivate)).isChecked();

        // get the start time
        Calendar calendar = Calendar.getInstance();
        calendar.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth(),
                startTimePicker.getCurrentHour(), startTimePicker.getCurrentMinute());
        e.startTime = calendar.getTimeInMillis();

        // get the end time
        calendar.set(endDatePicker.getYear(), endDatePicker.getMonth(), endDatePicker.getDayOfMonth(),
                endTimePicker.getCurrentHour(), endTimePicker.getCurrentMinute());
        e.endTime = calendar.getTimeInMillis();

        // if the start time is after the end the event is invalid
        if (e.startTime - e.endTime > 0) {
            Toast.makeText(getApplicationContext(), "Event end must be after it starts", Toast.LENGTH_SHORT).show();
            return;
        }

        // create the event
        String key = fbc.getRoot().child("events").push().getKey();
        fbc.getRoot().child("events").child(key).setValue(e);

        // set the user as signed up to the event
        if (e.isPrivate) {
            // create an invitation
            Invitation invitation = new Invitation();
            invitation.userID = fbc.getCurrentUserId();
            invitation.timestamp = Calendar.getInstance().getTimeInMillis();
            invitation.eventID = key;
            invitation.accepted = true;

            // store it in firebase
            String key2 = fbc.getRoot().child("invitations").push().getKey();
            fbc.getRoot().child("invitations").child(key2).setValue(invitation);
        } else {
            // create a sign up
            SignUp signUp = new SignUp();
            signUp.userID = fbc.getCurrentUserId();
            signUp.timestamp = Calendar.getInstance().getTimeInMillis();
            signUp.eventID = key;

            // store it in firebase
            String key2 = fbc.getRoot().child("signups").push().getKey();
            fbc.getRoot().child("signups").child(key2).setValue(signUp);
        }
        finish();
    }
}
