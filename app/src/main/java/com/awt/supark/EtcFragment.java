package com.awt.supark;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Mark on 2015.10.25..
 */
public class EtcFragment extends Fragment {

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.etc_layout, container, false);
        Log.d("EtcFragment", "Etc activity started");
        ParkingDataHandler parkhandler = new ParkingDataHandler(getActivity().getApplicationContext());
        parkhandler.checkForUpdate();
        return view;
    }
}
