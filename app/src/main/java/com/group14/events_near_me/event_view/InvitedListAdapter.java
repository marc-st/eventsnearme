package com.group14.events_near_me.event_view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.group14.events_near_me.EventsApplication;
import com.group14.events_near_me.Invitation;
import com.group14.events_near_me.R;
import com.group14.events_near_me.SignUp;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Ben on 17/04/2018.
 */

public class InvitedListAdapter extends ArrayAdapter<Invitation> {

    private EventsApplication app;
    private Context context;
    private int resourceId;
    private ArrayList<Invitation> invitations;

    public InvitedListAdapter(Context context, int layoutResourceId, ArrayList<Invitation> invitations, EventsApplication app) {
        super(context, layoutResourceId, invitations);
        this.context = context;
        this.resourceId = layoutResourceId;
        this.invitations = invitations;
        this.app = app;
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

        Invitation invitation = invitations.get(position);
        TextView textView = row.findViewById(R.id.invitationName);
        app.getFirebaseController().setTextViewToName(textView, invitation.userID);
        ((TextView)row.findViewById(R.id.invitationAccepted))
                .setText(invitation.accepted ? app.getString(R.string.yes) : app.getString(R.string.no));

        return row;
    }
}
