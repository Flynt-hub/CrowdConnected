/*
 * Copyright (c) Visioglobe SAS. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.visioglobe.samples.mapviewsample.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.visioglobe.samples.mapviewsample.MainActivity;
import com.visioglobe.samples.mapviewsample.R;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.callbacks.VMEAnimationCallback;
import com.visioglobe.visiomoveessential.enums.VMECameraMoveReason;
import com.visioglobe.visiomoveessential.enums.VMEViewMode;
import com.visioglobe.visiomoveessential.listeners.VMECameraListener;
import com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener;
import com.visioglobe.visiomoveessential.listeners.VMEMapListener;
import com.visioglobe.visiomoveessential.models.VMECameraContext;
import com.visioglobe.visiomoveessential.models.VMECameraDistanceRange;
import com.visioglobe.visiomoveessential.models.VMECameraHeading;
import com.visioglobe.visiomoveessential.models.VMECameraPitch;
import com.visioglobe.visiomoveessential.models.VMECameraUpdate;
import com.visioglobe.visiomoveessential.models.VMECameraUpdateBuilder;
import com.visioglobe.visiomoveessential.models.VMEPosition;
import com.visioglobe.visiomoveessential.models.VMESceneContext;

import java.util.Arrays;
import java.util.List;

/**
 * This fragment is a demo for VisioMove Essential's VME It loads and displays
 * a map. Then provides buttons to test the API that allows to manipulate the camera in the map
 * view.
 */
public class CameraDemoFragment extends android.support.v4.app.Fragment
{
    /**
     * The fragment's map view
     */
    private VMEMapView mMapView;

    private Handler firstHandler = new Handler();
    private Handler secondHandler = new Handler();
    private Handler fifthHandler = new Handler();
    private Handler thirdHandler = new Handler();
    private Handler fourthHandler = new Handler();
    private Handler sixthHandler = new Handler();
    private Handler seventhHandler = new Handler();

    /**
     * The fragment's layout.
     */
    private ViewGroup mFragment;

    /**
     * The checkbox that enables animation
     */
    private CheckBox mAnimateSwitch;

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {

        // Recycle the fragment's layout if already existent.
        if (mFragment == null) {
            // Inflate the fragment's layout
            mFragment = (ViewGroup) pInflater.inflate(R.layout.camera_demo_fragment, pContainer, false);

            // fetch views
            mAnimateSwitch = mFragment.findViewById(R.id.animate);
            mMapView = mFragment.findViewById(R.id.map_view);

            // Set the map listener and load the map.
            mMapView.setMapListener(mMapListener);
            mMapView.setLifeCycleListener(mLifeCycleListener);
            mMapView.setCameraListener(mCameraListener);
            mMapView.loadMap();

            // Configure buttons
            Button lButton = mFragment.findViewById(R.id.go_global);
            lButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    VMECameraUpdate lCameraUpdate = new VMECameraUpdateBuilder()
                            .setTargets(Arrays.asList("outside"))
                            .setViewMode(VMEViewMode.GLOBAL)
                            .setHeading(VMECameraHeading.newPlaceID("outside"))
                            .setPitch(VMECameraPitch.newPitch(-30))
                            .build();

                    if (mAnimateSwitch.isChecked()) {
                        mMapView.animateCamera(lCameraUpdate);
                    }
                    else {
                        mMapView.updateCamera(lCameraUpdate);
                    }
                }
            });
            // Will be enabled on mapDidLoad
            lButton.setEnabled(false);

            lButton = mFragment.findViewById(R.id.go_floor);
            lButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    VMECameraUpdate lCameraUpdate = new VMECameraUpdateBuilder()
                            .setTargets(Arrays.asList("B4-UL05"))
                            .setViewMode(VMEViewMode.FLOOR)
                            .setHeading(VMECameraHeading.newPlaceID("B4"))
                            .setPitch(VMECameraPitch.newPitch(-79))
                            .build();

                    if (mAnimateSwitch.isChecked()) {
                        mMapView.animateCamera(lCameraUpdate);
                    }
                    else {
                        mMapView.updateCamera(lCameraUpdate);
                    }
                }
            });
            // Will be enabled on mapDidLoad
            lButton.setEnabled(false);

            lButton = mFragment.findViewById(R.id.go_place);
            lButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    VMECameraUpdate lCameraUpdate = new VMECameraUpdateBuilder()
                            .setTargets(Arrays.asList("B2-UL01-ID0010"))
                            .setPitch(VMECameraPitch.newDefaultPitch())
                            .build();

                    if (mAnimateSwitch.isChecked()) {
                        mMapView.animateCamera(lCameraUpdate);
                    }
                    else {
                        mMapView.updateCamera(lCameraUpdate);
                    }
                }
            });
            // Will be enabled on mapDidLoad
            lButton.setEnabled(false);

            lButton = mFragment.findViewById(R.id.go_position);
            lButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    VMESceneContext lScene = new VMESceneContext("B3", "B3-UL01");
                    VMEPosition lPos1 = new VMEPosition(45.740797086277347, 4.8810472180340518, 3, lScene);
                    VMEPosition lPos2 = new VMEPosition(45.740876918853147, 4.8805385544669795, 3, lScene);
                    VMEPosition lPos3 = new VMEPosition(45.740555146004695, 4.8806516436439189, 3, lScene);
                    VMEPosition lPos4 = mMapView.getPlacePosition("B3-UL00-ID0070");
                    List<Object> lTargets = Arrays.asList(lPos1, lPos2, lPos3, "B3-UL01-ID0021", lPos4);

                    int lMargin = 50;
                    VMECameraUpdate lCameraUpdate = new VMECameraUpdateBuilder()
                            .setTargets(lTargets)
                            .setPaddingTop(lMargin)
                            .setPaddingBottom(lMargin)
                            .setPaddingLeft(lMargin)
                            .setPaddingRight(lMargin)
                            .setPitch(VMECameraPitch.newCurrent())
                            .build();
                    if (mAnimateSwitch.isChecked()) {
                        mMapView.animateCamera(lCameraUpdate);
                    }
                    else {
                        mMapView.updateCamera(lCameraUpdate);
                    }
                }
            });
            // Will be enabled on mapDidLoad
            lButton.setEnabled(false);
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
        firstHandler.removeCallbacksAndMessages(null);
        secondHandler.removeCallbacksAndMessages(null);
        thirdHandler.removeCallbacksAndMessages(null);
        fourthHandler.removeCallbacksAndMessages(null);
        fifthHandler.removeCallbacksAndMessages(null);
        sixthHandler.removeCallbacksAndMessages(null);
        seventhHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mFragment.removeAllViews();
    }

    private VMEMapListener mMapListener = new VMEMapListener()
    {
        @Override
        public void mapSceneDidUpdate(VMEMapView mapView, VMESceneContext scene, VMEViewMode viewMode) {
            Log.i(MainActivity.LOG_TAG, "Mode: " + viewMode.toString() + " Scene: " + scene.toString());
        }

        @Override
        public void mapDidReceiveTapGesture(VMEMapView mapView, VMEPosition position) {
            if (position == null) {
                Log.i(MainActivity.LOG_TAG, "mapTapGesture: out of map");
            }
            else {
                Log.i(MainActivity.LOG_TAG, "mapTapGesture: " + position.toString());
            }
        }
    };

    private VMELifeCycleListener mLifeCycleListener = new VMELifeCycleListener()
    {
        @Override
        public void mapDidLoad(VMEMapView mapView) {
            firstCameraAnimation();
            if(mFragment != null && mFragment.findViewById(R.id.go_global) != null && mFragment.findViewById(R.id.go_floor) != null && mFragment.findViewById(R.id.go_place) != null && mFragment.findViewById(R.id.go_position) != null){
                mFragment.findViewById(R.id.go_global).setEnabled(true);
                mFragment.findViewById(R.id.go_floor).setEnabled(true);
                mFragment.findViewById(R.id.go_place).setEnabled(true);
                mFragment.findViewById(R.id.go_position).setEnabled(true);
            }
        }

        @Override
        public void mapDidInitializeEngine(VMEMapView mapView) {
            startCameraPosition();
        }
    };

    private void startCameraPosition() {

        VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                .setTargets(Arrays.asList("outside"))
                .setHeading(VMECameraHeading.newPlaceID("outside"))
                .setPitch(VMECameraPitch.newPitch(-30))
                .setViewMode(VMEViewMode.GLOBAL)

                .build();
        mMapView.updateCamera(lUpdate);
    }

    private void firstCameraAnimation() {

        VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                .setTargets(Arrays.asList("outside"))
                .setPitch(VMECameraPitch.newPitch(-79))
                .build();
        VMEAnimationCallback lAnimationCallback = new VMEAnimationCallback()
        {
            @Override
            public void didFinish() {
                firstHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        secondCameraAnimation();
                    }
                }, 250);
            }
        };
        mMapView.animateCamera(lUpdate, 0.7f, lAnimationCallback);
    }
    private void secondCameraAnimation(){
        VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                .setPitch(VMECameraPitch.newPitch(-10))
                .setDistanceRange(VMECameraDistanceRange.newRadiusRange(20,50))
                .build();

        VMEAnimationCallback lAnimationCallback = new VMEAnimationCallback()
        {
            @Override
            public void didFinish() {
                secondHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        thirdCameraAnimation();
                    }
                }, 250);
            }
        };
        mMapView.animateCamera(lUpdate, 0.7f, lAnimationCallback);
    }

    private void thirdCameraAnimation(){
        VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                .setTargets(Arrays.asList("B2-UL00"))
                .setHeading(VMECameraHeading.newPlaceID("B2-UL00"))
                .setDistanceRange(VMECameraDistanceRange.newDefaultAltitudeRange())
                .setPitch(VMECameraPitch.newPitch(-30))
                .build();

        VMEAnimationCallback lAnimationCallback = new VMEAnimationCallback()
        {
            @Override
            public void didFinish() {
                thirdHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        fourthCameraAnimation();
                    }
                }, 250);
            }
        };
        mMapView.animateCamera(lUpdate, 0.7f, lAnimationCallback);
    }

    private void fourthCameraAnimation(){
        VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                .setTargets(Arrays.asList(VMECameraUpdate.CAMERA_FOCAL_POINT))
                .setHeading(VMECameraHeading.newHeading(80))
                .build();
        VMEAnimationCallback lAnimationCallback = new VMEAnimationCallback()
        {
            @Override
            public void didFinish() {
                fourthHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        fifthCameraAnimation();
                    }
                }, 250);
            }
        };
        mMapView.animateCamera(lUpdate, 0.7f, lAnimationCallback);
    }

    private void fifthCameraAnimation(){
        VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                .setHeading(VMECameraHeading.newHeading(45))
                .build();

        VMEAnimationCallback lAnimationCallback = new VMEAnimationCallback()
        {
            @Override
            public void didFinish() {
                fifthHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        sixthCameraAnimation();
                    }
                }, 250);
            }
        };
        mMapView.animateCamera(lUpdate, 0.7f, lAnimationCallback);

    }
    private void sixthCameraAnimation(){
        VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                .setTargets(Arrays.asList("B1-UL00"))
                .setHeading(VMECameraHeading.newPlaceID("B1-UL00"))
                .setDistanceRange(VMECameraDistanceRange.newAltitudeRange(200,250))
                .setPitch(VMECameraPitch.newPitch(-79))
                .build();
        VMEAnimationCallback lAnimationCallback = new VMEAnimationCallback()
        {
            @Override
            public void didFinish() {
                sixthHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        seventhCameraAnimation();
                    }
                }, 250);
            }
        };
        mMapView.animateCamera(lUpdate, 0.7f, lAnimationCallback);
    }

    private void seventhCameraAnimation(){
        VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                .setDistanceRange(VMECameraDistanceRange.newRadiusRange(20,50))
                .build();
        VMEAnimationCallback lAnimationCallback = new VMEAnimationCallback()
        {
            @Override
            public void didFinish() {
                seventhHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        eighthCameraAnimation();
                    }
                }, 250);
            }
        };
        mMapView.animateCamera(lUpdate, 0.7f, lAnimationCallback);
    }

    private void eighthCameraAnimation(){
        VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                .setTargets(Arrays.asList("outside"))
                .setHeading(VMECameraHeading.newPlaceID("outside"))
                .setViewMode(VMEViewMode.GLOBAL)
                .setDistanceRange(VMECameraDistanceRange.newDefaultAltitudeRange())
                .setPitch(VMECameraPitch.newDefaultPitch())
                .build();
        mMapView.animateCamera(lUpdate, 0.7f, null);
    }

    private VMECameraListener mCameraListener = new VMECameraListener()
    {
        public void mapCameraDidMove(VMEMapView mapView) {
            if(mMapView != null){
                VMECameraContext lCameraContext = mapView.getCameraContext();
                VMECameraMoveReason lCameraMoveReason = mapView.getCameraMoveReason();
                Log.i(MainActivity.LOG_TAG, lCameraContext.toString());
            }
        }
    };
}
