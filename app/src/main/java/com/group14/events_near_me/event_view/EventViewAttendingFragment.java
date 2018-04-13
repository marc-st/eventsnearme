package com.group14.events_near_me.event_view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.group14.events_near_me.EventsApplication;
import com.group14.events_near_me.MainActivity;
import com.group14.events_near_me.ProfileActivity;
import com.group14.events_near_me.R;
import com.group14.events_near_me.SignUp;

import java.util.ArrayList;

/**
 * Created by Ben on 05/03/2018.
 */

public class EventViewAttendingFragment extends ListFragment implements ChildEventListener {
    private ArrayList<SignUp> signUps = new ArrayList<>();
    private String eventID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieve event id
        eventID = ((MainActivity)getActivity()).getViewedEventID();

        // set list adapter for attending list
        setListAdapter(new AttendingListAdapter(getContext(), R.layout.event_attending_list_line, signUps, (EventsApplication)getActivity().getApplication()));

        ((EventsApplication)getActivity().getApplication()).getFirebaseController()
                .getRoot().child("signups").orderByChild("eventID")
                .equalTo(eventID).addChildEventListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ((EventsApplication)getActivity().getApplication()).getFirebaseController()
                .getRoot().child("signups").orderByChild("eventID")
                .equalTo(eventID).removeEventListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_view_attending, null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        Intent intent = new Intent(EventViewAttendingFragment.this.getActivity(), ProfileActivity.class);
        intent.putExtra("UserID", signUps.get(pos).userID);

        startActivity(intent);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        // add new sign up to list of sign ups then update listView
        SignUp signUp = dataSnapshot.getValue(SignUp.class);
        signUps.add(signUp);

        ((AttendingListAdapter)getListAdapter()).notifyDataSetChanged();

        if (signUp.userID.equals(((EventsApplication)getActivity().getApplication()).getFirebaseController().getCurrentUserId())) {
            ((EventViewFragment)getParentFragment()).setSignedUp();
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        SignUp signUp = dataSnapshot.getValue(SignUp.class);
        // find that sign up in the list and remove it
        for (int x = 0; x < signUps.size(); x++) {
            if (signUps.get(x).equals(signUp)) {
                signUps.remove(x);
            }
        }
        ((AttendingListAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
