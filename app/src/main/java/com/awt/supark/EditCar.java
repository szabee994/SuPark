package com.awt.supark;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
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
    Button cancelButton;
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
    Context context;
    InputFilter filter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_car, container, false);

        addCarButton =      (Button)        view.findViewById(R.id.DoneButton);
        deleteButton =      (Button)        view.findViewById(R.id.DeleteButton);
        cancelButton =      (Button)        view.findViewById(R.id.cancelButton);
        carName =           (EditText)      view.findViewById(R.id.carName);
        carLicense =        (EditText)      view.findViewById(R.id.carLicense);
        txtCity =           (TextView)      view.findViewById(R.id.city);
        txtNum =            (TextView)      view.findViewById(R.id.number);
        radioNewSrb =       (RadioButton)   view.findViewById(R.id.radioNewSrb);
        radioGeneric =      (RadioButton)   view.findViewById(R.id.radioGeneric);
        radioLicenseGroup = (RadioGroup)    view.findViewById(R.id.radioLicenseGroup);
        licensePlate =      (LinearLayout)  view.findViewById(R.id.licensePlate);
        licenseNum = "";
        context =           getContext();

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
            if(isCarParked(editid)) {
                deleteButton.setEnabled(false);
            }
            TextView text = (TextView)view.findViewById(R.id.text1);
            text.setText(context.getResources().getString(R.string.edit_car));
            d.close();
        } else {
            radioNewSrb.setChecked(true);
            radioListener();
        }

        addCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editid == -1) {
                    addCar(v);
                }
                else {
                    editCar(v);
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteQuestionDialog("", getResources().getString(R.string.are_you_sure), v);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) context).openCarFragment(null, true);
            }
        });

        // Filters the emojis and other unwanted characters
        filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        carName.setFilters(new InputFilter[] { filter });
        //carLicense.setFilters(new InputFilter[] { filter });


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
        InputFilter charFilter = filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        if (radioNewSrb.isChecked()) {
            txtCity.setVisibility(View.VISIBLE);
            licensePlate.setBackgroundDrawable(getResources().getDrawable(R.drawable.licenseplate));

            updateLicensePlate(licenseNum);
            //txtNum.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(7), new InputFilter.AllCaps()});
            carLicense.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8), new InputFilter.AllCaps(), charFilter});
        } else if (radioGeneric.isChecked()) {
            txtCity.setVisibility(View.GONE);
            licensePlate.setBackgroundDrawable(getResources().getDrawable(R.drawable.licenseplate2));

            // Reset
            txtCity.setText("");
            updateLicensePlate(licenseNum);
            //txtNum.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(12), new InputFilter.AllCaps()});
            carLicense.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12), new InputFilter.AllCaps(), charFilter});
        }
    }

    public boolean isCarParked(int id) {
        Cursor cursor = db.rawQuery("SELECT * FROM cars WHERE car_id = " + id + " AND parkedstate = 1", null);

        if(cursor.getCount() == 0) {
            cursor.close();
            return false;
        } else {
            cursor.close();
            return  true;
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
        db.delete("cars", "car_id = " + editid, null);
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
        alertBuilder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteCar(v);
            }
        });
        alertBuilder.setNegativeButton(getResources().getString(R.string.no), null);
        alertBuilder.show();
    }

    public void addCar(View v) {
        if (carName.getText().toString().equals("") || carLicense.getText().toString().equals("")) {
            showErrorDialog(getResources().getString(R.string.error), getResources().getString(R.string.empty_car_or_license));
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
            values_temp.put("car_license", carLicense.getText().toString().toUpperCase());
            values_temp.put("parkedstate", 0);

            int generic;
            if (radioNewSrb.isChecked()) {
                generic = 0;
            } else {
                generic = 1;
            }
            values_temp.put("isgeneric", generic);

            // Inserting the new database record
            db.insert("cars", null, values_temp);
            ((MainActivity) getActivity()).openCarFragment(v, -1);
        }
    }

    public void editCar(View v) {
        // Setting values
        ContentValues values_temp = new ContentValues();
        values_temp.put("car_name", carName.getText().toString());
        values_temp.put("car_license", carLicense.getText().toString().toUpperCase());
        int generic;
        if (radioNewSrb.isChecked()) {
            generic = 0;
        } else {
            generic = 1;
        }
        values_temp.put("isgeneric", generic);

        // Inserting the new database record
        db.update("cars", values_temp, "car_id = " + editid, null);
        ((MainActivity)getActivity()).openCarFragment(v, -1);
    }
}
