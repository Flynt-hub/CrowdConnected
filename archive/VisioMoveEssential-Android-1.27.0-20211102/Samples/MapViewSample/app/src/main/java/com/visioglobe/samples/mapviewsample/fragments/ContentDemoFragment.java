/*
 * Copyright (c) Visioglobe SAS. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.visioglobe.samples.mapviewsample.fragments;

import android.content.ContentResolver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.visioglobe.samples.mapviewsample.R;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.callbacks.VMECustomDataRefreshCallback;
import com.visioglobe.visiomoveessential.enums.VMEPlaceAltitudeMode;
import com.visioglobe.visiomoveessential.enums.VMEPlaceAnchorMode;
import com.visioglobe.visiomoveessential.enums.VMEPlaceDisplayMode;
import com.visioglobe.visiomoveessential.enums.VMEViewMode;
import com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener;
import com.visioglobe.visiomoveessential.models.VMECameraHeading;
import com.visioglobe.visiomoveessential.models.VMECameraUpdate;
import com.visioglobe.visiomoveessential.models.VMECameraUpdateBuilder;
import com.visioglobe.visiomoveessential.models.VMEPlaceOrientation;
import com.visioglobe.visiomoveessential.models.VMEPlaceSize;
import com.visioglobe.visiomoveessential.models.VMEPlaceVisibilityRamp;
import com.visioglobe.visiomoveessential.models.VMEPosition;
import com.visioglobe.visiomoveessential.models.VMESceneContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This fragment is a demo of VisioMove Essential's VMEPlaceDataInterface. It loads a map and
 * provides custom CMS content using a JSON file. The place update will be done in the map
 * listener's mapReadyForPlaceUpdate callback.
 */
public class ContentDemoFragment extends android.support.v4.app.Fragment
{
    /**
     * The fragment's map view
     */
    private VMEMapView mMapView;

    /**
     * The fragment's layout
     */
    private ViewGroup mFragment;

    private ToggleButton mColorToggle;
    private ToggleButton mColorAllToggle;
    private ToggleButton mPlaceToggle;

    private final static int REFRESH_CUSTOM_DATA_INTERVAL = 1000 * 60 * 2; //2 minutes
    private Handler mHandler = new Handler();

    private Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            if(mMapView != null){
                mMapView.fetchCustomData(new VMECustomDataRefreshCallback()
                {
                    @Override
                    public void refreshCustomDataDidFinish(Boolean success, String error) {
                        if(success){
                            JSONObject lCustomData = mMapView.getCustomData();
                            Log.d("ContentDemoFragment -","Custom data = "+lCustomData);
                            JSONObject lCustomDataForPlace = lCustomData.optJSONObject("parking01");
                            Log.d("ContentDemoFragment -","Parking 01 custom data = "+lCustomDataForPlace);
                        }else{
                            Log.e("ContentDemoFragment -",error);
                        }
                    }
                });
            }
            mHandler.postDelayed(mHandlerTask, REFRESH_CUSTOM_DATA_INTERVAL);
        }
    };


    static private @ColorInt
    int randomColor() {
        Random lRandom = new Random();
        int r = lRandom.nextInt(255);
        int g = lRandom.nextInt(255);
        int b = lRandom.nextInt(255);
        @ColorInt int lRandomColor = Color.rgb(r, g, b);
        return lRandomColor;
    }

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        // Recycle the fragment's layout, if any.
        if (mFragment == null) {
            // Inflate the layout.
            mFragment = (ViewGroup) pInflater.inflate(R.layout.content_demo_fragment, pContainer, false);

            // Fetch views.
            mMapView = mFragment.findViewById(R.id.map_view);

            // Set any relevant listeners and load map.
            mMapView.setLifeCycleListener(mLifeCycleListener);
            mMapView.loadMap();
            mColorToggle = mFragment.findViewById(R.id.color_toggle);
            // Configure Color toggle button.
            // Set disabled, it will be enabled when map view has loaded.
            mColorToggle.setEnabled(false);
            mColorToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    List<String> lPlaceIDs = Arrays.asList(
                            "B1-UL00-ID0034",
                            "B1-UL00-ID0042",
                            "B1-UL00-ID0038",
                            "B1-UL00-ID0043",
                            "busstation01",
                            "catOnline");

                    if (isChecked) {
                        @ColorInt int lRandomColor = ContentDemoFragment.randomColor();
                        for (String lPlaceID : lPlaceIDs) {
                            mMapView.setPlaceColor(lPlaceID, lRandomColor);
                        }
                    }
                    else {
                        for (String lPlaceID : lPlaceIDs) {
                            mMapView.resetPlaceColor(lPlaceID);
                        }
                    }
                    // Move the camera to look at the color change
                    VMECameraUpdate lUpdate = new VMECameraUpdateBuilder()
                            .setHeading(VMECameraHeading.newPlaceID("B1"))
                            .setViewMode(VMEViewMode.FLOOR)
                            .setTargets(lPlaceIDs)
                            .build();
                    mMapView.animateCamera(lUpdate);
                }
            });

            mColorAllToggle = mFragment.findViewById(R.id.all_color_toggle);
            // Configure Color toggle button.
            // Set disabled, it will be enabled when map view has loaded.
            mColorAllToggle.setEnabled(false);
            mColorAllToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    List<String> lPlaceIDs = mMapView.queryAllPlaceIDs();

                    if (isChecked) {
                        @ColorInt int lRandomColor = ContentDemoFragment.randomColor();
                        Map<String, Integer> lPlaceToColor = new HashMap<>();
                        for (String lPlaceID : lPlaceIDs) {
                            Integer lIntegerColor = Integer.valueOf(lRandomColor);
                            lPlaceToColor.put(lPlaceID, lIntegerColor);
                        }
                        mMapView.setPlaceColor(lPlaceToColor);
                    }
                    else {
                        mMapView.resetPlaceColor(lPlaceIDs);
                    }
                }
            });

            mPlaceToggle = (ToggleButton) mFragment.findViewById(R.id.place_toggle);
            // Configure Color toggle button.
            // Set disabled, it will be enabled when map view has loaded.
            mPlaceToggle.setEnabled(false);

            mPlaceToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String lCatOfflineDrawable = "lCatOfflineDrawable";
                    String lCatOfflineDrawableRaw = "lCatOfflineDrawableRaw";
                    String lCatOfflineAsset = "lCatOfflineAsset";
                    String lCatMapBundle = "lCatMapBundle";
                    String lCatOnline = "catOnline";

                    if (isChecked) {
                        List<VMEPosition> lPositions = new ArrayList<>();
                        // lCatMapBundle
                        {

                            Uri lIconUri = Uri.parse("/media/map/visio_island_essentials/category_exhibition.png");
                            JSONObject lPlaceData = null;
                            try {
                                lPlaceData = new JSONObject();
                                lPlaceData.put("name", "Cat - Map bundle");
                                lPlaceData.put("description", "<b>Map Bundle</b> <hr> The icon comes from the map bundle.");
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

                            VMEPosition lPos = new VMEPosition(45.74094, 4.88483, 0.0, new VMESceneContext());
                            lPositions.add(lPos);

                            mMapView.addPlace(lCatMapBundle,
                                    lIconUri,
                                    lPlaceData,
                                    lPos,
                                    new VMEPlaceSize(1.0f),
                                    VMEPlaceAnchorMode.BOTTOM_CENTER,
                                    VMEPlaceAltitudeMode.RELATIVE,
                                    VMEPlaceDisplayMode.OVERLAY,
                                    VMEPlaceOrientation.newPlaceOrientationFacing(),
                                    new VMEPlaceVisibilityRamp());

                            mMapView.setPlaceSize(lCatMapBundle, new VMEPlaceSize(20.0f), true);

                        }

                        // lCatOfflineDrawable
                        {
                            int resID = R.drawable.marker_cat_curious;
                            Uri lIconUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                                    getActivity().getResources().getResourcePackageName(resID) + '/' +
                                    getActivity().getResources().getResourceTypeName(resID) + '/' +
                                    getActivity().getResources().getResourceEntryName(resID));

                            JSONObject lPlaceData = null;
                            try {
                                lPlaceData = new JSONObject();
                                lPlaceData.put("name", "Cat - Drawable");
                                lPlaceData.put("description", "<b>Drawable</b> <hr> The icon comes from a drawable.");
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

                            VMEPosition lPos = new VMEPosition(45.74131, 4.88216, 0.0, new VMESceneContext());
                            lPositions.add(lPos);

                            mMapView.addPlace(lCatOfflineDrawable,
                                    lIconUri,
                                    lPlaceData,
                                    lPos,
                                    new VMEPlaceSize(1.0f),
                                    VMEPlaceAnchorMode.BOTTOM_CENTER,
                                    VMEPlaceAltitudeMode.RELATIVE,
                                    VMEPlaceDisplayMode.OVERLAY,
                                    VMEPlaceOrientation.newPlaceOrientationFacing(),
                                    new VMEPlaceVisibilityRamp());

                            mMapView.setPlaceSize(lCatOfflineDrawable, new VMEPlaceSize(20.0f), true);

                        }

                        // lCatOfflineDrawableRaw
                        {
                            int resID = R.raw.marker_cat_grumpy;
                            Uri lIconUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                                    getActivity().getResources().getResourcePackageName(resID) + '/' +
                                    getActivity().getResources().getResourceTypeName(resID) + '/' +
                                    getActivity().getResources().getResourceEntryName(resID));

                            JSONObject lPlaceData = null;
                            try {
                                lPlaceData = new JSONObject();
                                lPlaceData.put("name", "Cat - Drawable raw");
                                lPlaceData.put("description", "<b>Raw drawable</b> <hr> The icon comes from a raw drawable.");
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

                            VMEPosition lPos = new VMEPosition(45.74337, 4.88236, 0.0, new VMESceneContext());
                            lPositions.add(lPos);

                            mMapView.addPlace(lCatOfflineDrawableRaw,
                                    lIconUri,
                                    lPlaceData,
                                    lPos,
                                    new VMEPlaceSize(1.0f),
                                    VMEPlaceAnchorMode.BOTTOM_CENTER,
                                    VMEPlaceAltitudeMode.RELATIVE,
                                    VMEPlaceDisplayMode.OVERLAY,
                                    VMEPlaceOrientation.newPlaceOrientationFacing(),
                                    new VMEPlaceVisibilityRamp());

                            mMapView.setPlaceSize(lCatOfflineDrawableRaw, new VMEPlaceSize(20.0f), true);

                        }
                        // lCatOfflineAsset
                        {
                            Uri lIconUri = Uri.parse("file:///android_asset/marker_cat_alert.png");

                            JSONObject lPlaceData = null;
                            try {
                                lPlaceData = new JSONObject();
                                lPlaceData.put("name", "Cat - Asset");
                                lPlaceData.put("description", "<b>Assets</b> <hr> The icon comes from assets.");
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

                            VMEPosition lPos = new VMEPosition(45.74002, 4.88198, 0.0, new VMESceneContext());
                            lPositions.add(lPos);


                            mMapView.addPlace(lCatOfflineAsset,
                                    lIconUri,
                                    lPlaceData,
                                    lPos,
                                    new VMEPlaceSize(1.0f),
                                    VMEPlaceAnchorMode.BOTTOM_CENTER,
                                    VMEPlaceAltitudeMode.RELATIVE,
                                    VMEPlaceDisplayMode.OVERLAY,
                                    VMEPlaceOrientation.newPlaceOrientationFacing(),
                                    new VMEPlaceVisibilityRamp());

                            mMapView.setPlaceSize(lCatOfflineAsset, new VMEPlaceSize(20.0f), true);

                        }

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
                        mMapView.removePlace(lCatMapBundle);
                        mMapView.removePlace(lCatOfflineDrawable);
                        mMapView.removePlace(lCatOfflineDrawableRaw);
                        mMapView.removePlace(lCatOfflineAsset);
                        mMapView.removePlace(lCatOnline);
                    }
                }
            });

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
        mHandler.removeCallbacksAndMessages(null);
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
        public void mapDidInitializeEngine(VMEMapView mapView) {
            // This is where the place data update must take place (the only moment where the map
            // view can take the data into account).
            try {
                // The data comes from the visio_island_cms_update.json in the raw resources.
                InputStream resourceReader = getResources().openRawResource(R.raw.visio_island_cms_update);
                Writer writer = new StringWriter();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(resourceReader, "UTF-8"));
                    String line = reader.readLine();
                    while (line != null) {
                        writer.write(line);
                        line = reader.readLine();
                    }
                }
                catch (Exception e) {
                }
                finally {
                    try {
                        resourceReader.close();
                    }
                    catch (Exception e) {
                    }
                }
                String lJsonString = writer.toString();
                mapView.updatePlaceData(new JSONObject(lJsonString).getJSONObject("locale").getJSONObject("en"));
            }
            catch (JSONException e) {
            }
        }

        @Override
        public void mapDidLoad(VMEMapView mapView) {
            mColorToggle.setEnabled(true);
            mPlaceToggle.setEnabled(true);
            mColorAllToggle.setEnabled(true);

            JSONObject lCustomData = mMapView.getCustomData();
            Log.d("ContentDemoFragment -","Get data after map did load = "+lCustomData);
            //Refresh and get the data every 2 minutes
            mHandler.post(mHandlerTask);
        }
    };
}
