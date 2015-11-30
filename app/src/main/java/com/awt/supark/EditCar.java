package com.awt.supark;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    TextView txtCity;
    TextView txtNum;
    int editid = -1;
    String licenseNum;
    RadioButton radioNewSrb;
    RadioButton radioGeneric;
    RadioGroup radioLicenseGroup;
    LinearLayout licensePlate;

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
        radioNewSrb = (RadioButton) view.findViewById(R.id.radioNewSrb);
        radioGeneric = (RadioButton) view.findViewById(R.id.radioGeneric);
        radioLicenseGroup = (RadioGroup) view.findViewById(R.id.radioLicenseGroup);
        licensePlate = (LinearLayout) view.findViewById(R.id.licensePlate);
        licenseNum = "";

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
            if (d.getInt(d.getColumnIndex("isgeneric")) == 0) {
                radioNewSrb.setChecked(true);
                radioGeneric.setChecked(false);
                radioListener();
            } else {
                radioGeneric.setChecked(true);
                radioNewSrb.setChecked(false);
                radioListener();
            }

            deleteButton.setVisibility(View.VISIBLE);
            TextView text = (TextView)view.findViewById(R.id.text1);
            text.setText("Edit car");
            d.close();
        } else {
            radioNewSrb.setChecked(true);
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
                showDeleteQuestionDialog("", "Are you sure?", v);
            }
        });


        // License number filler
        carLicense.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                licenseNum = charSequence.toString();
                updateLicensePlate(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        radioLicenseGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                radioListener();
            }
        });

        return view;
    }

    public void radioListener() {
        if (radioNewSrb.isChecked()) {
            txtCity.setVisibility(View.VISIBLE);
            licensePlate.setBackgroundDrawable(getResources().getDrawable(R.drawable.licenseplate));

            updateLicensePlate(licenseNum);
            txtNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
            carLicense.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        } else if (radioGeneric.isChecked()) {
            txtCity.setVisibility(View.GONE);
            licensePlate.setBackgroundDrawable(getResources().getDrawable(R.drawable.licenseplate2));

            // Reset
            txtCity.setText("");
            updateLicensePlate(licenseNum);
            txtNum.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
            carLicense.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        }
    }

    public void updateLicensePlate(CharSequence charSequence) {
        if(radioNewSrb.isChecked()) {
            if (charSequence.length() > 1) {
                txtCity.setText(charSequence.subSequence(0, 2));

                if (charSequence.length() > 5) {
                    txtNum.setText(charSequence.subSequence(2, charSequence.length() - 2) + "-" + charSequence.subSequence(charSequence.length() - 2,
                            charSequence.length()));
                }
                else if (charSequence.length() > 2) {
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

            if (charSequence.length() == 8) {
                txtNum.setTextScaleX(0.9f);
            }
            else {
                txtNum.setTextScaleX(1);
            }
        }
        else {
            txtNum.setText(charSequence);

            if (charSequence.length() > 10) {
                txtNum.setTextScaleX(0.85f);
            }
            else {
                txtNum.setTextScaleX(1);
            }
        }
    }

    public void DeleteCar(View v) {
        db.delete("cars","car_id = " + editid,null);
        ((MainActivity)getActivity()).setLicenseToArray();
        ((MainActivity)getActivity()).openCarFragment(v, -1);
    }

    public void showErrorDialog(String title, String content) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(content);
        alertBuilder.setPositiveButton("Ok", null);
        alertBuilder.show();
    }

    public void showDeleteQuestionDialog(String title, String content, final View v) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(content);
        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteCar(v);
            }
        });
        alertBuilder.setNegativeButton("No", null);
        alertBuilder.show();
    }

    public void AddCar(View v) {
        if (carName.getText().toString().equals("") || carLicense.getText().toString().equals("")) {
            showErrorDialog("Error", "Car name or license number is empty.");
        }
        else {
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
            int generic;
            if (radioNewSrb.isChecked()) {
                generic = 0;
            } else {
                generic = 1;
            }
            values_temp.put("isgeneric", generic);

            // Inserting the new database record
            db.insert("cars", null, values_temp);
            ((MainActivity) getActivity()).setLicenseToArray();
            ((MainActivity) getActivity()).openCarFragment(v, -1);
        }
    }

    public void editCar(View v) {
        // Setting values
        ContentValues values_temp = new ContentValues();
        values_temp.put("car_name", carName.getText().toString());
        values_temp.put("car_license", carLicense.getText().toString());
        int generic;
        if (radioNewSrb.isChecked()) {
            generic = 0;
        } else {
            generic = 1;
        }
        values_temp.put("isgeneric", generic);

        // Inserting the new database record
        db.update("cars", values_temp, "car_id = " + editid, null);
        ((MainActivity)getActivity()).setLicenseToArray();
        ((MainActivity)getActivity()).openCarFragment(v, -1);
    }
}
