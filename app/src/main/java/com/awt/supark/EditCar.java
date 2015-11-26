package com.awt.supark;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Mark on 2015.11.09
 */
public class EditCar extends Fragment {
    View view;
    Button addCarButton;
    Button deleteButton;
    SQLiteDatabase db;
    EditText carName;
    EditText carLicense;
    int editid = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_car, container, false);
        addCarButton = (Button) view.findViewById(R.id.DoneButton);
        deleteButton = (Button) view.findViewById(R.id.DeleteButton);
        carName = (EditText) view.findViewById(R.id.carName);
        carLicense = (EditText) view.findViewById(R.id.carLicense);
        db = SQLiteDatabase.openDatabase(getContext().getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        final Bundle b = getArguments();

        if(b.getInt("editid") != -1){
            editid = b.getInt("editid");
            Cursor d = db.rawQuery("SELECT * FROM cars WHERE car_id = " + editid, null);
            d.moveToFirst();
            carName.setText(d.getString(d.getColumnIndex("car_name")));
            carLicense.setText(d.getString(d.getColumnIndex("car_license")));
            deleteButton.setVisibility(View.VISIBLE);
            TextView text = (TextView)view.findViewById(R.id.text1);
            text.setText("Edit car");
            d.close();
        }

        addCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editid == -1) {
                    AddCar(v);
                }
                else {
                    editCar(v);
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCar(v);
            }
        });

        return view;
    }

    public void DeleteCar(View v){
        db.delete("cars","car_id = " + editid,null);
        ((MainActivity)getActivity()).setLicenseToArray();
        ((MainActivity)getActivity()).openCarFragment(v, -1);
    }

    public void AddCar(View v) {
        int numberOfCars;
        // Opening database
        Cursor d = db.rawQuery("SELECT * FROM cars", null);
        numberOfCars = d.getCount();
        d.close();

        // Setting values
        ContentValues values_temp = new ContentValues();
        values_temp.put("car_id", numberOfCars + 1);
        values_temp.put("car_name", carName.getText().toString());
        values_temp.put("car_license", carLicense.getText().toString());

        // Inserting the new database record
        db.insert("cars", null, values_temp);
        ((MainActivity)getActivity()).setLicenseToArray();
        ((MainActivity)getActivity()).openCarFragment(v, -1);
    }

    public void editCar(View v) {
        // Setting values
        ContentValues values_temp = new ContentValues();
        values_temp.put("car_name", carName.getText().toString());
        values_temp.put("car_license", carLicense.getText().toString());

        // Inserting the new database record
        db.update("cars", values_temp, "car_id = " + editid, null);
        ((MainActivity)getActivity()).setLicenseToArray();
        ((MainActivity)getActivity()).openCarFragment(v, -1);
    }
}
