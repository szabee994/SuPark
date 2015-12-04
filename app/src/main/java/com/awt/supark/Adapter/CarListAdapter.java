package com.awt.supark.Adapter;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.awt.supark.MainActivity;
import com.awt.supark.Model.Car;
import com.awt.supark.ParkingTimerService;
import com.awt.supark.R;

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.licens = (TextView) view.findViewById(R.id.licens);
            holder.editbtn = (ImageButton) view.findViewById(R.id.imageButton);
            holder.state = (TextView) view.findViewById(R.id.state);
            holder.remaining = (TextView) view.findViewById(R.id.remaining);
            holder.buttonCancel = (ImageButton) view.findViewById(R.id.imageButtonCancel);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        final Car car = carArray.get(position);
        holder.name.setText(car.getName());
        holder.licens.setText(car.getLicens());
        holder.state.setText(car.getState());

        if (car.getState() == "Parked") {
            int time = car.getRemaining();
            int totalMinutes = (time / 60) + 1; // Don't know why, but this was the only way that worked it out
            holder.remaining.setText(Integer.toString(totalMinutes) + " minutes remaining");
            holder.remaining.setVisibility(View.VISIBLE);
            holder.buttonCancel.setVisibility(View.VISIBLE);
        }
        else {
            holder.buttonCancel.setVisibility(View.GONE);
        }

        holder.editbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).openCarFragment(v, car.getSqlid());
            }
        });

        holder.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).carHandler.getCar(car.getSqlid());
                holder.state.setText("Free");
                holder.remaining.setVisibility(View.GONE);
                holder.buttonCancel.setVisibility(View.GONE);
                Log.i("SQLID", "ID: " + car.getSqlid());

                //MainActivity act = new MainActivity();
                ((MainActivity) context).openCarFragment(null, false);
            }
        });

        return view;
    }

    public class ViewHolder {
        TextView name, licens, state, remaining;
        ImageButton editbtn;
        ImageButton buttonCancel;
    }
}
