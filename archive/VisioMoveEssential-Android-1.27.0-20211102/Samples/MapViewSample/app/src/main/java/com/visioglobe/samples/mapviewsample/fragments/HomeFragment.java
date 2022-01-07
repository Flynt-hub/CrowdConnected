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

import com.visioglobe.samples.mapviewsample.R;

public class HomeFragment extends android.support.v4.app.Fragment
{
    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        return pInflater.inflate(R.layout.home_fragment, pContainer, false);
    }
}
