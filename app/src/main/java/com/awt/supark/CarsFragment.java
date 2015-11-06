package com.awt.supark;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Mark on 2015.10.25..
 */
public class CarsFragment extends Fragment {

    View view;
    Button btn1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.cars_layout, container, false);
        Log.d("CarsFragment", "Cars activity started");
        return view;
    }
}
