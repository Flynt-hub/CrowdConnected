/*
 * Copyright (c) Visioglobe SAS. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */

package com.visioglobe.samples.mapviewsample;

import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.models.VMECameraDistanceRange;
import com.visioglobe.visiomoveessential.models.VMECameraHeading;
import com.visioglobe.visiomoveessential.models.VMECameraPitch;
import com.visioglobe.visiomoveessential.models.VMECameraUpdate;
import com.visioglobe.visiomoveessential.models.VMECameraUpdateBuilder;
import com.visioglobe.visiomoveessential.models.VMELocation;

import java.util.Arrays;


public class CustomLocationTracker
{
    public CustomLocationTracker() {
        super();
    }


    public void trackLocation(VMEMapView mapView, VMELocation pLocation) {
        VMECameraHeading lCameraHeading;
        if(null != pLocation){
            if (pLocation.getBearing() < 0) {
                lCameraHeading = VMECameraHeading.newCurrent();
            } else {
                lCameraHeading = VMECameraHeading.newHeading(pLocation.getBearing());
            }
            VMECameraUpdate lCameraUpdate = new VMECameraUpdateBuilder()
                    .setTargets(Arrays.asList(pLocation.getPosition()))
                    .setHeading(lCameraHeading)
                    .setPitch(VMECameraPitch.newPitch(-30))
                    .setDistanceRange(VMECameraDistanceRange.newAltitudeRange(5, 20))
                    .build();
            mapView.animateCamera(lCameraUpdate, 0.5f, null);
        }
    }
}
