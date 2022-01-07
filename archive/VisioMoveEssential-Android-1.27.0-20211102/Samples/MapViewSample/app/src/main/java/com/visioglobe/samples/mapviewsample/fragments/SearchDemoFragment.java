/*
 * Copyright (c) Visioglobe SAS. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.visioglobe.samples.mapviewsample.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.visioglobe.samples.mapviewsample.R;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.callbacks.VMESearchViewCallback;
import com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener;

/**
 * This fragment is a demo of VisioMove Essential's VMESearchViewInterface. It creates a map view,
 * loads a map and provides buttons that demonstrate how to show the search view and use it's
 * results.
 */
public class SearchDemoFragment extends android.support.v4.app.Fragment
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
     * The text view that will hold the result from search view
     */
    private TextView mPlaceTextView;

    /**
     * The button that will trigger a camera update to look at the place
     */
    private Button mGoButton;

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        // Recycle existing fragment, if any
        if (mFragment == null) {
            // Inflate the layout
            mFragment = (ViewGroup) pInflater.inflate(R.layout.search_demo_fragment, pContainer, false);

            // Fetch views
            mPlaceTextView = mFragment.findViewById(R.id.found_place);
            mGoButton = mFragment.findViewById(R.id.go_place);

            // Fetch map view, set map listener and load map
            mMapView = mFragment.findViewById(R.id.map_view);
            mMapView.setLifeCycleListener(mLifeCycleListener);
            mMapView.loadMap();

            // Configure the "go" button
            mGoButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    mMapView.showPlaceInfo((String) mPlaceTextView.getText());
                }
            });
            // Disable it, it will be enabled during mapDidLoad.
            mGoButton.setEnabled(false);

            // Configure the button that opens the search view.
            Button lButton = (Button) mFragment.findViewById(R.id.open_search_view);
            lButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    mMapView.showSearchViewWithTitle("Search place", mSearchViewCallback);
                }
            });
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
        super.onDestroyView();
    }
    @Override
    public void onDestroy() {
        mFragment.removeAllViews();
        super.onDestroy();
    }

    /**
     * The map life cycle listener will be notified of map events.
     */
    private VMELifeCycleListener mLifeCycleListener = new VMELifeCycleListener()
    {
        @Override
        public void mapDidLoad(VMEMapView mapView) {
            // Enable the button that open the search view.
            if(mFragment != null && mFragment.getChildCount() > 0 && mFragment.findViewById(R.id.open_search_view) != null){
                mFragment.findViewById(R.id.open_search_view).setEnabled(true);
            }
        }
    };

    /**
     * This is the callback that will be notified of search view events.
     */
    private VMESearchViewCallback mSearchViewCallback = new VMESearchViewCallback() {
        @Override public void searchView(VMEMapView mapView, String placeID) {
            Toast.makeText(getContext(), "Place selected : " + placeID, Toast.LENGTH_LONG).show();
            mPlaceTextView.setText(placeID);
            mGoButton.setEnabled(true);
        }

        @Override
        public void searchViewDidCancel(VMEMapView mapView) {
            Toast.makeText(getContext(), "searchViewDidCancel", Toast.LENGTH_LONG).show();
            mPlaceTextView.setText("");
            mGoButton.setEnabled(false);
        }
    };
}
