package com.visioglobe.crowdconnected;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import androidx.core.content.ContextCompat;

import com.visioglobe.crowdconnected.databinding.ActivityMainBinding;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.enums.VMELocationTrackingMode;
import com.visioglobe.visiomoveessential.models.VMECameraDistanceRange;
import com.visioglobe.visiomoveessential.models.VMECameraHeading;
import com.visioglobe.visiomoveessential.models.VMECameraPitch;
import com.visioglobe.visiomoveessential.models.VMECameraUpdate;
import com.visioglobe.visiomoveessential.models.VMECameraUpdateBuilder;
import com.visioglobe.visiomoveessential.models.VMELocation;

import android.widget.TextView;

import net.crowdconnected.android.core.Configuration;
import net.crowdconnected.android.core.ConfigurationBuilder;
import net.crowdconnected.android.core.CrowdConnected;
import net.crowdconnected.android.core.StatusCallback;
import net.crowdconnected.android.ips.IPSModule;

import java.util.Arrays;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    private CrowdConnected mCrowdConnected;
    private TextView mLatitudeView, mLongitudeView, mAltitudeView;
    private final Context mContext = this;
    private VMEMapView mMapView;
    private final ActivityResultLauncher< String[] > requestPermissionLauncher =
            registerForActivityResult( new ActivityResultContracts.RequestMultiplePermissions(), lPermissionMap ->
            {
                boolean lOk = true;
                for ( Map.Entry< String, Boolean > lEntry : lPermissionMap.entrySet() )
                {
                    if ( ! lEntry.getValue() )
                    {
                        lOk = false;
                        break;
                    }
                }
                if ( lOk ) showLocation();
                else { mAltitudeView.setText( "permission not granted" ); };
            } );

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( ActivityMainBinding.inflate( getLayoutInflater() ).getRoot() );

        mLatitudeView = findViewById( R.id.positionLatText );
        mLongitudeView = findViewById( R.id.positionLonText );
        mAltitudeView = findViewById( R.id.positionAltText );

        mMapView = (VMEMapView) findViewById( R.id.mapView );
        mMapView.setLocationTrackingMode( VMELocationTrackingMode.CUSTOM );
//        mMapView.setSelectorViewVisible( false );
        mMapView.loadMap();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if( mCrowdConnected != null )
            mCrowdConnected.startNavigation();
        if( mMapView != null )
            mMapView.onResume();
        showLocation();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if( mCrowdConnected != null )
            mCrowdConnected.stopNavigation();
        if( mMapView != null )
            mMapView.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if( mCrowdConnected != null )
            mCrowdConnected.stop();
        if( mMapView != null )
            mMapView.unloadMap();
    }


    private void showLocation()
    {
        if ( ContextCompat.checkSelfPermission( mContext, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission( mContext, Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission( mContext, Manifest.permission.BLUETOOTH ) == PackageManager.PERMISSION_GRANTED )
        {
            mMapView.setFocusOnMap(); // close other view components such as the navigation, the search view or the place info view...
            getLocation();
        }
        else
        {
            requestPermissionLauncher.launch( new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH } );
        }
    }

    private void getLocation()
    {
        Configuration configuration = new ConfigurationBuilder()
                .withAppKey( "your_api_token" ) // Crowd Connected App Key
                .withToken( "your_crowd_connected_token" ) // Crowd Connected Token
                .withSecret( "your_crowd_connected_secret" ) // Crowd Connected Secret
                .withStatusCallback(new StatusCallback()
                {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onStartUpFailure( String reason )
                    {
                        Log.e( LOG_TAG, "Start up failure: " + reason );
                    }

                    @SuppressLint( "SetTextI18n" )
                    @Override
                    public void onStartUpSuccess()
                    {
                        mCrowdConnected = CrowdConnected.getInstance();
                        if ( mCrowdConnected != null )
                        {
                            mCrowdConnected.registerPositionCallback( lPosition ->
                            {
                                Location lAndroidLocation = new Location("");

                                // this have to be set manually because crowdConnected only use 2D floor map
                                lAndroidLocation.setAltitude( 3 );// hint : register all crowdConnected beacons IDs to determine on which floor you're located => mCrowdConnected.getDeviceId()
                                lAndroidLocation.setLongitude( lPosition.getLongitude() );
                                lAndroidLocation.setLatitude( lPosition.getLatitude() );

                                lAndroidLocation.setAccuracy( 2.f );// TODO change this value for something relevant

                                VMELocation lVMELocation = mMapView.createLocationFromLocation( lAndroidLocation );

                                // custom location tracker
                                VMECameraHeading lCameraHeading;
                                if( null != lVMELocation )
                                {
                                    if ( lVMELocation.getBearing() < 0 )
                                    {
                                        lCameraHeading = VMECameraHeading.newCurrent();
                                    }
                                    else
                                    {
                                        lCameraHeading = VMECameraHeading.newHeading( lVMELocation.getBearing() );
//                                        lCameraHeading = VMECameraHeading.newHeading( 20.f );
                                    }
                                    VMECameraUpdate lCameraUpdate = new VMECameraUpdateBuilder()
                                            .setTargets( Arrays.asList( lVMELocation.getPosition() ) )
                                            .setHeading( lCameraHeading )
                                            .setPitch( VMECameraPitch.newPitch( -50 ) )
//                                            .setDistanceRange( VMECameraDistanceRange.newAltitudeRange( 1, 5 ) )
                                            .setDistanceRange( VMECameraDistanceRange.newRadiusRange( 7.f, 9.f ) )
                                            .build();
                                    mMapView.animateCamera( lCameraUpdate, 0.5f, null );
                                    mMapView.updateLocation( lVMELocation );
                                }
                            });
                        }
                    }
                })
                .addModule( new IPSModule() )
                .build();
        CrowdConnected.start( getApplication(), configuration );
    }
}