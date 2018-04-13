package com.group14.events_near_me;

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
    private HashMap<String, Event> events;
    private ArrayList<String> eventNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        events = ((MainActivity)getActivity()).getEvents();
        eventNames = ((MainActivity)getActivity()).getEventNames();

        setListAdapter(new EventListAdapter(getContext(), R.layout.events_list_line, eventNames,
                events, ((EventsApplication)getActivity().getApplication()).getHandler()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_list, null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        ((MainActivity)getActivity()).displayEventView(eventNames.get(pos));
    }

    public void updateList() {
        ((EventListAdapter)getListAdapter()).notifyDataSetChanged();
    }
}