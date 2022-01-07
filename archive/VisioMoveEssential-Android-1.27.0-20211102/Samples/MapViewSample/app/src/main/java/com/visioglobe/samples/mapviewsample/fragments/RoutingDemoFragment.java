/*
 * Copyright (c) Visioglobe SAS. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.visioglobe.samples.mapviewsample.fragments;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.visioglobe.samples.mapviewsample.R;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.callbacks.VMEComputeRouteCallback;
import com.visioglobe.visiomoveessential.enums.VMERouteDestinationsOrder;
import com.visioglobe.visiomoveessential.enums.VMERouteRequestType;
import com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener;
import com.visioglobe.visiomoveessential.models.VMEPosition;
import com.visioglobe.visiomoveessential.models.VMERouteRequest;
import com.visioglobe.visiomoveessential.models.VMERouteResult;
import com.visioglobe.visiomoveessential.models.VMESegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This fragment is a demo for VisioMove Essential's VME It creates a map
 * view, loads a map and provides buttons to trigger route computing.
 */
public class RoutingDemoFragment extends android.support.v4.app.Fragment
{

    /**
     * The fragment's map view
     */
    private VMEMapView mMapView;

    /**
     * The fragment's layout
     */
    private ViewGroup mFragment;

    /**
     * The checkbox that controls whether computed routes must be accessible
     */
    private CheckBox mAccessibleSwitch;

    /**
     * The checkbox that controls whether computed routes must optimize the waypoint order
     */
    private CheckBox mOptimizeSwitch;

    /**
     * The checkbox that controls whether computed routes must optimize the waypoint order
     */
    private CheckBox mShowNavigationHeader;

    private Button discardNavigation;

    private static final String TAG = RoutingDemoFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        //((AppCompatActivity) getActivity()).getSupportActionBar().show();
        // Recycle the existing layout, if any
        if (mFragment == null) {
            // Inflate the layout
            mFragment = (ViewGroup) pInflater.inflate(R.layout.routing_demo_fragment, pContainer, false);
            // Fetch checkboxes
            mAccessibleSwitch = mFragment.findViewById(R.id.accessible);
            mOptimizeSwitch = mFragment.findViewById(R.id.optimize);
            mShowNavigationHeader = mFragment.findViewById(R.id.showHeader);

            // Fetch map view, set it's map listener and load the map.
            mMapView = mFragment.findViewById(R.id.map_view);
            mMapView.setLifeCycleListener(mLifeCycleListener);
            mMapView.loadMap();

            discardNavigation = mFragment.findViewById(R.id.focus_api);
            // Configure the create route from place button
            Button lButton = mFragment.findViewById(R.id.create_route_from_place);
            lButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    computeRouteFromPlace();
                }
            });

            discardNavigation.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    mMapView.setFocusOnMap();
                }
            });

            mShowNavigationHeader.setChecked(mMapView.getNavigationHeaderViewVisible());
            mShowNavigationHeader.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mMapView.setNavigationHeaderViewVisible(isChecked);
                }
            });

            // Will be enabled during mMapListener.mapDidLoad()
            lButton.setEnabled(false);

            // Configure the create route from location button
            lButton = mFragment.findViewById(R.id.create_route_from_location);
            lButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    computeRouteFromLocation();
                }
            });
            // Will be enabled during mMapListener.mapDidLoad()
            lButton.setEnabled(false);
        }
        return mFragment;
    }

    private void computeRouteFromPlace() {
        List<Object> lDests = new ArrayList<>();
        lDests.addAll(Arrays.asList("B4-UL05-ID0032", "B2-LL01-ID0011", "B3-UL00-ID0070"));
        VMERouteDestinationsOrder lDestOrder = mOptimizeSwitch.isChecked() ?
                VMERouteDestinationsOrder.OPTIMAL :
                VMERouteDestinationsOrder.IN_ORDER;
        VMERouteRequest lRouteRequest = new VMERouteRequest(VMERouteRequestType.FASTEST, lDestOrder, mAccessibleSwitch.isChecked());
        lRouteRequest.setOrigin("B1-UL00-ID0039");
        lRouteRequest.addDestinations(lDests);
        mMapView.computeRoute(lRouteRequest, mRouteCallback);
    }

    private void computeRouteFromLocation() {
        // The altitude determines the layer the position is associated with
        Location lLoc = new Location(LocationManager.GPS_PROVIDER);
        lLoc.setLatitude(45.7431);
        lLoc.setLongitude(4.8832);
        lLoc.setAltitude(0);
        VMEPosition lPositionStart = RoutingDemoFragment.this.mMapView.createPositionFromLocation(lLoc);

        lLoc = new Location(LocationManager.GPS_PROVIDER);
        lLoc.setLatitude(45.7409978);
        lLoc.setLongitude(4.8806556);
        lLoc.setAltitude(0);
        VMEPosition lPositionWaypoint = RoutingDemoFragment.this.mMapView.createPositionFromLocation(lLoc);

        List<Object> lDests = Arrays.asList(lPositionWaypoint, "B4-UL05-ID0032", "B2-LL01-ID0011", "B3-UL00-ID0070");
        VMERouteDestinationsOrder lDestOrder = mOptimizeSwitch.isChecked() ?
                VMERouteDestinationsOrder.OPTIMAL :
                VMERouteDestinationsOrder.IN_ORDER;
        VMERouteRequest lRouteRequest = new VMERouteRequest(VMERouteRequestType.FASTEST, lDestOrder, mAccessibleSwitch.isChecked());

        lRouteRequest.setOrigin(lPositionStart);
        lRouteRequest.addDestinations(lDests);
        mMapView.computeRoute(lRouteRequest, mRouteCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    public void onDestroyView(){
        mMapView.unloadMap();
        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mFragment.removeAllViews();
    }
    private VMELifeCycleListener mLifeCycleListener = new VMELifeCycleListener()
    {
        @Override
        public void mapDidLoad(VMEMapView mapView) {
            mFragment.findViewById(R.id.create_route_from_place).setEnabled(true);
            mFragment.findViewById(R.id.create_route_from_location).setEnabled(true);
            computeRouteFromPlace();
        }

        @Override
        public void mapDidDisplayRoute(VMEMapView mapView, VMERouteResult routeResult) {
            Log.i(TAG, "Route displayed");
        }
    };
    /**
     * The callback that will be notified of route events.
     */
    private VMEComputeRouteCallback mRouteCallback = new VMEComputeRouteCallback() {

        @Override public boolean computeRouteDidFinish(VMEMapView mapView, VMERouteRequest routeRequest, VMERouteResult routeResult) {
            String lRouteDescription = String.format("computeRouteDidFinish, duration: %.0fmins and length: %.0fm", (routeResult.getDuration() / 60), routeResult.getLength());
            Log.i(TAG, lRouteDescription);

            for (VMESegment lSegment : routeResult.getSegments()) {
                Log.i(TAG, "floor transition type: " + lSegment.getFloorTransitionType() + "and id: " + lSegment.getFloorTransitionId());
            }
            return true;
        }
        @Override public void computeRouteDidFail(VMEMapView mapView, VMERouteRequest routeRequest, String error) {
            String lRouteDescription = String.format("computeRouteDidFail, Error: %s", error);
            Log.i(TAG, lRouteDescription);
        }
    };


}
