package com.visioglobe.crowdconnected;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.visioglobe.crowdconnected.databinding.ActivityMainBinding;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.enums.VMELocationTrackingMode;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import net.crowdconnected.android.core.Configuration;
import net.crowdconnected.android.core.ConfigurationBuilder;
import net.crowdconnected.android.core.CrowdConnected;
import net.crowdconnected.android.core.StatusCallback;
import net.crowdconnected.android.ips.IPSModule;

import java.util.Map;

public class MainActivity extends AppCompatActivity
{

    private ActivityMainBinding binding;
    private CrowdConnected mCrowdConnected;
    private Button mLocationButton;
    private TextView mLatitudeView, mLongitudeView, mAltitudeView;
    private final Context mContext = this;
    private FusedLocationProviderClient mFusedLocationClient;
    private VMEMapView mMapView;
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), lPermissionMap ->
            {
                boolean lOk = true;
                for (Map.Entry<String, Boolean> lEntry : lPermissionMap.entrySet())
                {
                    if (!lEntry.getValue())
                    {
                        lOk = false;
                        break;
                    }
                }
                if (lOk) getLocation();
                else {mAltitudeView.setText("permission not granted");};
            });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mLocationButton = findViewById( R.id.button_get_position );
        mLatitudeView = findViewById( R.id.positionLatText );
        mLongitudeView = findViewById( R.id.positionLonText );
        mAltitudeView = findViewById( R.id.positionAltText );
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mMapView = (VMEMapView) findViewById(R.id.mapView);
        mMapView.setLocationTrackingMode( VMELocationTrackingMode.FOLLOW );
        mMapView.loadMap();

        this.mLocationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                getLocation();
            }
        });
    }

    private void getLocation()
    {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
//            mFusedLocationClient.getCurrentLocation( PRIORITY_HIGH_ACCURACY, null ).addOnSuccessListener((Activity) mContext, new OnSuccessListener<Location>()
//            {
//                @Override
//                public void onSuccess(Location location)
//                {
//                    if (location != null)
//                    {
//                        Location lolo = new Location(location);
//                        lolo.setAltitude(3);
//                        VMELocation lVmeLocation = mMapView.createLocationFromLocation( lolo );
//                        mMapView.updateLocation( lVmeLocation );
//
//                        mLatitudeView.setText( Double.toString( lVmeLocation.getPosition().getLatitude() ) );
//                        mLongitudeView.setText( Double.toString( lVmeLocation.getPosition().getLongitude() ) );
//                        mAltitudeView.setText( Double.toString( lolo.getAltitude() ) );
//                    }
//                }
//            });
            mMapView.setFocusOnMap(); // close other view components such as the navigation, the search view or the place info view...
            startCrowdConnected();
        }
        else
        {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    private void startCrowdConnected()
    {
        Configuration configuration = new ConfigurationBuilder()
//                .withAppKey("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBLZXkiOiJhR3FGSnBBaCIsImlhdCI6MTYzOTM5MTMzNCwiZXhwIjoxNjQ2NjQ4OTM0fQ.vGlKEB2LrFWQuwrUkCSiQtLpR13YjtmlGIeEynRVYPw") // Crowd Connected App Key
                .withAppKey("aGqFJpAh") // Crowd Connected App Key
                .withToken("7ce30f4688d94e4bb93e0069a517c817") // Crowd Connected Token
                .withSecret("z9e49L1L3p5N5I5u8L94N6E1DSwfY898") // Crowd Connected Secret
                .withStatusCallback(new StatusCallback()
                {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onStartUpFailure(String reason)
                    {
                        Log.e(LOG_TAG, "Start up failure: " + reason);
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onStartUpSuccess()
                    {
                        mCrowdConnected = CrowdConnected.getInstance();
                        if ( mCrowdConnected != null )
                        {
                            mCrowdConnected.registerPositionCallback( lPosition ->
                            {
                                Location lAndroidLocation = new Location("");

                                lAndroidLocation.setAltitude( 3 );
                                lAndroidLocation.setLongitude( lPosition.getLongitude() );
                                lAndroidLocation.setLatitude( lPosition.getLatitude() );

                                mLatitudeView.setText( Double.toString( lAndroidLocation.getLongitude() ) );
                                mLongitudeView.setText( Double.toString( lAndroidLocation.getLongitude() ) );
                                mAltitudeView.setText( mCrowdConnected.getDeviceId() );

                                mMapView.updateLocation( mMapView.createLocationFromLocation( lAndroidLocation ) );
                            });
                        }
                    }
                })
                .addModule( new IPSModule() )
                .build();
        CrowdConnected.start( getApplication(), configuration );
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if( mCrowdConnected != null )
        {
            mCrowdConnected.stopNavigation();
        }
        mMapView.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if( mCrowdConnected != null )
        {
            mCrowdConnected.startNavigation();
        }
        mMapView.onResume();
    }
}