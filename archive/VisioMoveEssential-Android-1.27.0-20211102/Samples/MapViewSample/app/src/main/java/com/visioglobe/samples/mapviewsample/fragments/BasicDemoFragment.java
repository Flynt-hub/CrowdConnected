/*
 * Copyright (c) Visioglobe SAS. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.visioglobe.samples.mapviewsample.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.visioglobe.samples.mapviewsample.R;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * This fragment is the simplest demo for VisioMove Essential's VMEMapView. It simply loads and
 * displays a map.
 */
public class BasicDemoFragment extends android.support.v4.app.Fragment {
    /**
     * The fragment's map view.
     */
    private VMEMapView mMapView;

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        if (mMapView == null) {
            mMapView = (VMEMapView) pInflater.inflate(R.layout.basic_demo_fragment, pContainer, false);
            // Set the life cycle listener
            mMapView.setLifeCycleListener(mLifeCycleListener);
            // Load the map
            mMapView.loadMap();
        }
        return mMapView;
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

    private VMELifeCycleListener mLifeCycleListener = new VMELifeCycleListener()
    {
        @Override
        public void mapDidInitializeEngine(VMEMapView mapView) {
            String lFilePath = extractFromAssetsAndGetFilePath("artifika_regular.ttf");
            if(lFilePath != null){
                mapView.setMapFont(lFilePath);
            }
        }
    };

    private String extractFromAssetsAndGetFilePath(String pFileName) {
        Context ctx = getContext();
        if(ctx != null){
            File f = new File(getContext().getCacheDir() + "/" + pFileName);
            if (!f.exists()) {
                try {
                    InputStream is = getContext().getAssets().open(pFileName);
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(buffer);
                    fos.close();
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return f.getPath();
        }
        return null;
    }
}
