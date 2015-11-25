package com.awt.supark;

/**
 * Created by Mark on 11/4/2015.
 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;
import com.awt.supark.Adapter.CarListAdapter;
import com.awt.supark.Model.Car;

import java.util.ArrayList;


public class CarListFragment extends Fragment {

    View view;
    ListView listview;
    public ArrayList<Car> carArray = new ArrayList<>();
    CarListAdapter adapter;
    SQLiteDatabase db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list_layout, container, false);
        listview = (ListView) view.findViewById(R.id.listview);

        // Creating database if it's not exist yet
        db = SQLiteDatabase.openDatabase(getContext().getFilesDir().getPath()+"/carDB.db",null, SQLiteDatabase.CREATE_IF_NECESSARY);
        db.execSQL("CREATE TABLE IF NOT EXISTS `cars` (\n" +
                "  `car_id` int(2) NOT NULL ,\n" +
                "  `car_name` varchar(100) NOT NULL,\n" +
                "  `car_license` varchar(100) NOT NULL,\n" +
                "  PRIMARY KEY (`car_id`)\n" +
                ")");


        //------------------------ Creating the car list ---------------------------
        Cursor d = db.rawQuery("SELECT * FROM cars", null);

        for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
            int carNameIndex = d.getColumnIndex("car_name");
            int carLicenseIndex = d.getColumnIndex("car_license");
            int carSqlId = d.getColumnIndex("car_id");

            Car car = new Car();

            car.setName(d.getString(carNameIndex)); // set name
            car.setLicens(d.getString(carLicenseIndex)); // set license
            car.setSqlid(d.getInt(carSqlId));

            carArray.add(car);
        }

        // List adapter
        adapter = new CarListAdapter(getActivity(), carArray);

        // Turning off dividers
        listview.setDivider(null);
        listview.setDividerHeight(0);

        listview.setAdapter(adapter);

        // Never worked lol
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), String.valueOf(position), Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    // On exit we have to sync car data between this and main activity in order to keep everything up do date
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).setLicenseToArray();
    }
}