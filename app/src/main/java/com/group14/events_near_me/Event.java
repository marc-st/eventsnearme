package com.group14.events_near_me;

/**
 * Created by Ben on 28/02/2018.
 *
 * data storage class for an event
 */

public class Event {
    public String name;
    public double lat;
    public double lng;
    public long startTime;
    public long endTime;
    public String ownerID;
    public boolean isPrivate;

    public Event() {

    }

    public Event(String name, double lat, double lng, long startTime, long endTime, String ownerID, boolean isPrivate) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ownerID = ownerID;
        this.isPrivate = isPrivate;
    }
}