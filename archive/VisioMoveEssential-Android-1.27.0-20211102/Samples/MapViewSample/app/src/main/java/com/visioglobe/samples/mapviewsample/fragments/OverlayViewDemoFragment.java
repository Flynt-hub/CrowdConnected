/*
 * Copyright (c) Visioglobe SAS. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.visioglobe.samples.mapviewsample.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.visioglobe.samples.mapviewsample.MainActivity;
import com.visioglobe.samples.mapviewsample.R;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.enums.VMEAnchorMode;
import com.visioglobe.visiomoveessential.enums.VMEViewMode;
import com.visioglobe.visiomoveessential.listeners.VMEBuildingListener;
import com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener;
import com.visioglobe.visiomoveessential.listeners.VMEPlaceListener;
import com.visioglobe.visiomoveessential.models.VMECameraUpdate;
import com.visioglobe.visiomoveessential.models.VMECameraUpdateBuilder;
import com.visioglobe.visiomoveessential.models.VMECategory;
import com.visioglobe.visiomoveessential.models.VMEPlace;
import com.visioglobe.visiomoveessential.models.VMEPosition;
import com.visioglobe.visiomoveessential.models.VMESceneContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OverlayViewDemoFragment extends android.support.v4.app.Fragment
{
    private ViewGroup mFragment;
    private VMEMapView mMapView;
    private ToggleButton mOverlayToggle;
    private HashMap<String, View> mOverlays = new HashMap<>();
    private boolean mOverlaysShown = false;
    private Button mMoveButton;
    private View mPlaceView;
    private static String sPlaceOverlayViewID = "placeOverlayView";
    private boolean mIsPlaceMarkerDisplayed = false;

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        // Recycle the existing map view (if any)
        if (mMapView == null) {
            mFragment = (ViewGroup) pInflater.inflate(R.layout.overlay_demo_fragment, pContainer, false);

            // Create the overlays
            mOverlays.put("Overlay1", pInflater.inflate(R.layout.overlay, new FrameLayout(getContext()), false));
            mOverlays.put("Overlay2", pInflater.inflate(R.layout.overlay, new FrameLayout(getContext()), false));
            mOverlays.put("Overlay3", pInflater.inflate(R.layout.overlay, new FrameLayout(getContext()), false));
            mOverlays.put("Overlay4", pInflater.inflate(R.layout.overlay, new FrameLayout(getContext()), false));
            mOverlays.put("Overlay5", pInflater.inflate(R.layout.overlay, new FrameLayout(getContext()), false));

            mOverlays.get("Overlay5").setBackgroundResource(R.drawable.overlay_background_2);

            mPlaceView = pInflater.inflate(R.layout.overlay, new FrameLayout(getContext()), false);
            Button lCloseButton = (Button) mPlaceView.findViewById(R.id.overlayClose);
            lCloseButton.setText("Close");
            lCloseButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    mMapView.removeOverlayView(sPlaceOverlayViewID);
                    mIsPlaceMarkerDisplayed = false;
                }
            });

            // Configure move overlay button.
            // Set disabled, it will be enabled when map view has loaded.
            mMoveButton = mFragment.findViewById(R.id.move_button);
            mMoveButton.setEnabled(false);
            mMoveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    mMapView.setOverlayAnchor("Overlay5", "B1");
                    mMapView.setOverlayAnchor("Overlay1", new VMEPosition(45.74156f, 4.88192f, 0.0f, new VMESceneContext()));
                }
            });

            mOverlayToggle = mFragment.findViewById(R.id.overlay_toggle);
            // Set disabled, it will be enabled when map view has loaded.
            mOverlayToggle.setEnabled(false);
            mOverlayToggle.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if (mOverlaysShown) {
                        // Remove overlays
                        for (Map.Entry<String, View> lEntry : mOverlays.entrySet()) {
                            mMapView.removeOverlayView(lEntry.getKey());
                        }
                        mOverlaysShown = false;
                        mMoveButton.setEnabled(false);
                    }
                    else {
                        // Set up and add overlays
                        // overlay on outside place
                        View lView = mOverlays.get("Overlay1");
                        TextView lTitle = lView.findViewById(R.id.overlayTitle);
                        lTitle.setText("Lone pine Overlay View");
                        mMapView.addOverlayView("Overlay1", lView, "outside-lonepine");

                        // overlay on indoor place
                        lView = mOverlays.get("Overlay2");
                        lTitle = lView.findViewById(R.id.overlayTitle);
                        lTitle.setText("B4-UL00-ID0015");
                        mMapView.addOverlayView("Overlay2", lView, "B4-UL00-ID0015");

                        // overlay on underground place
                        lView = mOverlays.get("Overlay3");
                        lTitle = lView.findViewById(R.id.overlayTitle);
                        lTitle.setText("B2-LL01-ID0008");
                        mMapView.addOverlayView("Overlay3", lView, "B2-LL01-ID0008");

                        // One with lat/lon pos in the first floor of the offices
                        lView = mOverlays.get("Overlay4");
                        lTitle = lView.findViewById(R.id.overlayTitle);
                        lTitle.setText("45.74271 N 4.88076 E");
                        mMapView.addOverlayView("Overlay4", lView, new VMEPosition(45.74271f, 4.88076f, 0.0f, new VMESceneContext("B4", "B4-UL01")));

                        // One with lat/lon pos outside
                        lView = mOverlays.get("Overlay5");
                        lTitle = lView.findViewById(R.id.overlayTitle);
                        lTitle.setText("45.74156 N 4.88192 E");
                        mMapView.addOverlayView("Overlay5", lView, new VMEPosition(45.74156f, 4.88192f, 0.0f, new VMESceneContext()));

                        mOverlaysShown = true;
                        mMoveButton.setEnabled(true);
                    }
                }
            });

            // Load the map
            mMapView = mFragment.findViewById(R.id.map_view);
            mMapView.setLifeCycleListener(mLifeCycleListener);
            mMapView.setPlaceListener(mPlaceListener);
            mMapView.setBuildingListener(mBuildingListener);
            mMapView.loadMap();
        }
        return mFragment;
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
        mFragment.removeAllViews();
        super.onDestroy();
    }
    private VMELifeCycleListener mLifeCycleListener = new VMELifeCycleListener()
    {
        @Override
        public void mapDidLoad(final VMEMapView mapView) {
            mOverlayToggle.setEnabled(true);
        }
    };

    private VMEPlaceListener mPlaceListener = new VMEPlaceListener()
    {
        @Override
        public boolean mapDidSelectPlace(VMEMapView mapView, String placeID, VMEPosition position) {
            Log.i(MainActivity.LOG_TAG, "Place selected: " + placeID + " at position: " + position.toString());
            TextView lPlaceTitle = mPlaceView.findViewById(R.id.overlayTitle);
            TextView lPlaceCategories = mPlaceView.findViewById(R.id.overlayCategories);
            VMEPlace lPlace = mapView.getPlace(placeID);
            String lTitle = (null != lPlace) ? lPlace.getName() : placeID;
            lPlaceTitle.setText(lTitle);

            String lCategories = "";
            for (String lCategoryID : lPlace.getCategories()) {
                VMECategory lCategory = mapView.getCategory(lCategoryID);
                if (!lCategories.isEmpty()) {
                    lCategories += ",";
                }
                lCategories += " " + lCategory.getName();
            }
            lPlaceCategories.setText(lCategories);

            if (mIsPlaceMarkerDisplayed) {
                mMapView.setOverlayAnchor(sPlaceOverlayViewID, position);
            }
            else {
                mMapView.addOverlayView(sPlaceOverlayViewID, mPlaceView, position, VMEAnchorMode.BOTTOM_CENTER);
                mIsPlaceMarkerDisplayed = true;
            }

            ArrayList<VMEPosition> lPositions = new ArrayList<>();
            lPositions.add(position);
            VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                    .setViewMode(VMEViewMode.FLOOR)
                    .setTargets(lPositions)
                    .setPaddingTop(100)
                    .build();

            mMapView.animateCamera(lUpdate);

            return true;
        }
    };

    private VMEBuildingListener mBuildingListener = new VMEBuildingListener()
    {
        @Override
        public boolean mapDidSelectBuilding(VMEMapView mapView, String buildingID, VMEPosition position) {
            VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                    .setTargets(Arrays.asList(buildingID))
                    .build();
            mMapView.updateCamera(lUpdate);

            return true;
        }
    };
}
