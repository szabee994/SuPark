package com.awt.supark;

/**
 * Created by Mark on 11/4/2015.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
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
    //ezt a tÃ¶mbÃ¶t tÃ¶ltitek majd fel, szervertÅ‘l, vagy filebÃ³l, model alapjÃ¡n van lÃ©trehozva
    public ArrayList<Car> carArray = new ArrayList<>();
    CarListAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list_layout, container, false);
        listview = (ListView) view.findViewById(R.id.listview);
        //csak a szimulalaskent
        for (int i = 0; i < 10; i++) {
            Car car = new Car();
            car.setName("BMW" + String.valueOf(i));
            car.setLicens("ISD" + String.valueOf(i));
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