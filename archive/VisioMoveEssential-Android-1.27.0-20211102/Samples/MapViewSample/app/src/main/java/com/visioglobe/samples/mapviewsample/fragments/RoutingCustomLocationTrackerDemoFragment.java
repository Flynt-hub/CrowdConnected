/*
 * Copyright (c) Visioglobe SAS. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.visioglobe.samples.mapviewsample.fragments;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.visioglobe.samples.mapviewsample.CustomLocationTracker;
import com.visioglobe.samples.mapviewsample.R;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.callbacks.VMEComputeRouteCallback;
import com.visioglobe.visiomoveessential.enums.VMELocationTrackingMode;
import com.visioglobe.visiomoveessential.enums.VMERouteDestinationsOrder;
import com.visioglobe.visiomoveessential.enums.VMERouteRequestType;
import com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener;
import com.visioglobe.visiomoveessential.listeners.VMELocationTrackingModeListener;
import com.visioglobe.visiomoveessential.listeners.VMEMapListener;
import com.visioglobe.visiomoveessential.models.VMECameraPitch;
import com.visioglobe.visiomoveessential.models.VMECameraUpdate;
import com.visioglobe.visiomoveessential.models.VMECameraUpdateBuilder;
import com.visioglobe.visiomoveessential.models.VMELocation;
import com.visioglobe.visiomoveessential.models.VMEPosition;
import com.visioglobe.visiomoveessential.models.VMERouteRequest;
import com.visioglobe.visiomoveessential.models.VMERouteResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This fragment is a demo for VisioMove Essential's VMELocationInterface API.
 */
public class RoutingCustomLocationTrackerDemoFragment extends android.support.v4.app.Fragment
{
    /**
     * The fragment's map view.
     */
    private VMEMapView mMapView;
    private ToggleButton mSimulateLocation;
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
    private List<Location> mFakeRouteLocations = new ArrayList<>();
    private int mCurrentFakeUserLocationsIndex;
    private VMELocationTrackingMode mCurrentTrackingMode = VMELocationTrackingMode.NONE;
    private VMELocation mVMELocation;

    private ToggleButton mCustomTrackerButton;
    private ToggleButton mDisplayCompassHeadingButton;
    private CustomLocationTracker mCustomTracker = new CustomLocationTracker();

    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private Future mLocationSimulatorTaskFuture;

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        if (mFragment == null) {
            // Inflate the fragment's layout
            mFragment = (ViewGroup) pInflater.inflate(R.layout.routing_custom_location_tracker, pContainer, false);

            // Fetch the views
            mMapView = mFragment.findViewById(R.id.map_view);
            mSimulateLocation = mFragment.findViewById(R.id.simulate_location_button);
            mCustomTrackerButton = mFragment.findViewById(R.id.customTrackingMode);

            mCustomTrackerButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    VMELocationTrackingMode lNewTrackingMode = mCustomTrackerButton.isChecked() ? VMELocationTrackingMode.CUSTOM : VMELocationTrackingMode.NONE;
                    mMapView.setLocationTrackingMode(lNewTrackingMode);
                }
            });

            mDisplayCompassHeadingButton = mFragment.findViewById(R.id.displayCompassHeading);
            mDisplayCompassHeadingButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    mMapView.setCompassHeadingMarkerVisible(mDisplayCompassHeadingButton.isChecked());
                }
            });

            mSimulateLocation.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if(!mSimulateLocation.isChecked()){
                        mMapView.setFocusOnMap();
                        mLocationSimulatorTaskFuture.cancel(true);
                        mExecutorService = Executors.newSingleThreadExecutor();
                        mHandler = new Handler();
                        VMELocation lLocation = mMapView.createLocationFromLocation(mFakeUserLocations.get(0));
                        mMapView.updateLocation(lLocation);
                    }else{
                        mCurrentFakeUserLocationsIndex = 0;
                        computeRouteFromLocation();
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
            initRouteLocations();

            // Set up map listener to know when map view has loaded.
            mMapView.setLifeCycleListener(mLifeCycleListener);
            mMapView.setMapListener(mMapListener);
            mMapView.setLocationTrackingMode(mCurrentTrackingMode);
            mMapView.setLocationTrackingModeListener(mLocationTrackingModeListener);

            List<VMELocationTrackingMode> lToggleTrackingModes = new ArrayList<>();
            lToggleTrackingModes.add(VMELocationTrackingMode.NONE);
            lToggleTrackingModes.add(VMELocationTrackingMode.CUSTOM);
            lToggleTrackingModes.add(VMELocationTrackingMode.FOLLOW);
            mMapView.setLocationTrackingButtonToggleModes(lToggleTrackingModes);
            mMapView.loadMap();
        }
        return mFragment;
    }

    private void simulateLocationCustom(){
        if (mSimulateLocation.isChecked() && mCurrentFakeUserLocationsIndex < mFakeUserLocations.size()) {
            Location lLocation = mFakeUserLocations.get(mCurrentFakeUserLocationsIndex);
            mLocationCustomListener.onLocationChanged(lLocation);
            int mLocationUpdateDelayInMilliSecs = 2000;
            mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run() {
                    simulateLocationCustom();
                }
            }, mLocationUpdateDelayInMilliSecs);
            mCurrentFakeUserLocationsIndex++;
        }
    }

    @Override
    public void onPause() {
        if (mLocationManager != null) {
            // Stop GPS updates.
            mLocationManager.removeUpdates(mLocationCustomListener);
            mMapView.updateLocation(null);
        }
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    public void onDestroyView(){
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if(mLocationSimulatorTaskFuture != null && mSimulateLocation.isChecked()){
            mLocationSimulatorTaskFuture.cancel(true);
        }
        mMapView.unloadMap();
        super.onDestroyView();
    }
    @Override
    public void onDestroy () {
        mFragment.removeAllViews();
        super.onDestroy();
    }

    private void computeRouteFromLocation() {
        // The altitude determines the layer the position is associated with
        VMEPosition lPositionStart = RoutingCustomLocationTrackerDemoFragment.this.mMapView.createPositionFromLocation(mFakeRouteLocations.get(0));

        VMEPosition lPositionWaypoint = RoutingCustomLocationTrackerDemoFragment.this.mMapView.createPositionFromLocation(mFakeRouteLocations.get(1));
        VMEPosition lPositionWaypoint2 = RoutingCustomLocationTrackerDemoFragment.this.mMapView.createPositionFromLocation(mFakeRouteLocations.get(2));
        VMEPosition lPositionWaypoint3 = RoutingCustomLocationTrackerDemoFragment.this.mMapView.createPositionFromLocation(mFakeRouteLocations.get(3));

        List<?> lDests = Arrays.asList(lPositionWaypoint,  lPositionWaypoint2, lPositionWaypoint3);
        VMERouteDestinationsOrder lDestOrder = VMERouteDestinationsOrder.IN_ORDER;
        VMERouteRequest lRouteRequest = new VMERouteRequest(VMERouteRequestType.FASTEST, lDestOrder);

        lRouteRequest.setOrigin(lPositionStart);
        lRouteRequest.addDestinations((List<Object>) lDests);
        mMapView.computeRoute(lRouteRequest, mRouteCallback);
    }

    /**
     * init possible location list to fake user position
     */
    private void initFakeLocations() {
        //SEGMENT 1
        mFakeUserLocations.add(getLocation(45.7414425, 4.8817110, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.74146018470637, 4.8816267137889, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.741503242232234, 4.881454566005357, 0.0, 0, 5));

        //SEGMENT 2
        mFakeUserLocations.add(getLocation(45.74146648908168, 4.881362728302338, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.7413581006088, 4.881325249556848, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.741250622518066, 4.881290955936187, 0.0, 0, 5));

        //LOST SEGMENT
        mFakeUserLocations.add(getLocation(45.74159580963391, 4.880611763935984, 0.0, 0, 5));

        //SEGMENT 3
        mFakeUserLocations.add(getLocation(45.74122681279279, 4.881281834037413, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.741161167362506, 4.881259036449916, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.74111573850439, 4.881241967302465, 0.0, 0, 5));

        //SEGMENT 4
        mFakeUserLocations.add(getLocation(45.74111185234072, 4.88126743386255, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.741082109492496, 4.881358671707478, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.740943741367786, 4.881399690958989, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.7408876462151, 4.881378630427969, 0.0, 0, 5));

        //SEGMENT 5
        mFakeUserLocations.add(getLocation(45.740887806100226, 4.881352023515476, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.74087755098924, 4.881280439421292, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.740872365892706, 4.881238342261903, 0.0, 0, 5));

        //LOST SEGMENT 2
        mFakeUserLocations.add(getLocation(45.740732552168815,  4.880405907263137, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.74067635261162,  4.880451422529728, 0.0, 0, 5));

        //SEGMENT 6
        mFakeUserLocations.add(getLocation(45.74086110693569, 4.881218688870884, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.74083808686493, 4.881192991978083, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.740824658458685, 4.881192991978083, 0.0, 0, 5));

        //SEGMENT 7
        mFakeUserLocations.add(getLocation(45.74083085871918, 4.881128139191, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.740869812520074, 4.881100961623086, 0.0, 0, 5));

        //SEGMENT 8
        mFakeUserLocations.add(getLocation(45.74088103914783, 4.88106341625494, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.740890381049525, 4.881013240897742, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.74091548961581, 4.880795148700796, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.74094078061023, 4.880569249628036, 0.0, 0, 5));
        mFakeUserLocations.add(getLocation(45.7409480, 4.8805126, 0.0, 0, 5));
    }

    /**
     * init fake route
     */
    private void initRouteLocations() {
        mFakeRouteLocations.add(getLocation(45.7414425, 4.8817110, 0.0, 0, 5));
        mFakeRouteLocations.add(getLocation(45.7415096, 4.8813725, 0.0, 0, 5));
        mFakeRouteLocations.add(getLocation(45.7408424, 4.8811657, 0.0, 0, 5));
        mFakeRouteLocations.add(getLocation(45.7409480, 4.8805126, 0.0, 0, 5));
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
    public LocationListener mLocationCustomListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location) {
            // Forward location to the VMEMapView.
            mVMELocation = mMapView.createLocationFromLocation(location);
            if (mCurrentTrackingMode == VMELocationTrackingMode.CUSTOM) {
                mCustomTracker.trackLocation(mMapView, mVMELocation);
            }
            mMapView.updateLocation(mVMELocation);
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
            mCustomTrackerButton.setEnabled(true);
            mSimulateLocation.setEnabled(true);
        }
    };

    private VMELocationTrackingModeListener mLocationTrackingModeListener = new VMELocationTrackingModeListener()
    {
        @Override
        public void mapDidUpdateLocationTrackingMode(VMEMapView mapView, VMELocationTrackingMode locationTrackingMode) {
            mCurrentTrackingMode = locationTrackingMode;
            if (locationTrackingMode != VMELocationTrackingMode.CUSTOM) {
                mCustomTrackerButton.setChecked(false);
                if(null != mVMELocation){
                    VMECameraUpdate lCameraUpdate = new VMECameraUpdateBuilder()
                            .setTargets(Collections.singletonList(VMECameraUpdate.CAMERA_FOCAL_POINT))
                            .setPitch(VMECameraPitch.newPitch(-79))
                            .build();
                    mapView.animateCamera(lCameraUpdate, 0.7f, null);
                }
            }else{
                mCustomTrackerButton.setChecked(true);
            }
        }
    };

    private VMEMapListener mMapListener = new VMEMapListener()
    {
        @Override
        public void mapDidReceiveTapGesture(VMEMapView mapView, VMEPosition position) {
            super.mapDidReceiveTapGesture(mapView, position);
        }
    };
    /**
     * The callback that will be notified of route events.
     */
    private VMEComputeRouteCallback mRouteCallback = new VMEComputeRouteCallback() {
        @Override public boolean computeRouteDidFinish(VMEMapView mapView, VMERouteRequest routeRequest, VMERouteResult routeResult) {
            String lRouteDescription = String.format("computeRouteDidFinish, duration: %.0fmins and length: %.0fm", (routeResult.getDuration() / 60), routeResult.getLength());
            return true;
        }
        @Override public void computeRouteDidFail(VMEMapView mapView, VMERouteRequest routeRequest, String error) {
            String lRouteDescription = String.format("computeRouteDidFail, Error: %s", error);
        }
    };
}
