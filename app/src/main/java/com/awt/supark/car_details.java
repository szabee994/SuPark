package com.awt.supark;

/**
 * Created by Mark on 11/9/2015.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;


import com.awt.supark.R;
import com.awt.supark.Adapter.CarListAdapter;
import com.awt.supark.Model.Car;

import java.util.ArrayList;


public class car_details extends Fragment {

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.car_details, container, false);
        return view;
    }
}