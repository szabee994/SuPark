package com.awt.supark;

import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;


import com.awt.supark.R;
//import com.awt.supark.adapter.CarListAdapter;
//import com.awt.supark.model.Car;
import com.google.android.gms.common.images.ImageManager;

import java.util.ArrayList;


public class CarsFragment extends Fragment {

    View view;
    ImageButton btn1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.cars_layout, container, false);

        btn1 = (ImageButton) view.findViewById(R.id.buttonAdd);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foo();
            }
        });


        return view;
    }

    public void foo() {
        Toast.makeText(getActivity(), "ADD", Toast.LENGTH_LONG).show();
    }
}
