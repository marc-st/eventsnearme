package com.group14.events_near_me.event_view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.group14.events_near_me.Comment;
import com.group14.events_near_me.EventsApplication;
import com.group14.events_near_me.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Ben on 04/03/2018.
 *
 * this adapter is used in the comments list of the event view activity.
 */

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private Context context;
    private ArrayList<Comment> comments;
    private int layoutResourceId;
    private EventsApplication app;

    public CommentListAdapter(Context context, int layoutResourceId, ArrayList<Comment> comments, EventsApplication app) {
        super(context, layoutResourceId, comments);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.comments = comments;
        this.app = app;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        View row;
        // if we're being given an existing row to repopulate use that, otherwise inflate a new row
        if (convertView != null) {
            row = convertView;
        } else {
            row = inflater.inflate(layoutResourceId, parent, false);
        }

        Comment comment = comments.get(position);

        // set comment text and body
        TextView textView = row.findViewById(R.id.discussionName);
        app.getFirebaseController().setTextViewToName(textView, comment.userID);

        ((TextView)row.findViewById(R.id.discussionText)).setText(comment.body);

        // convert time, stored in milliseconds, into a readable form
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm d/M/yy", Locale.UK);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(comment.timestamp);
        ((TextView)row.findViewById(R.id.discussionTimestamp)).setText(sdf.format(calendar.getTime()));

        return row;
    }
}