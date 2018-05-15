package com.group14.events_near_me;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Ben on 18/04/2018.
 */

public class InviteListAdapter extends ArrayAdapter<String> {
    private Context context;
    private int resourceId;
    private HashMap<String, User> users;
    private ArrayList<String> userNames;
    private ArrayList<String> clickedNames;

    public InviteListAdapter(Context context, int layoutResourceId, HashMap<String, User> users, ArrayList<String> userNames, ArrayList<String> clickedNames) {
        super(context, layoutResourceId, userNames);
        this.context = context;
        this.resourceId = layoutResourceId;
        this.users = users;
        this.userNames = userNames;
        this.clickedNames = clickedNames;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        final View row;

        if (convertView != null){
            row = convertView;
        }else {
            row = inflater.inflate(resourceId, parent, false);
        }

        String userID = userNames.get(position);

        User user = users.get(userID);
        ((TextView)row.findViewById(R.id.attendingName))
                .setText(user.firstName + ' ' + user.surname);

        // search clicked names for current user, if so change background colour
        if (!findFromList(userID, row)) {
            // if the item wasn't removed from the list, add it to the list
            row.setBackgroundColor(getContext().getResources().getColor(R.color.colorNotSelected));
        }

        return row;
    }

    private boolean findFromList(String userID, View row) {
        // return true if the item was removed, false if not
        Iterator<String> iterator = clickedNames.iterator();
        while(iterator.hasNext()) {
            String s = iterator.next();
            if (s.equals(userID)) {
                row.setBackgroundColor(getContext().getResources().getColor(R.color.colorSelected));
                return true;
            }
        }
        return false;
    }
}
