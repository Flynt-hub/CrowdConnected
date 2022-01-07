/*
 * Copyright (c) Visioglobe SAS. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.visioglobe.samples.mapviewsample.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.visioglobe.samples.mapviewsample.R;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener;
import com.visioglobe.visiomoveessential.models.VMELocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This fragment is a demo for VisioMove Essential's VMELocationInterface API.
 */
public class LocationDemoFragment extends android.support.v4.app.Fragment
{

    /**
     * The fragment's map view.
     */
    private VMEMapView mMapView;

    /**
     * The fragment's GPS toggling button.
     */
    private ToggleButton mGpsToggle;

    private ToggleButton mSimulateLocation;

    /**
     * The fragment's GPS activity.
     */
    private boolean mGpsActive;

    /**
     * The fragment's layout.
     */
    private ViewGroup mFragment;

    /**
     * The fragment's location manager.
     */
    private LocationManager mLocationManager;

    private Handler mHandler = new Handler();
    private List<Location> mFakeUserLocations = new ArrayList<>();
    private int mCurrentFakeUserLocationsIndex;

    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private Future mLocationSimulatorTaskFuture;


    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        if (mFragment == null) {
            mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // If permission to access location was denied, we null out the location manager to
                // prevent trying to use it
                mLocationManager = null;
                Toast.makeText(getContext(), "The application's Location permission must be enabled before the location services will work.", Toast.LENGTH_LONG).show();
            }

            // Inflate the fragment's layout
            mFragment = (ViewGroup) pInflater.inflate(R.layout.location_demo_fragment, pContainer, false);

            // Fetch the views
            mMapView = mFragment.findViewById(R.id.map_view);
            mGpsToggle = mFragment.findViewById(R.id.gps_toggle);
            mSimulateLocation = mFragment.findViewById(R.id.simulate_location_button);

            // Configure GPS toggle button.
            // Set disabled, it will be enabled when map view has loaded.
            mGpsToggle.setEnabled(false);
            mGpsActive = false;
            mGpsToggle.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if (mLocationManager != null) {
                        if (mGpsActive) {
                            // Stop GPS updates
                            mLocationManager.removeUpdates(mLocationListener);
                            mMapView.updateLocation(null);
                            mGpsActive = false;
                        }
                        else {
                            // Start GPS updates
                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.1f, mLocationListener);
                            mGpsActive = true;
                        }
                    }
                }
            });

            mSimulateLocation.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if(!mSimulateLocation.isChecked()){
                        mLocationSimulatorTaskFuture.cancel(true);
                        mExecutorService = Executors.newSingleThreadExecutor();
                        mHandler = new Handler();
                        VMELocation lLocation = mMapView.createLocationFromLocation(mFakeUserLocations.get(0));
                        mMapView.updateLocation(lLocation);
                    }else{
                        mCurrentFakeUserLocationsIndex = 0;
                        mLocationSimulatorTaskFuture = mExecutorService.submit(new Runnable()
                        {
                            @Override
                            public void run() {
                                simulateLocationCustom();
                            }

                        });
                    }
                }
            });
            mSimulateLocation.setEnabled(false);

            initFakeLocations();

            // Set up map listener to know when map view has loaded.
            mMapView.setLifeCycleListener(mLifeCycleListener);
            mMapView.loadMap();

        }
        return mFragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if (mGpsActive && mLocationManager != null) {
            // Stop GPS updates.
            mLocationManager.removeUpdates(mLocationListener);
            mMapView.updateLocation(null);
            mGpsActive = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (this.getContext() != null && mGpsActive && mLocationManager != null) {
            // Restart GPS updates.
            if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.1f, mLocationListener);
            mGpsActive = true;
        }
    }

    @Override
    public void onDestroyView(){
        mMapView.unloadMap();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if(mLocationSimulatorTaskFuture != null && mSimulateLocation.isChecked()){
            mLocationSimulatorTaskFuture.cancel(true);
        }
        super.onDestroyView();
    }

    // fix https://projects.visioglobe.com/issues/9183
    @Override
    public void onDestroy () {
        mFragment.removeAllViews();
        super.onDestroy();
    }

    private void simulateLocationCustom(){
        if (mSimulateLocation.isChecked() && mCurrentFakeUserLocationsIndex < mFakeUserLocations.size()) {
            Location lLocation = mFakeUserLocations.get(mCurrentFakeUserLocationsIndex);
            mLocationListener.onLocationChanged(lLocation);
            int mLocationUpdateDelayInMilliSecs = 2000;
            mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run() {
                    simulateLocationCustom();
                }
            }, mLocationUpdateDelayInMilliSecs);
            mCurrentFakeUserLocationsIndex++;
        }else{
            mLocationSimulatorTaskFuture.cancel(true);
        }
    }

    /**
     * init possible location list to fake user position
     */
    private void initFakeLocations() {
        mFakeUserLocations.add(getLocation(45.7414425, 4.8817110, 0.0, 0, 25));
        mFakeUserLocations.add(getLocation(45.7415096, 4.8813725, 0.0, 0, 20));
        mFakeUserLocations.add(getLocation(45.741124, 4.8812375, 0.0, 0, 15));
        mFakeUserLocations.add(getLocation(45.7408424, 4.8811657, 0.0, 0, 10));
        mFakeUserLocations.add(getLocation(45.7409480, 4.8805126, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.7406672, 4.8804190, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.7405301, 4.8804921, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.7405301, 4.8804921, 4.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.7405229, 4.8806677, 4.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.7407234, 4.8810041, 4.0, 0, 5));
    }

    private Location getLocation(double latitude, double longitude, double altitude, float bearing, float accuracy) {
        Location location = new Location("TEST");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(altitude);
        location.setBearing(bearing);
        location.setAccuracy(accuracy);
        return location;
    }

    /**
     * The location demo fragment's location listener that receives GPS updates.
     */
    private LocationListener mLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location) {
            // Forward location to the VMEMapView.
            VMELocation lVMELocation = mMapView.createLocationFromLocation(location);
            mMapView.updateLocation(lVMELocation);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    /**
     * The location demo fragment's life cycle listener that will be notified of map events.
     */
    private VMELifeCycleListener mLifeCycleListener = new VMELifeCycleListener()
    {
        @Override
        public void mapDidLoad(VMEMapView mapView) {
            mGpsToggle.setEnabled(true);
            mSimulateLocation.setEnabled(true);
        }
    };
}
