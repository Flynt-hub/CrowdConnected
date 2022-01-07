/*
 * Copyright (c) Visioglobe SAS. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.visioglobe.samples.mapviewsample;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.visioglobe.samples.mapviewsample.fragments.BasicDemoFragment;
import com.visioglobe.samples.mapviewsample.fragments.CameraDemoFragment;
import com.visioglobe.samples.mapviewsample.fragments.ContentDemoFragment;
import com.visioglobe.samples.mapviewsample.fragments.HomeFragment;
import com.visioglobe.samples.mapviewsample.fragments.LocationDemoFragment;
import com.visioglobe.samples.mapviewsample.fragments.OverlayViewDemoFragment;
import com.visioglobe.samples.mapviewsample.fragments.RoutingCustomLocationTrackerDemoFragment;
import com.visioglobe.samples.mapviewsample.fragments.RoutingDemoFragment;
import com.visioglobe.samples.mapviewsample.fragments.SearchDemoFragment;
import com.visioglobe.samples.mapviewsample.fragments.StatisticsDemoFragment;
import com.visioglobe.visiomoveessential.VMEMapView;

/**
 * The MapViewSample's main (and only) activity. It manages the navigation drawer and the switching
 * of demo fragments.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    public static final String LOG_TAG = "MapSample";
    private Toolbar toolbar;
    private int mMargin = 0;
    /**
     * A reference to the basic demo fragment. We keep it to retain state of this particular
     * fragment (and the map view it contains).
     */
    private BasicDemoFragment mBasicDemoFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        mMargin = (int) getResources().getDimension(com.visioglobe.visiomoveessential.R.dimen.margin);
        toolbar = findViewById(R.id.toolbar);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)toolbar.getLayoutParams();
        layoutParams.setMargins(0,mMargin,0, 0);
        toolbar.setLayoutParams(layoutParams);
        setSupportActionBar(toolbar);
        Log.i(LOG_TAG, "VisioMoveEssential v" + VMEMapView.getVersion());

        // Find our drawer view
        DrawerLayout lDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, lDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        lDrawer.addDrawerListener(toggle);
        lDrawer.openDrawer(GravityCompat.START);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, new HomeFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int lItemId = item.getItemId();
        Fragment lFragment = null;
        String lTitle = getString(R.string.app_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DisplayCutout cutout = getWindow().getDecorView().getRootWindowInsets().getDisplayCutout();
            if(null != cutout) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)toolbar.getLayoutParams();
                layoutParams.setMargins(cutout.getSafeInsetLeft(),cutout.getSafeInsetTop()-mMargin,cutout.getSafeInsetRight(), 0);
                toolbar.setLayoutParams(layoutParams);
            }
        }

        if(lItemId == R.id.basic_demo){
            getSupportActionBar().hide();
            // The basic demo is the only demo fragment that will persist it's state. This is
            // done simply by retaining a reference on the fragment.
            if (mBasicDemoFragment == null) {
                mBasicDemoFragment = new BasicDemoFragment();
            }
            lFragment = mBasicDemoFragment;
            lTitle = getString(R.string.basic_demo);
        }else{
            getSupportActionBar().show();
            switch (lItemId) {
                case R.id.home: {
                    lFragment = new HomeFragment();
                    lTitle = getString(R.string.home);
                }
                break;
                case R.id.camera_demo: {
                    lFragment = new CameraDemoFragment();
                    lTitle = getString(R.string.camera_demo);

                }
                break;
                case R.id.routing_demo: {
                    lFragment = new RoutingDemoFragment();
                    lTitle = getString(R.string.routing_demo);
                }
                break;
                case R.id.location_demo: {
                    lFragment = new LocationDemoFragment();
                    lTitle = getString(R.string.location_demo);
                }
                break;
                case R.id.content_demo: {
                    lFragment = new ContentDemoFragment();
                    lTitle = getString(R.string.content_demo);
                }
                break;
                case R.id.search_demo: {
                    lFragment = new SearchDemoFragment();
                    lTitle = getString(R.string.search_demo);
                }
                break;
                case R.id.overlay_demo: {
                    lFragment = new OverlayViewDemoFragment();
                    lTitle = getString(R.string.overlay_demo);
                }
                break;
                case R.id.statistics_demo: {
                    lFragment = new StatisticsDemoFragment();
                    lTitle = "Statistics";
                }
                break;
                case R.id.routing_custom_location_tracker: {
                    lFragment = new RoutingCustomLocationTrackerDemoFragment();
                    lTitle = getString(R.string.routing_custom_location_tracker);
                }
                break;
            }
        }


        android.support.v7.widget.Toolbar lToolbar = (Toolbar) this.findViewById(R.id.toolbar);
        lToolbar.setTitle(lTitle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, lFragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
