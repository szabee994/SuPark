package com.awt.supark;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.Location;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by docto on 03/12/2015.
 */
public class carHandler {
    Context context;
    SQLiteDatabase db;
    SharedPreferences sharedprefs;

    public carHandler(Context cont) {
        context = cont;
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        db.execSQL("CREATE TABLE IF NOT EXISTS `cars` (\n" +
                "  `car_id` int(2) NOT NULL ,\n" +
                "  `car_name` varchar(100) NOT NULL,\n" +
                "  `car_license` varchar(100) NOT NULL,\n" +
                "  `isgeneric` int(1) NOT NULL,\n" +
                "  `parkedtime` long default 0,\n" +
                "  `parkeduntil` long default 0,\n" +
                "  `parkedlon` double default 0,\n" +
                "  `parkedlat` double default 0,\n" +
                "  `parkedstate` int(1) NOT NULL default 0,\n" +
                "  PRIMARY KEY (`car_id`)\n" +
                ")");
    }

    public int getIdByLicense(String lic) {
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        Cursor d = db.rawQuery("SELECT car_id FROM cars WHERE car_license = '" + lic + "'", null);
        d.moveToFirst();

        int id;

        if (d.isAfterLast()) {
            id = -1;
        } else {
            id = d.getInt(d.getColumnIndex("car_id"));
        }

        d.close();
        db.close();

        return id;
    }

    public String getCarName(String lic) {
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        Cursor d = db.rawQuery("SELECT car_name FROM cars WHERE car_license = '" + lic + "'", null);
        d.moveToFirst();

        String name;

        if (d.isAfterLast()) {
            name = lic;
        } else {
            name = d.getString(d.getColumnIndex("car_name"));
        }

        d.close();
        db.close();

        return name;
    }

    public void saveCarState(int id, int parktime, Location currloc) {
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        ContentValues temp = new ContentValues();
        int timestamp = (int) (System.currentTimeMillis() / 1000L);

        temp.put("parkedtime", timestamp);
        temp.put("parkeduntil", timestamp + parktime * 60); //Parktime
        temp.put("parkedlat", currloc.getLatitude());
        temp.put("parkedlon", currloc.getLongitude());
        temp.put("parkedstate", 1);

        db.update("cars", temp, "car_id = " + id, null);
        db.close();
    }

    // Set car's park state to false
    public void stopPark(int id) {
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        ContentValues temp = new ContentValues();

        temp.put("parkedstate", 0);

        db.update("cars", temp, "car_id = " + id, null);
        db.close();

        // Refreshing the notifications
        ((MainActivity) context).startTimerService();
    }

    public void updateLicense(MainActivity act) {
        updateLicensePlate(getIdByLicense(act.currentLicense), act.licenseCity, act.licenseNum, act.licensePlate);
    }

    public void updateLicensePlate(int sqlid, TextView txtCity, TextView txtNum, LinearLayout licensePlate) {
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        Cursor d = db.rawQuery("SELECT car_license, isgeneric FROM cars WHERE car_id = '" + sqlid + "'", null);
        int generic;
        CharSequence charSequence;
        if (d.getCount() > 0) {
            d.moveToFirst();
            generic = d.getInt(d.getColumnIndex("isgeneric"));
            charSequence = d.getString(d.getColumnIndex("car_license"));
        } else {
            generic = 1;
            charSequence = "NO LICENSE";
        }
        Typeface licenseFont = Typeface.createFromAsset(context.getAssets(), "fonts/LicensePlate.ttf");
        txtCity.setTypeface(licenseFont);
        txtNum.setTypeface(licenseFont);
        if (generic == 0) {
            txtCity.setVisibility(View.VISIBLE);
            licensePlate.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.licenseplate));
            if (charSequence.length() > 1) {
                txtCity.setText(charSequence.subSequence(0, 2));

                if (charSequence.length() > 5) {
                    txtNum.setText(charSequence.subSequence(2, charSequence.length() - 2) + "-" + charSequence.subSequence(charSequence.length() - 2,
                            charSequence.length()));
                } else if (charSequence.length() > 2) {
                    txtNum.setText(charSequence.subSequence(2, charSequence.length()));
                } else {
                    txtNum.setText("");
                }
            } else {
                txtCity.setText("");
                txtNum.setText("");
            }

            if (charSequence.length() == 8) {
                txtNum.setTextScaleX(0.9f);
            } else {
                txtNum.setTextScaleX(1);
            }
        } else {
            txtCity.setVisibility(View.GONE);
            licensePlate.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.licenseplate2));
            txtNum.setText(charSequence);

            if (charSequence.length() > 10) {
                txtNum.setTextScaleX(0.85f);
            } else {
                txtNum.setTextScaleX(1);
            }
        }
    }
}
