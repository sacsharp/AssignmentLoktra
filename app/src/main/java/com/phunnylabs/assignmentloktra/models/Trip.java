package com.phunnylabs.assignmentloktra.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by sachin on 06/06/17.
 */

public class Trip extends RealmObject {

    @PrimaryKey
    private int tripId;
    private double tripDistance;
    private long tripTime;
    private RealmList<LocationItem> locationItems;

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public RealmList<LocationItem> getLocationItems() {
        return locationItems;
    }

    public void setLocationItems(RealmList<LocationItem> locationItems) {
        this.locationItems = locationItems;
    }

    public double getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(double tripDistance) {
        this.tripDistance = tripDistance;
    }

    public long getTripTime() {
        return tripTime;
    }

    public void setTripTime(long tripTime) {
        this.tripTime = tripTime;
    }
}
