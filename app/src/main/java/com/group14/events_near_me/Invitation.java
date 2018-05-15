package com.group14.events_near_me;

/**
 * Created by Ben on 16/04/2018.
 *
 * data storage class for an invitation
 */
public class Invitation {
    public String eventID;
    public String userID;
    public long timestamp;
    public boolean accepted;

    public Invitation() {

    }
    public Invitation(String eventID, String userID, long timestamp, boolean accepted) {
        this.eventID = eventID;
        this.userID = userID;
        this.timestamp = timestamp;
        this.accepted = accepted;
    }
}
