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
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by Mark on 2015.10.25..
 */
public class EtcFragment extends Fragment {

    View view;
    CheckBox autoLoc;
    CheckBox debugNumbers;
    CheckBox debugSendSMS;
    CheckBox lastLicense;
    CheckBox showTicketCheck;
    CheckBox alertBefore;
    CheckBox alertAfter;
    RadioGroup languageGroup;
    RadioButton auto;
    RadioButton en;
    RadioButton hu;
    RadioButton sr;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.etc_layout, container, false);
        Log.d("EtcFragment", "Etc activity started");

        Context cont = getActivity().getApplicationContext();
        //ParkingDataHandler parkhandler = new ParkingDataHandler(cont);
        //parkhandler.checkForUpdate();

        autoLoc =           (CheckBox)      view.findViewById(R.id.checkBoxAutomaticZone);
        debugNumbers = (CheckBox) view.findViewById(R.id.checkBoxDebugNumbers);
        debugSendSMS = (CheckBox) view.findViewById(R.id.checkBoxDebugSendSMS);
        lastLicense =       (CheckBox)      view.findViewById(R.id.checkBoxRememberLicenseNumber);
        showTicketCheck =   (CheckBox)      view.findViewById(R.id.checkBoxShowParkingTicket);
        alertBefore =       (CheckBox)      view.findViewById(R.id.checkBoxAlertBefore);
        alertAfter =        (CheckBox)      view.findViewById(R.id.checkBoxAlertAfter);
        languageGroup = (RadioGroup) view.findViewById(R.id.languageGroup);
        en = (RadioButton) view.findViewById(R.id.radioButtonEnglish);
        hu = (RadioButton) view.findViewById(R.id.radioButtonHungarian);
        sr = (RadioButton) view.findViewById(R.id.radioButtonSerbian);
        auto = (RadioButton) view.findViewById(R.id.radioButtonAutomatic);

        SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(cont);
        autoLoc.setChecked(sharedprefs.getBoolean("autoloc", true));
        lastLicense.setChecked(sharedprefs.getBoolean("lastlicenseremember", true));
        debugNumbers.setChecked(sharedprefs.getBoolean("debugNumbers", true));
        debugSendSMS.setChecked(sharedprefs.getBoolean("debugsendsms", true));
        showTicketCheck.setChecked(sharedprefs.getBoolean("showTicket", true));
        alertBefore.setChecked(sharedprefs.getBoolean("alertBefore", true));
        alertAfter.setChecked(sharedprefs.getBoolean("alertAfter", true));

        switch (sharedprefs.getString("lang", "auto")) {
            case "auto":
                auto.setChecked(true);
                break;
            case "en":
                en.setChecked(true);
                break;
            case "hu":
                hu.setChecked(true);
                break;
            case "sr":
                sr.setChecked(true);
                break;
        }

        languageGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonAutomatic:
                        ((MainActivity) getActivity()).setLanguage("auto");
                        break;
                    case R.id.radioButtonHungarian:
                        ((MainActivity) getActivity()).setLanguage("hu");
                        break;
                    case R.id.radioButtonSerbian:
                        ((MainActivity) getActivity()).setLanguage("sr");
                        break;
                    case R.id.radioButtonEnglish:
                        ((MainActivity) getActivity()).setLanguage("en");
                        break;
                }
            }
        });


        return view;
    }
}
