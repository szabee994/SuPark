package com.awt.supark;

import android.app.ActionBar;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Doctor on 2015.10.25..
 */
public class StatsFragment extends Fragment {
    View view;
    Context context;
    SQLiteDatabase db;
    private final Handler statsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i("msg",msg.toString());
            if(msg.what == 10){
                switch (msg.arg1){
                    case 2:
                        printStats();
                        break;
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.stats_layout, container, false);
        context = getContext();
        Log.d("StatsFragment", "Stats activity started");

        ParkingDataHandler parkdata = new ParkingDataHandler(context);
        parkdata.throwHandler(statsHandler);
        parkdata.updateStats();

        return view;
    }

    public void printStats(){
        try {
            // dani baszki how dis works?
            // database stuff from here
            db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/ParkingDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
            Cursor d = db.rawQuery("SELECT * FROM regions ORDER BY zone_id ASC", null);
            String[] regionname = new String[d.getCount()];
            int[] zoneid = new int[d.getCount()];
            int[] maxparked = new int[d.getCount()];
            int[] currentparked = new int[d.getCount()];
            int regionnum = 0;
            for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
                int nameindex = d.getColumnIndex("name");
                int zoneindex = d.getColumnIndex("zone_id");
                int maxindex = d.getColumnIndex("stats_max");
                int currentindex = d.getColumnIndex("stats_current");
                regionname[regionnum] = d.getString(nameindex);
                zoneid[regionnum] = d.getInt(zoneindex);
                maxparked[regionnum] = d.getInt(maxindex);
                currentparked[regionnum] = d.getInt(currentindex);
                regionnum++;
            }
            d.close();
            // to here
            getActivity().findViewById(R.id.loading).setVisibility(View.GONE);
            LinearLayout content = (LinearLayout)getActivity().findViewById(R.id.contentlayout);
            for (int i = 0; i < regionnum; i++) { //for every region
                LinearLayout layout = new LinearLayout(context); //creating new LinearLayout
                layout.setLayoutParams(new ActionBar.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)); //With these params
                View inflate = getActivity().getLayoutInflater().inflate(R.layout.stat_item_template, layout); //Inflates stat_item_template layout to the newly created LinearLayout
                //From here
                TextView regionnametext = (TextView)inflate.findViewById(R.id.regionname);
                regionnametext.setText(regionname[i]);
                double ratio = (maxparked[i] == 0 ? 0 : ((double)currentparked[i] / (double)maxparked[i])*100);
                TextView statustext = (TextView)inflate.findViewById(R.id.regionstatus);
                statustext.setText(String.format("%.0f", ratio) + " %"); // sets precision of double to 2 digit after ,
                ProgressBar bar = (ProgressBar)inflate.findViewById(R.id.progressBar);
                bar.setProgress((int) ratio);
                int green = (int)(255-(ratio*2.55));
                int red = (int)(ratio*2.55);
                bar.getIndeterminateDrawable().setColorFilter(Color.argb(255, red, green, 0), PorterDuff.Mode.MULTIPLY); //This doesn't work right
                ImageView zone = (ImageView)inflate.findViewById(R.id.zoneimg);
                switch (zoneid[i]){
                    case 1: zone.setImageResource(R.drawable.zone1); break;
                    case 2: zone.setImageResource(R.drawable.zone2); break;
                    case 3: zone.setImageResource(R.drawable.zone3); break;
                    case 4: zone.setImageResource(R.drawable.zone4); break;
                }
                //To here, it's just modifying the template layout to match region info

                content.addView(layout); // Adds the new LinearLayout with modified info to stat content.
            }

        }catch (Exception e){
            Log.i("Exception",e.toString());
        }
    }
}
