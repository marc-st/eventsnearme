package com.group14.events_near_me;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.Calendar;

/**
 * Created by Ben on 26/02/2018.
 *
 * the first activity a new user will see. It prompts them to authenticate with google,
 * once they do that ask for extra information that google doesn't supply
 */

public class SignInActivity extends AppCompatActivity implements View.OnClickListener, ChildEventListener {
    private GoogleSignInClient googleClient;
    private GoogleSignInAccount account;
    private String listenerToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isSignedIn = getIntent().getBooleanExtra("isAuthenticated", false);
        if (isSignedIn) {
            account = ((EventsApplication)getApplication()).getAccount();
            checkForExistingUser(account.getIdToken());

            // set the screen to display a splash screen while the user data is retrieved
            setContentView(R.layout.splash_screen);
        } else {
            setContentView(R.layout.activity_sign_in_eula);
            WebView wv = findViewById(R.id.eulaView);
            wv.loadUrl("file:///android_asset/EventManagerEULA.htm");
            findViewById(R.id.eulaButton).setOnClickListener(this);
        }


        googleClient = ((EventsApplication)getApplication()).getClient();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                // call the google client to display authentication screen
                Intent signIn = googleClient.getSignInIntent();
                // use request code 123 so we know when we get a response
                startActivityForResult(signIn, 123);
                break;
            case R.id.confirmDetails:
                // when they click to confirm details entry

                // get the ID of the currently selected radio button in the genderselectgroup,
                // then find the text of that button
                String gender = ((RadioButton)findViewById(((RadioGroup)findViewById(R.id.genderSelectGroup))
                        .getCheckedRadioButtonId())).getText().toString();

                DatePicker datePicker = findViewById(R.id.dobSelect);
                Calendar dateOfBirth = Calendar.getInstance();
                dateOfBirth.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                // create a new user based on the data they entered
                User user = new User(account.getGivenName(), account.getFamilyName(), gender, dateOfBirth.getTimeInMillis(), account.getIdToken());
                ((EventsApplication)this.getApplication()).getFirebaseController().addUser(user);

                // start looking for private events now that the user is signed in
                ((EventsApplication)getApplication()).getEventsController().notifyNameAvailable();

                // finish this activity bringing up the default (main)
                finish();
                break;
            case R.id.eulaButton:
                setContentView(R.layout.activity_sign_in);

                findViewById(R.id.sign_in_button).setOnClickListener(this);
                findViewById(R.id.confirmDetails).setOnClickListener(this);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if the request code is the one that symbolises a response to the google authentication request
        if (requestCode == 123) {
            // get the task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // try and get the google account
                account = task.getResult(ApiException.class);

                // the user is now authenticated so we can search for a user
                checkForExistingUser(account.getIdToken());
            } catch (ApiException e) {
                // the user maybe pressed cancel on the authenticate screen so don't change the UI so they can do it again
                e.printStackTrace();
            }
        }
    }

    private void checkForExistingUser(String token) {
        ((EventsApplication)getApplication()).getFirebaseController()
                .getRoot().child("users").orderByChild("googleAuthToken")
                .equalTo(token).addChildEventListener(this);
        listenerToken = token;

        // after a 2 second timeout period assume an account doesnt exist and show form to make one
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setupDetailsEntry();
            }
        }, 2000);
    }

    /**
     * The method enables extra personal details to be shared by the user. After
     * the Google authentication is complete, the invisible elements are made visible
     * which allows the user to select his age and gender. A confirm button to send
     * the data to FireBase is made visible as well. The Google sign-in button is
     * hidden and disabled
     */
    private void setupDetailsEntry() {
        setContentView(R.layout.activity_sign_in);
        findViewById(R.id.dobSelect).setVisibility(View.VISIBLE);
        findViewById(R.id.genderSelectGroup).setVisibility(View.VISIBLE);
        Button confirm = findViewById(R.id.confirmDetails);
        confirm.setVisibility(View.VISIBLE);
        confirm.setOnClickListener(this);

        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        // A user was found with our token
        Log.d("MyDebug", "SignInActivity: onChildAdded:" + dataSnapshot.getKey());
        ((EventsApplication)getApplication()).getFirebaseController().setCurrentUserId(dataSnapshot.getKey());

        // start looking for private events now that the user is signed in
        ((EventsApplication)getApplication()).getEventsController().notifyNameAvailable();

        ((EventsApplication)getApplication()).getFirebaseController()
                .getRoot().child("users").orderByChild("googleAuthToken")
                .equalTo(listenerToken).removeEventListener(this);

        // user found no need to input details go to default (main) activity
        finish();
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
        Exception e = databaseError.toException();
        e.printStackTrace();
        Log.d("MyDebug", "UserFindError" + databaseError.getDetails());
    }
}