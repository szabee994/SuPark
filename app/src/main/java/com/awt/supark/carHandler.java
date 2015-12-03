package com.awt.supark;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

/**
 * Created by docto on 03/12/2015.
 */
public class carHandler {
    Context context;
    SQLiteDatabase db;

    public carHandler(Context cont) {
        context = cont;
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

    public void getCar(int id) {
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/carDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        ContentValues temp = new ContentValues();
        temp.put("parkedstate", 0);
        db.update("cars", temp, "car_id = " + id, null);
        db.close();
    }

    public void setLicenseToArray(final MainActivity act) {

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

        int numberOfCars;

        Cursor d = db.rawQuery("SELECT * FROM cars", null);
        numberOfCars = d.getCount();
        Log.i("Number", Integer.toString(numberOfCars));
        if (numberOfCars > 0) {
            act.licenseNumberDb = new String[numberOfCars];
            int i = 0;
            for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
                int carlicenseindex = d.getColumnIndex("car_license");
                act.licenseNumberDb[i] = d.getString(carlicenseindex);
                i++;
            }
        }
        d.close();
        db.close();

        // Loading license numbers database into the UI element licenseNumber (AutoCompleteTextView)
        act.licenseNumberDbAdapter = new ArrayAdapter<>(act, android.R.layout.select_dialog_item, act.licenseNumberDb);
        act.licenseNumber = (AutoCompleteTextView) act.findViewById(R.id.licenseNumber);
        act.licenseNumber.setThreshold(1);  // Starts the matching after one letter entered
        act.licenseNumber.setAdapter(act.licenseNumberDbAdapter);  // Applying the adapter

        if (act.lastLicense) {
            act.licenseNumber.setText(act.sharedprefs.getString("lastlicense", ""));
        }

    }

}
