package com.group14.events_near_me.event_view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.group14.events_near_me.Event;
import com.group14.events_near_me.EventsApplication;
import com.group14.events_near_me.InviteActivity;
import com.group14.events_near_me.MainActivity;
import com.group14.events_near_me.R;
import com.group14.events_near_me.SignUp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ben on 05/03/2018.
 */

public class EventViewSignUpFragment extends Fragment{
    private String eventID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_view_sign_up, null);

        eventID = ((MainActivity)getActivity()).getViewedEventID();
        Event event = ((EventsApplication)getActivity().getApplication()).getEventsController().getEvents().get(eventID);

        if (event.isPrivate) {
            ((Button)view.findViewById(R.id.signUpButton)).setText("Accept Invitation");
        } else {
            view.findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signUpToEvent();
                }
            });
        }

        if(event == null){
            ((TextView)view.findViewById(R.id.eventName)).setText("Error");
            return view;
        }

        ((TextView)view.findViewById(R.id.eventName)).setText(event.name != null ? event.name : "No Event Name");
        ((TextView)view.findViewById(R.id.eventPrivate))
                .setText(event.isPrivate ? getString(R.string.event_private) : getString(R.string.event_public));

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

        if (event.ownerID.equals(((EventsApplication)getActivity().getApplication())
                .getFirebaseController().getCurrentUserId())) {
            view.findViewById(R.id.signUpButton).setVisibility(View.GONE);
            view.findViewById(R.id.event_admin_buttons).setVisibility(View.VISIBLE);

            if (event.isPrivate) {
                view.findViewById(R.id.inviteButton).setEnabled(true);
            }
        }

        view.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Confirmation");
                alert.setMessage(R.string.confirm_delete);
                alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // if they click confirm delete the event
                        deleteEvent();
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // if they clicked cancel don't delete the event; do nothing
                    }
                });
                alert.show();
            }
        });

        view.findViewById(R.id.inviteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventViewSignUpFragment.this.getActivity(), InviteActivity.class);
                intent.putExtra("EventID", eventID);

                startActivity(intent);

            }
        });

        view.findViewById(R.id.eventDirections).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + e.lat + "," + e.lng));
                startActivity(intent);
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


    }

    public void setSignedUp() {
        try {
            getView().findViewById(R.id.isAttending).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.signUpButton).setEnabled(false);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setInvited(final String invitationID, boolean accepted) {
        if (accepted) {
            getView().findViewById(R.id.isAttending).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.signUpButton).setEnabled(false);
        } else {
            getView().findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((EventsApplication)getActivity().getApplication()).getFirebaseController()
                            .getRoot().child("invitations").child(invitationID).child("accepted").setValue(true);
                }
            });
        }
    }

    public void deleteEvent() {
        DatabaseReference ref = ((EventsApplication)getActivity().getApplication()).getFirebaseController().getRoot();
        // remove the event
        ref.child("events").child(eventID).removeValue();

        // remove either invitations or sign ups for the event
        ref.child(((EventsApplication)getActivity().getApplication())
                        .getEventsController().getEvents().get(eventID).isPrivate ? "invitations" : "signups")
                .orderByChild("eventID").equalTo(eventID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                dataSnapshot.getRef().removeValue();
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

            }
        });

        // go back to previous layout
        getActivity().getSupportFragmentManager().popBackStack();
    }
}
