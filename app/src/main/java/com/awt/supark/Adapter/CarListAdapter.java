package com.awt.supark.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awt.supark.MainActivity;
import com.awt.supark.Model.Car;
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
            holder.name =           (TextView)      view.findViewById(R.id.name);
            holder.licensePlate =   (LinearLayout)  view.findViewById(R.id.licensePlateLayout);
            holder.city =           (TextView)      view.findViewById(R.id.city);
            holder.num =            (TextView)      view.findViewById(R.id.num);
            holder.editbtn =        (ImageButton)   view.findViewById(R.id.imageButton);
            holder.state =          (TextView)      view.findViewById(R.id.state);
            holder.remaining =      (TextView)      view.findViewById(R.id.remaining);
            holder.buttonCancel =   (ImageButton)   view.findViewById(R.id.imageButtonCancel);
            holder.cardGeneral =    (CardView)      view.findViewById(R.id.cardGeneral);

            view.setTag(holder);
        } else {
            view = convertView;
            holder =                (ViewHolder)    view.getTag();
        }

        final Car car = carArray.get(position);

        holder.name.setText(car.getName());
        Typeface licenseFont = Typeface.createFromAsset(context.getAssets(), "fonts/LicensePlate.ttf");
        holder.city.setTypeface(licenseFont);
        holder.num.setTypeface(licenseFont);
        updateLicensePlate(car.getLicens(), car.getGeneric(), holder.city, holder.num, holder.licensePlate);
        holder.state.setText(car.getState());

        Log.i("State", car.getState() + "...");

        if (car.getState() == context.getResources().getString(R.string.parked)) {
            int time = car.getRemaining();
            int totalMinutes = (time / 60) + 1; // Don't know why, but this was the only way that worked it out

            holder.remaining.setText(Integer.toString(totalMinutes) + " " + context.getResources().getString(R.string.minutes_remaining));
            holder.remaining.setVisibility(View.VISIBLE);
            holder.buttonCancel.setVisibility(View.VISIBLE);
        } else {
            holder.buttonCancel.setVisibility(View.INVISIBLE);
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
                ((MainActivity) context).CarHandler.stopPark(car.getSqlid());
                holder.state.setText(context.getResources().getString(R.string.free));
                holder.remaining.setVisibility(View.GONE);
                holder.buttonCancel.setVisibility(View.INVISIBLE);
                ((MainActivity) context).openCarFragment(null, false);
            }
        });

        holder.cardGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).setLicense(v, car.getLicens());
            }
        });

        return view;
    }

    public void updateLicensePlate(CharSequence charSequence, int generic, TextView txtCity, TextView txtNum, LinearLayout licensePlate) {
        if (generic == 0) {
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

    public class ViewHolder {
        TextView name, city, num, state, remaining;
        ImageButton editbtn;
        ImageButton buttonCancel;
        LinearLayout licensePlate;
        CardView cardGeneral;
    }
}
