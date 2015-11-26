package com.awt.supark;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    TextView txtCity;
    TextView txtNum;
    int editid = -1;
    String licenseNum;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_car, container, false);

        addCarButton = (Button) view.findViewById(R.id.DoneButton);
        deleteButton = (Button) view.findViewById(R.id.DeleteButton);
        carName = (EditText) view.findViewById(R.id.carName);
        carLicense = (EditText) view.findViewById(R.id.carLicense);
        txtCity = (TextView) view.findViewById(R.id.city);
        txtNum = (TextView) view.findViewById(R.id.number);

        // Setting the custom font
        Typeface licenseFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/LicensePlate.ttf");
        txtCity.setTypeface(licenseFont);
        txtNum.setTypeface(licenseFont);

        db = SQLiteDatabase.openDatabase(getContext().getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        final Bundle b = getArguments();

        if(b.getInt("editid") != -1){
            editid = b.getInt("editid");
            Cursor d = db.rawQuery("SELECT * FROM cars WHERE car_id = " + editid, null);
            d.moveToFirst();

            carName.setText(d.getString(d.getColumnIndex("car_name")));

            licenseNum = d.getString(d.getColumnIndex("car_license"));
            carLicense.setText(licenseNum);
            updateLicensePlate(licenseNum);

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

        // License number filler
        carLicense.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateLicensePlate(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    public void updateLicensePlate(CharSequence charSequence) {
        if(charSequence.length() > 1) {
            txtCity.setText(charSequence.subSequence(0, 2));

            if(charSequence.length() > 5) {
                txtNum.setText(charSequence.subSequence(2, charSequence.length()-2) + "-" + charSequence.subSequence(charSequence.length()-2, charSequence.length()));
            }
            else if(charSequence.length() > 2) {
                txtNum.setText(charSequence.subSequence(2, charSequence.length()));
            }
            else {
                txtNum.setText("");
            }
        }
        else {
            txtCity.setText("");
            txtNum.setText("");
        }

        if(charSequence.length() == 8) {
            txtNum.setTextScaleX(0.9f);
        }
        else {
            txtNum.setTextScaleX(1);
        }
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
