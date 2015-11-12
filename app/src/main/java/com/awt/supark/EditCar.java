package com.awt.supark;

import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.app.Activity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Mark on 2015.11.09
 */
public class EditCar extends Fragment {

    View view;

    Button addCarButton;
    SQLiteDatabase cardb;
    SharedPreferences sharedprefs;
    LocationManager locationManager;
    SQLiteDatabase db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_car, container, false);
        addCarButton = (Button) view.findViewById(R.id.DoneButton);

        addCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddCar();
            }
        });


        return view;
    }

   public void AddCar() {
       Fragment fragment = new EditCar();
       String name = "Audi";
       String license = "SU-326-DI";

       int numberOfCars;

       db = SQLiteDatabase.openDatabase(getContext().getFilesDir().getPath()+"/carDB.db",null, SQLiteDatabase.CREATE_IF_NECESSARY);

       Cursor d = db.rawQuery("SELECT * FROM cars", null);
       String[] cars = new String[d.getCount()];
       numberOfCars = d.getCount();
       for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
           int caridindex = d.getColumnIndex("car_id");
           int carnameindex = d.getColumnIndex("car_name");
           int carlicenseindex = d.getColumnIndex("car_license");
           Log.i("NEW CAR", Integer.toString(d.getInt(caridindex)) + "," + d.getString(carnameindex) + "," + d.getString(carlicenseindex));
       }

       EditText carName = (EditText) view.findViewById(R.id.carName);
       EditText carLicense = (EditText) view.findViewById(R.id.carLicense);
       ContentValues values_temp = new ContentValues();
       values_temp.put("car_id",numberOfCars+1);
       values_temp.put("car_name",carName.getText().toString());
       values_temp.put("car_license", carLicense.getText().toString());
       db.insert("cars", null, values_temp);
   }
}
