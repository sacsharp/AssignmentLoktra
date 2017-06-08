package com.phunnylabs.assignmentloktra.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.phunnylabs.assignmentloktra.R;
import com.phunnylabs.assignmentloktra.adapter.PastTripsAdapter;
import com.phunnylabs.assignmentloktra.models.LocationItem;
import com.phunnylabs.assignmentloktra.models.Trip;
import com.phunnylabs.assignmentloktra.utilities.UtilityClass;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener, View.OnClickListener {

    private static final int PERMISSIONS_ACCESS_LOCATION = 1;
    private static final String TAG = "MapsActivity";
    private static final int REQUEST_LOCATION = 2;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    RealmList<LocationItem> locationItems = new RealmList<>();
    ArrayList<LatLng> locationItemsForPath = new ArrayList<>();

    private Polyline mPolyline;
    private ImageView imageViewStartEndTrip;

    private boolean mIsTripGoingOn = false;
    private boolean mMapConnected = false;
    private Realm mRealm;
    private ImageView imageViewPastTrips;

    private BottomSheetBehavior mBottomSheetBehavior;
    private PastTripsAdapter mPastTripsAdapter;
    private double tripDistance = 0;
    private TextView mTextViewTripTime;
    private int tripTime;
    private Timer mTripTimer;
    private ListView mBottomSheetListView;
    private SupportMapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mRealm = Realm.getDefaultInstance();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        setViews();
    }


    public void setViews() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mBottomSheetListView = (ListView) findViewById(R.id.listViewTrips);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetListView);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        imageViewPastTrips = (ImageView) findViewById(R.id.imageViewShowPastTrips);
        imageViewPastTrips.setOnClickListener(this);

        imageViewStartEndTrip = (ImageView) findViewById(R.id.imageViewStartEndTrip);
        imageViewStartEndTrip.setOnClickListener(this);

        mTextViewTripTime = (TextView) findViewById(R.id.textViewTripTime);
        mTextViewTripTime.setVisibility(View.GONE);
    }


    public void startTimer() {
        mTripTimer = new Timer();
        mTripTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    mTextViewTripTime.setText("Trip Time = " + tripTime + "s");
                    tripTime++;
                });
            }
        }, 1000, 1000);
    }

    private void showPathOnMap(Trip trip) {
        if (mPolyline != null) {
            mPolyline.remove();
        }

        mPolyline = mMap.addPolyline(new PolylineOptions());

        RealmList<LocationItem> locationItems = trip.getLocationItems();
        locationItemsForPath = new ArrayList<>();
        for (int i = 0; i < locationItems.size(); i++) {
            locationItemsForPath.add(new LatLng(locationItems.get(i).getLatitude(), locationItems.get(i).getLongitude()));
        }
        mPolyline.setPoints(locationItemsForPath);

        LatLng startPoint = new LatLng(locationItems.first().getLatitude(), locationItems.first().getLongitude());
        mMap.addMarker(new MarkerOptions().position(startPoint).icon(BitmapDescriptorFactory.fromResource(R.drawable.start_trip))
                .title("Start Point"));

        LatLng endPoint = new LatLng(locationItems.last().getLatitude(), locationItems.last().getLongitude());
        mMap.addMarker(new MarkerOptions().position(endPoint).icon(BitmapDescriptorFactory.fromResource(R.drawable.end_trip))
                .title("End Point"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(startPoint));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkForPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (mMap != null) {
                            mMap.setMyLocationEnabled(true);
                        }
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    alertAlert("These permissions are required to use the app. Would you like to grant the permissions?");
                }
            }
        }
    }

    private void alertAlert(String msg) {
        new AlertDialog.Builder(MapsActivity.this)
                .setTitle("Permission Request")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> checkForPermissions())
                .setNegativeButton(android.R.string.no, (dialogInterface, i) -> finish())
                .show();
    }

    private void checkForPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
                // For example if the user has previously denied the permission.
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Permission Request")
                        .setMessage("Please give permissions if you want to continue.")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            //re-request
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSIONS_ACCESS_LOCATION);
                        })
                        .setNegativeButton(android.R.string.no, (dialogInterface, i) -> finish())
                        .show();
            } else {
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Permission Request")
                        .setMessage("Please Give Permissions from the Settings App.")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> dialog.dismiss())
                        .setNegativeButton(android.R.string.no, (dialogInterface, i) -> finish())
                        .show();
            }
        } else {
            if (mMap != null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForPermissions();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mMapConnected = true;
    }

    protected void createLocationRequest() {
        mRequestingLocationUpdates = true;
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //restart location updates with the new interval


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> mLocationResult = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        mLocationResult.setResultCallback(result -> {
            final Status status = result.getStatus();
            //final LocationSettingsStates state = result.getLocationSettingsStates();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    //...
                    requestLocationUpdates();

                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // LocationItem settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                                MapsActivity.this,
                                REQUEST_LOCATION);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // LocationItem settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    //...
                    break;
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkForPermissions();
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult()", Integer.toString(resultCode));

        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_LOCATION:
                mRequestingLocationUpdates = false;
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        // All required changes were successfully made
                        requestLocationUpdates();
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(MapsActivity.this, "Please enable location.", Toast.LENGTH_LONG).show();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: " + location);

        LocationItem locationItem = new LocationItem();
        locationItem.setCurrentTime(System.currentTimeMillis());
        locationItem.setLatitude(location.getLatitude());
        locationItem.setLongitude(location.getLongitude());

        //get last element if there are items
        if (locationItems.size() > 0) {
            LocationItem start = locationItems.last();

            double distance = UtilityClass.distance(start.getLatitude(), start.getLongitude(), locationItem.getLatitude(), locationItem.getLongitude()) * 1000;
            tripDistance += distance;
        }

        locationItems.add(locationItem);

        locationItemsForPath.add(new LatLng(location.getLatitude(), location.getLongitude()));
        if (mMap != null) {
            if (locationItems.size() > 0) {
                Toast.makeText(this, "updating path " + location, Toast.LENGTH_SHORT).show();
                mPolyline.setPoints(locationItemsForPath);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageViewShowPastTrips:
                RealmResults<Trip> realmResults = mRealm.where(Trip.class).findAll();
                if (realmResults.size() > 0) {
                    if (mPastTripsAdapter == null) {
                        mPastTripsAdapter = new PastTripsAdapter(MapsActivity.this, realmResults);
                        mBottomSheetListView.setAdapter(mPastTripsAdapter);
                    } else {
                        mPastTripsAdapter.notifyDataSetChanged();
                    }

                    mBottomSheetListView.setOnItemClickListener((adapterView, view1, i, l) -> {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        showPathOnMap(realmResults.get(i));
                    });
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    Toast.makeText(this, "No Trips.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.imageViewStartEndTrip:
                if (mIsTripGoingOn) {
                    //end trip
                    mIsTripGoingOn = false;
                    imageViewStartEndTrip.setImageResource(R.drawable.start);

                    //stop location updates
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

                    //stop Timer
                    if (mTripTimer != null) {
                        mTripTimer.cancel();
                    }

                    //add this as a trip if distance is greater than 0.
                    mRealm.executeTransaction(realm -> {
                        if (locationItems.size() > 0 && tripDistance > 0) {
                            Number currentIdNum = realm.where(Trip.class).max("tripId");
                            int nextId;
                            if (currentIdNum == null) {
                                nextId = 1;
                            } else {
                                nextId = currentIdNum.intValue() + 1;
                            }

                            Trip trip = new Trip();
                            trip.setTripId(nextId);
                            trip.setLocationItems(locationItems);
                            trip.setTripDistance(tripDistance);
                            trip.setTripTime(tripTime);
                            mRealm.copyToRealmOrUpdate(trip);

                            locationItems.clear();
                            locationItemsForPath.clear();
                        }
                    });
                    mTextViewTripTime.setVisibility(View.GONE);
                } else {
                    //start trip
                    mIsTripGoingOn = true;
                    tripDistance = 0;
                    tripTime = 0;
                    mTextViewTripTime.setVisibility(View.VISIBLE);
                    imageViewStartEndTrip.setImageResource(R.drawable.stop);

                    //clear any polyline
                    if (mPolyline != null) {
                        mPolyline.remove();
                    }

                    if (mMapConnected) {
                        if (!mRequestingLocationUpdates) {
                            mPolyline = mMap.addPolyline(new PolylineOptions());
                            createLocationRequest();
                            startTimer();
                        }
                    } else {
                        mMapFragment.getMapAsync(this);
                    }
                }
                break;
        }
    }
}
