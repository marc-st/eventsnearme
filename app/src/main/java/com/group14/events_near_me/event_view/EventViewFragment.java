package com.group14.events_near_me.event_view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.group14.events_near_me.MainActivity;
import com.group14.events_near_me.R;

import java.util.ArrayList;

/**
 * Created by Ben on 05/03/2018.
 *
 * this activity produces the viewpager to the 3 fragments that make up viewing an event
 */
public class EventViewFragment extends Fragment {
    private ArrayList<Fragment> fragments;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragments = new ArrayList<>();

        // add each of the three fragments to the adapter
        fragments.add(new EventViewDiscussionFragment());
        fragments.add(new EventViewAttendingFragment());
        fragments.add(new EventViewSignUpFragment());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_event_view, null);

        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                // this will always be 3 as there are 3 fragments
                return 3;
            }
        };

        ViewPager viewPager = view.findViewById(R.id.eventViewPager);
        viewPager.setAdapter(fragmentPagerAdapter);

        return view;
    }

    public void setSignedUp() {
        ((EventViewSignUpFragment)fragments.get(2)).setSignedUp();
    }
}