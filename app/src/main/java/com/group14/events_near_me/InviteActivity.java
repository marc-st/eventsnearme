package com.group14.events_near_me;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class InviteActivity extends AppCompatActivity implements ChildEventListener {
    private boolean searchSurname;
    private String lastSearch;
    private ArrayList<String> clickedNames = new ArrayList<>();
    private HashMap<String, User> users = new HashMap<>();
    private ArrayList<String> userNames = new ArrayList<>();
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        list = findViewById(R.id.inviteList);
        list.setAdapter(new InviteListAdapter(this, R.layout.event_attending_list_line, users, userNames, clickedNames));

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String userID = userNames.get(i);
                if (!removeFromList(userID)) {
                    // if the item wasn't removed from the list, add it to the list
                    clickedNames.add(userID);
                }

                // redraw the list
                ((InviteListAdapter)list.getAdapter()).notifyDataSetChanged();
            }
        });

        // add on click listener for forename button
        findViewById(R.id.inviteForenameButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText commentInput = new EditText(InviteActivity.this);

                // create an alert prompting the user to enter a name
                AlertDialog.Builder alert = new AlertDialog.Builder(InviteActivity.this);
                alert.setTitle("Enter forename:");
                alert.setView(commentInput);
                alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // if they click confirm get what they entered and send it to searchName()
                        searchName(commentInput.getText().toString(), false);
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // if they clicked cancel don't search; do nothing
                    }
                });
                alert.show();
            }
        });

        // add on click listener for surname button
        findViewById(R.id.inviteSurnameButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText commentInput = new EditText(InviteActivity.this);

                // create an alert prompting the user to enter a name
                AlertDialog.Builder alert = new AlertDialog.Builder(InviteActivity.this);
                alert.setTitle("Enter surname:");
                alert.setView(commentInput);
                alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // if they click confirm get what they entered and send it to searchName()
                        searchName(commentInput.getText().toString(), true);
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // if they clicked cancel don't search; do nothing
                    }
                });
                alert.show();
            }
        });

        // add on click listener for confirm button
        findViewById(R.id.inviteConfirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(InviteActivity.this);
                alert.setTitle("Confirmation");
                alert.setMessage(getString(R.string.confirm_invite, clickedNames.size()));
                alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        confirmInvite();
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alert.show();

            }
        });
    }

    private void searchName(String name, boolean surname) {
        ((TextView)findViewById(R.id.inviteSearchText))
                .setText(String.format("Results for %s %s:", surname ? "surname" : "forename", name));

        // if this isn't the first search remove the old listener
        if (lastSearch != null) {
            ((EventsApplication)getApplication()).getFirebaseController().getRoot().child("users")
                    .orderByChild(searchSurname ? "surname" : "firstName").equalTo(lastSearch).removeEventListener(this);
        }

        users.clear();
        userNames.clear();

        ((InviteListAdapter)list.getAdapter()).notifyDataSetChanged();

        // query the database
        ((EventsApplication)getApplication()).getFirebaseController().getRoot().child("users")
                .orderByChild(surname ? "surname" : "firstName").equalTo(name).addChildEventListener(this);

        // store details on the previous search
        lastSearch = name;
        searchSurname = surname;
    }

    private void confirmInvite() {
        String eventID = getIntent().getStringExtra("EventID");
        FirebaseController fbc = ((EventsApplication)getApplication()).getFirebaseController();
        for (String userID : clickedNames) {
            Invitation invitation = new Invitation();
            invitation.userID = userID;
            invitation.timestamp = Calendar.getInstance().getTimeInMillis();
            invitation.eventID = eventID;
            invitation.accepted = false;

            // store it in firebase
            String key2 = fbc.getRoot().child("invitations").push().getKey();
            fbc.getRoot().child("invitations").child(key2).setValue(invitation);

        }

        finish();
    }

    private boolean removeFromList(String userID) {
        // return true if the item was removed, false if not
        Iterator<String> iterator = clickedNames.iterator();
        while(iterator.hasNext()) {
            String s = iterator.next();
            if (s.equals(userID)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        users.put(dataSnapshot.getKey(), dataSnapshot.getValue(User.class));
        userNames.add(dataSnapshot.getKey());

        ((InviteListAdapter)list.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();
        users.remove(key);

        Iterator<String> iterator = userNames.iterator();
        while(iterator.hasNext()) {
            String s = iterator.next();
            if (s.equals(key)) {
                iterator.remove();
                break;
            }
        }

        ((InviteListAdapter)list.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w("MyDebug", "InviteList: onCancelled", databaseError.toException());
    }
}
