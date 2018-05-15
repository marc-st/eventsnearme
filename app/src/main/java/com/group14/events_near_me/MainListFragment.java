package com.group14.events_near_me;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.group14.events_near_me.event_view.EventViewFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ben on 04/03/2018.
 *
 * The activity which displays events in list form
 */

public class MainListFragment extends ListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new EventListAdapter(getContext(), R.layout.events_list_line,
                ((EventsApplication)getActivity().getApplication()).getEventsController().getEventNames(),
                ((EventsApplication)getActivity().getApplication()).getEventsController().getEvents(),
                ((EventsApplication)getActivity().getApplication()).getHandler()));

        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ((EventListAdapter)getListAdapter()).notifyDataSetChanged();
            }
        }, new IntentFilter("com.group14.events_near_me.EVENTS_UPDATE"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_list, null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        ((MainActivity)getActivity()).displayEventView(((EventsApplication)getActivity().getApplication())
                        .getEventsController().getEventNames().get(pos));
    }
}