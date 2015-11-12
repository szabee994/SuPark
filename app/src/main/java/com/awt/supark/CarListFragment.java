package com.awt.supark;

/**
 * Created by Mark on 11/4/2015.
 */

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;


import com.awt.supark.R;
import com.awt.supark.Adapter.CarListAdapter;
import com.awt.supark.Model.Car;

import java.util.ArrayList;


public class CarListFragment extends Fragment {

    View view;
    ListView listview;
    public ArrayList<Car> carArray = new ArrayList<>();
    CarListAdapter adapter;

    SQLiteDatabase cardb;
    SharedPreferences sharedprefs;
    LocationManager locationManager;
    SQLiteDatabase db;

    // Csak mert nem ment el a COMMIT ÚJRA
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list_layout, container, false);
        listview = (ListView) view.findViewById(R.id.listview);
        //csak a szimulalaskent

        // Ez azért kellett, mert ha ezt nyitja meg elsőnek akkor ne fagyjon ki az app. De úgy sem hajtja végre, ha már létezik az adatbázis.
        db = SQLiteDatabase.openDatabase(getContext().getFilesDir().getPath()+"/carDB.db",null, SQLiteDatabase.CREATE_IF_NECESSARY);
        db.execSQL("CREATE TABLE IF NOT EXISTS `cars` (\n" +
                "  `car_id` int(2) NOT NULL ,\n" +
                "  `car_name` varchar(100) NOT NULL,\n" +
                "  `car_license` varchar(100) NOT NULL,\n" +
                "  PRIMARY KEY (`car_id`)\n" +
                ")");


        //------------------------Set car name and license to list---------------------------
        int numberOfCars;

        Cursor d = db.rawQuery("SELECT * FROM cars", null);
        String[] cars = new String[d.getCount()];
        numberOfCars = d.getCount();
        for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
            int caridindex = d.getColumnIndex("car_id");
            int carnameindex = d.getColumnIndex("car_name");
            int carlicenseindex = d.getColumnIndex("car_license");
            Car car = new Car();
            car.setName(d.getString(carnameindex)); // set name
            car.setLicens(d.getString(carlicenseindex)); // set license
            carArray.add(car);
        }

        adapter = new CarListAdapter(getActivity(), carArray);
        listview.setDivider(null);
        listview.setDividerHeight(0);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), String.valueOf(position), Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }
}