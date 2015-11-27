package com.awt.supark;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

/**
 * Created by Mark on 2015.10.25..
 */
public class EtcFragment extends Fragment {

    View view;
    CheckBox autoLoc;
    CheckBox lastLicense;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.etc_layout, container, false);
        Log.d("EtcFragment", "Etc activity started");
        Context cont = getActivity().getApplicationContext();
        ParkingDataHandler parkhandler = new ParkingDataHandler(cont);
        parkhandler.checkForUpdate();
        autoLoc = (CheckBox) view.findViewById(R.id.checkBoxAutomaticZone);
        lastLicense = (CheckBox) view.findViewById(R.id.checkBoxRememberLicenseNumber);
        SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(cont);
        autoLoc.setChecked(sharedprefs.getBoolean("autoloc", true));
        lastLicense.setChecked(sharedprefs.getBoolean("lastlicenseremember", true));
        return view;
    }

    public void autoLocListener(View v) {

    }
}
