package com.group14.events_near_me;

/**
 * Created by Ben on 07/03/2018.
 *
 * data storage class for a sign up
 */

public class SignUp {
    public String eventID;
    public String userID;
    public long timestamp;

    public SignUp() {

    }
    public SignUp(String eventID, String userID, long timestamp) {
        this.eventID = eventID;
        this.userID = userID;
        this.timestamp = timestamp;
    }
}