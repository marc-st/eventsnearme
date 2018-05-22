package com.group14.events_near_me.event_view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.group14.events_near_me.Event;
import com.group14.events_near_me.EventsApplication;
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
        fragments.add(new EventViewSignUpFragment());
        fragments.add(new EventViewDiscussionFragment());
        fragments.add(new EventViewAnalyticsFragment());

        // if the event's private add an invited fragment otherwise an attending fragment
        Event event = ((EventsApplication)getActivity().getApplication())
                .getEventsController().getEvents().get(((MainActivity)getActivity()).getViewedEventID());
        if (event.isPrivate) {
            fragments.add(new EventViewInvitedFragment());
        } else {
            fragments.add(new EventViewAttendingFragment());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_view, null);

        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                // this will always be 4 as there are 4 fragments
                return 4;
            }
        };

        ViewPager viewPager = view.findViewById(R.id.eventViewPager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(fragmentPagerAdapter);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        for (Fragment f : fragments) {
            transaction.remove(f);
        }
        transaction.commit();
    }

    public void setSignedUp() {
        ((EventViewSignUpFragment)fragments.get(0)).setSignedUp();
    }

    public void setInvited(String invitationId, boolean accepted) {
        ((EventViewSignUpFragment)fragments.get(0)).setInvited(invitationId, accepted);
    }

}
