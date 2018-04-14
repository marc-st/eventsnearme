package com.group14.events_near_me.event_view;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.group14.events_near_me.Event;
import com.group14.events_near_me.EventsApplication;
import com.group14.events_near_me.MainActivity;
import com.group14.events_near_me.R;
import com.group14.events_near_me.SignUp;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ben on 05/03/2018.
 */

public class EventViewSignUpFragment extends Fragment{

    private boolean isSignedUp = false;
    private String eventID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_view_sign_up, null);

        eventID = ((MainActivity)getActivity()).getViewedEventID();
        Event event = ((EventsApplication)getActivity().getApplication()).getEventsController().getEvents().get(eventID);

        if(event != null){

            ((TextView)view.findViewById(R.id.eventName)).setText(event.name != null ? event.name : "No Event Name");

            // declare the variables as final so they can be accessed by the background task
            final TextView eventLocation = view.findViewById(R.id.eventLocation);
            final Event e = event;

            // find the name of the event location as a background task
            ((EventsApplication)getActivity().getApplication()).getHandler().post(new Runnable() {
                @Override
                public void run() {
                    Geocoder geo = new Geocoder(getContext());
                    try {
                        List<Address> matches = geo.getFromLocation(e.lat, e.lng, 1);
                        Address address = (matches.isEmpty() ? null : matches.get(0));

                        eventLocation.setText(address.getAddressLine(0));
                    } catch (IOException | NullPointerException e1) {
                        eventLocation.setText(getString(R.string.location_missing));
                        e1.printStackTrace();
                    }
                }
            });


            // convert the times from milliseconds into a human readable form
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm d/M/yy", Locale.UK);
            Calendar calendar = Calendar.getInstance();

            // set the start time
            calendar.setTimeInMillis(event.startTime);
            ((TextView)view.findViewById(R.id.eventStart)).setText(sdf.format(calendar.getTime()));

            // set the end time
            calendar.setTimeInMillis(event.endTime);
            ((TextView)view.findViewById(R.id.eventEnd)).setText(sdf.format(calendar.getTime()));

            TextView textView = view.findViewById(R.id.eventOwner);
            ((EventsApplication)getActivity().getApplication())
                    .getFirebaseController().setTextViewToName(textView, event.ownerID);
        }

        if(isSignedUp) {
            view.findViewById(R.id.isAttending).setVisibility(View.VISIBLE);
            view.findViewById(R.id.signUp).setEnabled(false);
        }

        view.findViewById(R.id.signUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpToEvent();
            }
        });

        return view;
    }

    public void signUpToEvent(){

        // get reference to signups table
        DatabaseReference ref = ((EventsApplication)getActivity().getApplication()).getFirebaseController().getRoot().child("signups");

        String key = ref.push().getKey();

        // get properties to put into table
        String userID = ((EventsApplication)getActivity().getApplication()).getFirebaseController().getCurrentUserId();
        String eventID = ((MainActivity)getActivity()).getViewedEventID();

        long timestamp = Calendar.getInstance().getTimeInMillis();

        SignUp signup = new SignUp(eventID, userID, timestamp);
        ref.child(key).setValue(signup);

        isSignedUp = true;

    }

    public void setSignedUp() {
        this.isSignedUp = true;
        try {
            getView().findViewById(R.id.isAttending).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.signUp).setEnabled(false);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
