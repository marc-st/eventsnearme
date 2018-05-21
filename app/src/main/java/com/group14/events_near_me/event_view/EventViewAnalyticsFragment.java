package com.group14.events_near_me.event_view;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.group14.events_near_me.EventsApplication;
import com.group14.events_near_me.MainActivity;
import com.group14.events_near_me.R;
import com.group14.events_near_me.SignUp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Ben on 05/03/2018.
 */

public class EventViewAnalyticsFragment extends Fragment implements ChildEventListener{
    private int males = 0;
    private int females = 0;
    private int averageAge = 0;
    private String eventID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_view_analytics, null);

        // retrieve event id
        eventID = ((MainActivity) getActivity()).getViewedEventID();

        ((EventsApplication)getActivity().getApplication()).getFirebaseController()
                .getRoot().child("signups").orderByChild("eventID")
                .equalTo(eventID).addChildEventListener(this);

        return view;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        SignUp signUp = dataSnapshot.getValue(SignUp.class);
        ((EventsApplication)getActivity().getApplication()).getFirebaseController().getRoot().child("users/" + signUp.userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // update male and female count
                String gender = dataSnapshot.child("gender").getValue().toString();
                if (gender.equals("Male")) males++;
                else females++;

                // retrieve date of birth in milliseconds
                Object milliseconds = dataSnapshot.child("dateOfBirth").getValue();

                // convert mill time to Epoch time
                Calendar dateOfBirth = Calendar.getInstance();
                dateOfBirth.setTimeInMillis((long) milliseconds);

                // extract year
                int year = dateOfBirth.get(Calendar.YEAR);

                // update average
                averageAge = (averageAge + year) / (males + females);

                PieChart chart = getView().findViewById(R.id.chart);

                List<PieEntry> entries = new ArrayList<>();

                float m = (males/(males+females))*100;
                float f = (females/(males+females))*100;

                entries.add(new PieEntry(m, "Male"));
                entries.add(new PieEntry(f, "Female"));

                PieDataSet set = new PieDataSet(entries, "Gender representation");
                set.setColors(ColorTemplate.COLORFUL_COLORS);
                PieData data = new PieData(set);
                chart.setData(data);
                chart.invalidate(); // refresh

                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                ((TextView)getView().findViewById(R.id.averageAge)).setText
                        (String.format("People attending this event are around %d years old", currentYear-averageAge));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
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
}