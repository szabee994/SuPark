package com.awt.supark.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.awt.supark.R;
import com.awt.supark.Model.Car;

import java.util.ArrayList;

/**
 * Created by Mark on 2015.11.05..
 */
public class CarListAdapter extends BaseAdapter {

    Context context;
    ArrayList<Car> carArray;
    LayoutInflater inflater;
    ViewHolder holder;

    public CarListAdapter(Context context, ArrayList<Car> carArray) {
        this.context = context;
        this.carArray = carArray;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return carArray.size();
    }

    @Override
    public Object getItem(int position) {
        return carArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView name, licens;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.licens = (TextView) view.findViewById(R.id.licens);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        Car car = carArray.get(position);
        holder.name.setText(car.getName());
        holder.licens.setText(car.getLicens());
        return view;
    }
}
