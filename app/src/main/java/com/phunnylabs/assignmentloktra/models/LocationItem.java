package com.phunnylabs.assignmentloktra.models;

import io.realm.RealmObject;

/**
 * Created by sachin on 06/06/17.
 */

public class LocationItem extends RealmObject {
    private double latitude;
    private double longitude;
    private long currentTime;


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }
}
