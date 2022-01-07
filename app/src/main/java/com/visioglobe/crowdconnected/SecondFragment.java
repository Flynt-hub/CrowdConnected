package com.visioglobe.crowdconnected;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.visioglobe.crowdconnected.databinding.FragmentSecondBinding;
import com.visioglobe.visiomoveessential.VMEMapView;

import java.util.Map;

public class SecondFragment extends Fragment
{

    private FragmentSecondBinding binding;
    private Button mLocationButton;
    private Context mContext;
    private FusedLocationProviderClient mFusedLocationClient;

    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), lPermissionMap -> {
                boolean lOk = true;
                for(Map.Entry<String, Boolean> lEntry : lPermissionMap.entrySet() )
                {
                    if( ! lEntry.getValue() )
                    {
                        lOk = false;
                        break;
                    }
                }
                if( lOk ) getLocation();
            });

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        this.mContext = this.getContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient( mContext );
        this.mLocationButton = getView().findViewById(R.id.button_get_position);
        super.onViewCreated(view, savedInstanceState);
        VMEMapView mapView = (VMEMapView) view.findViewById(R.id.mapView);
        mapView.loadMap();
        binding.buttonSecond.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
        this.mLocationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if( ContextCompat.checkSelfPermission( mContext, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission( mContext, Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED )
                {
                    getLocation();
                }
                else
                {
                    requestPermissionLauncher.launch(new String [] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION} );
                }
            }
        });
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }

    private void getLocation()
    {
//        mFusedLocationClient.getLastLocation().addOnSuccessListener(mContext, new OnSuccessListener<Location>()
//        {
//            @Override
//            public void onSuccess(Location location)
//            {
//                if( location != null )
//                {
//
//                }
//            }
//        })
    }

}