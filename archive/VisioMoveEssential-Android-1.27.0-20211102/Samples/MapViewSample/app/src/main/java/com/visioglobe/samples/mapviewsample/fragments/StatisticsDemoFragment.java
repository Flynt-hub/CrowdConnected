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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.visioglobe.samples.mapviewsample.R;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.enums.VMEPlaceAltitudeMode;
import com.visioglobe.visiomoveessential.enums.VMEPlaceAnchorMode;
import com.visioglobe.visiomoveessential.enums.VMEPlaceDisplayMode;
import com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener;
import com.visioglobe.visiomoveessential.listeners.VMEMapListener;
import com.visioglobe.visiomoveessential.models.VMECameraUpdate;
import com.visioglobe.visiomoveessential.models.VMECameraUpdateBuilder;
import com.visioglobe.visiomoveessential.models.VMELocation;
import com.visioglobe.visiomoveessential.models.VMEPlaceOrientation;
import com.visioglobe.visiomoveessential.models.VMEPlaceSize;
import com.visioglobe.visiomoveessential.models.VMEPlaceVisibilityRamp;
import com.visioglobe.visiomoveessential.models.VMEPosition;
import com.visioglobe.visiomoveessential.models.VMESceneContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * This fragment is a demo for VisioMove Essential's VMELocationInterface API.
 */
public class StatisticsDemoFragment extends android.support.v4.app.Fragment
{

    /**
     * The fragment's map view.
     */
    private VMEMapView mMapView;

    /**
     * The fragment's GPS toggling button.
     */
    private ToggleButton mGpsToggle;

    /* cat tracker */
    private ToggleButton mPlaceToggle;

    private Button mSimulateLocation;

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
    private int mLocationUpdateDelayInMilliSecs = 2000;
    private int mCurrentFakeUserLocationsIndex;

    private static float kLonMoverArr[] = {0.0002f, 0.0f, -0.0002f, 0.0002f, 0.0f, -0.0002f};
    private static float kLatMoverArr[] = {0.0002f, 0.0004f, 0.0002f, -0.0002f, -0.0004f, -0.0002f};
    private static int mCatMoverIndex = 0;
    private Runnable mCatMoverRunnable = null;
    private Handler mDelayedCatMoverHandler = null;

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
            mFragment = (ViewGroup) pInflater.inflate(R.layout.statistics_demo_fragment, pContainer, false);

            // Fetch the views
            mMapView = mFragment.findViewById(R.id.map_view);
            mGpsToggle = mFragment.findViewById(R.id.gps_toggle);
            mPlaceToggle = mFragment.findViewById(R.id.place_toggle);
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
                            try {
                                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.1f, mLocationListener);
                            }
                            catch (SecurityException e) {
                                e.printStackTrace();
                            }
                            mGpsActive = true;
                        }
                    }
                }
            });

            mSimulateLocation.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    mCurrentFakeUserLocationsIndex = 0;
                    mLocationSimulator.run();
                }
            });
            mSimulateLocation.setEnabled(false);

            initFakeLocations();

            // Set up map listener to know when map view has loaded.
            mMapView.setLifeCycleListener(mLifeCycleListener);
            mMapView.setMapListener(mMapListener);
            // Load map.
            mMapView.loadMap();
            mMapView.setStatisticsLogCamera(true);
            mMapView.setStatisticsLogLocation(true);
            mMapView.setStatisticsLogInterest(true);
            mMapView.setStatisticsLog(true);

            mPlaceToggle = (ToggleButton) mFragment.findViewById(R.id.place_toggle);
            // Configure Color toggle button.
            // Set disabled, it will be enabled when map view has loaded.
            mPlaceToggle.setEnabled(false);
            mDelayedCatMoverHandler = new Handler();

            mPlaceToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String lCatOnline = "catOnline";

                    if (isChecked) {

                        List<VMEPosition> lPositions = new ArrayList<>();

                        // lCatOnline
                        {
                            Uri lIconUri = Uri.parse("https://i.pinimg.com/originals/5c/b9/18/5cb918952f184853961ea83597082f38.png");

                            JSONObject lPlaceData = null;
                            try {
                                lPlaceData = new JSONObject();
                                lPlaceData.put("name", "Cat - Online");
                                lPlaceData.put("description", "https://en.wikipedia.org/wiki/Cat");
                                lPlaceData.put("icon", lIconUri.toString());

                                JSONArray list = new JSONArray();
                                list.put("2");
                                list.put("3");
                                list.put("99");

                                lPlaceData.put("categories", list);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }

                            VMEPosition lPos = new VMEPosition(45.74140, 4.88349, 0.0, new VMESceneContext());
                            lPositions.add(lPos);

                            mMapView.addPlace(lCatOnline,
                                    lIconUri,
                                    lPlaceData,
                                    lPos,
                                    new VMEPlaceSize(1.0f),
                                    VMEPlaceAnchorMode.BOTTOM_CENTER,
                                    VMEPlaceAltitudeMode.RELATIVE,
                                    VMEPlaceDisplayMode.OVERLAY,
                                    VMEPlaceOrientation.newPlaceOrientationFacing(),
                                    new VMEPlaceVisibilityRamp());

                            mMapView.setPlaceSize(lCatOnline, new VMEPlaceSize(20.0f), true);

                            if (mCatMoverRunnable == null) {
                                List<String> trackedPlaces = Arrays.asList(lCatOnline);
                                mMapView.setStatisticsTrackedPlaceIDs(trackedPlaces);
                                mMapView.setStatisticsLog(true);
                                mCatMoverRunnable = new Runnable()
                                {
                                    public void run() {
                                        moveCat();
                                    }
                                };
                                mDelayedCatMoverHandler.postDelayed(mCatMoverRunnable, 2000);
                            }

                        }
                        int lPadding = 50;
                        VMECameraUpdate lCameraUpdate = new VMECameraUpdateBuilder()
                                .setTargets(lPositions)
                                .setPaddingTop(lPadding)
                                .setPaddingBottom(lPadding)
                                .setPaddingLeft(lPadding)
                                .setPaddingRight(lPadding)
                                .build();
                        mMapView.animateCamera(lCameraUpdate);
                    }
                    else {
                        mDelayedCatMoverHandler.removeCallbacks(mCatMoverRunnable);
                        mCatMoverRunnable = null;
                        mMapView.removePlace(lCatOnline);
                        mMapView.setStatisticsTrackedPlaceIDs(new ArrayList<String>());
                    }
                }
            });

        }
        return mFragment;
    }

    private void moveCat() {
        String lCatOnline = "catOnline";

        mCatMoverIndex++;
        if (mCatMoverIndex > 5) {
            mCatMoverIndex = 0;
        }
        Random r = new Random();
        //int i1 = r.nextInt(45 - 28) + 28;
        VMEPosition lPos = new VMEPosition(45.74140 + kLatMoverArr[mCatMoverIndex] + (r.nextInt(1000) - 500.0) / 1000000.0
                , 4.88349 + kLonMoverArr[mCatMoverIndex] + (r.nextInt(1000) - 500.0) / 1000000.0,
                0.0, new VMESceneContext());

        mMapView.setPlacePosition(lCatOnline, lPos, true);

        mDelayedCatMoverHandler.postDelayed(mCatMoverRunnable, 2000);
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
        if (mGpsActive && mLocationManager != null) {
            // Restart GPS updates.
            try {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.1f, mLocationListener);
            }
            catch (SecurityException e) {
                e.printStackTrace();
            }
            mGpsActive = true;
        }
    }

    @Override
    public void onDestroyView(){
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mDelayedCatMoverHandler != null) {
            mDelayedCatMoverHandler.removeCallbacksAndMessages(null);
        }
        mCatMoverRunnable = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && mLocationManager != null) {
            mLocationManager.unregisterGnssMeasurementsCallback(null);
            mLocationManager.unregisterGnssNavigationMessageCallback(null);
            mLocationManager.unregisterGnssStatusCallback(null);
        }
        mMapView.unloadMap();
        super.onDestroyView();
    }
    @Override
    public void onDestroy () {
        mFragment.removeAllViews();
        super.onDestroy();
    }

    private Runnable mLocationSimulator = new Runnable()
    {
        @Override
        public void run() {
            if (mCurrentFakeUserLocationsIndex < mFakeUserLocations.size()) {
                Location lLocation = mFakeUserLocations.get(mCurrentFakeUserLocationsIndex);
                mLocationListener.onLocationChanged(lLocation);
                mHandler.postDelayed(mLocationSimulator, mLocationUpdateDelayInMilliSecs);
                mCurrentFakeUserLocationsIndex++;
            }
        }
    };

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
            mPlaceToggle.setEnabled(true);
        }
    };

    private VMEMapListener mMapListener = new VMEMapListener()
    {
        @Override
        public void mapDidReceiveTapGesture(VMEMapView mapView, VMEPosition position) {
            super.mapDidReceiveTapGesture(mapView, position);
        }
    };

}
