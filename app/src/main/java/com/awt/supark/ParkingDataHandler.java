package com.awt.supark;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Doctor on 27/10/2015.
 */
public class ParkingDataHandler implements LocationListener {
    SQLiteDatabase db;
    SharedPreferences sharedprefs;
    LocationManager locationManager;
    int lastupdate;
    Context context;
    ArrayList<LatLng> polyLoc[];
    int polyzone[];
    int region[];
    int polynum = 0;
    Handler mHandler;
    Location currloc = new Location("dummyprovider");

    public ParkingDataHandler(Context cont) {
        context = cont;
        Log.i("Context", context.toString());

        // ---------- Initializing database ----------
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/ParkingDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);

        // Zone table
        db.execSQL("CREATE TABLE IF NOT EXISTS `zones` (\n" +
                "  `zone_id` int(2) NOT NULL,\n" +
                "  `maxtime` int(11) NOT NULL,\n" +
                "  `name` varchar(100) NOT NULL,\n" +
                "  `priceperhour` double NOT NULL,\n" +
                "  `phone` int(4) NOT NULL,\n" +
                "  PRIMARY KEY (`zone_id`)\n" +
                ")");

        // Region table
        db.execSQL("CREATE TABLE IF NOT EXISTS `regions` (\n" +
                "  `region_id` int(3) NOT NULL,\n" +
                "  `zone_id` int(2) NOT NULL,\n" +
                "  `name` varchar(100) NOT NULL,\n" +
                "  `location_poly` varchar(1000) NOT NULL,\n" +
                "  `stats_max` INT(2),\n" +
                "  `stats_current` INT(2),\n" +
                "  PRIMARY KEY (`region_id`)\n" +
                ")");
        db.close();

        // Getting the last update timestamp from shardedprefs
        sharedprefs = PreferenceManager.getDefaultSharedPreferences(context);
        lastupdate = sharedprefs.getInt("lastupdate", 0);

        Log.i("ParkingDataHandler", "Last update timestamp: " + Integer.toString(lastupdate));
    }

    public void throwHandler(Handler mHandl){ // Handler init for communication with MainActivity
        mHandler = mHandl;
    }

    public void parkingInit(String state, final MainActivity act) {
        switch (state) {
            case "start":
                if (act.currentZone == 0) { // If there's no zone selected
                    Toast.makeText(act.cont, act.getResources().getString(R.string.wait_for_zone), Toast.LENGTH_LONG).show();
                    act.pullUpStarted = false;
                    break;
                }
                if (act.CarHandler.getIdByLicense(act.currentLicense) == -1) {
                    Toast.makeText(act.cont, act.getResources().getString(R.string.select_license), Toast.LENGTH_LONG).show();
                    act.pullUpStarted = false;
                    break;
                }

                    // Initializing parking process layout
                    act.parkingBackground.setBackgroundColor(act.getResources().getColor(R.color.colorPrimaryDark)); // Background resets
                    act.textParkingScreen.setText(act.getResources().getString(R.string.please_wait)); // Setting text
                    act.btnPark.startAnimation(act.anim_anticipate_rotate_zoom_out); // Button disappears
                    act.anim_anticipate_rotate_zoom_out.setFillAfter(true);
                    act.pullUpStarted = false;

                    // Dimming the background
                    act.backDimmer.setVisibility(View.VISIBLE);
                    act.backDimmer.startAnimation(act.anim_fade_in);
                    act.dimActive = true;
                    act.anim_fade_in.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(act.showTicket) {    // If the user requested the parking ticket preview
                                // Displaying the dialog
                                LayoutInflater inflater = act.getLayoutInflater();
                                View dialoglayout = inflater.inflate(R.layout.parking_ticket_preview, null);

                                TextView zoneText =     (TextView) dialoglayout.findViewById(R.id.zoneInfo);
                                TextView lengthText =   (TextView) dialoglayout.findViewById(R.id.lengthInfo);
                                TextView endText =      (TextView) dialoglayout.findViewById(R.id.endInfo);
                                TextView priceText =    (TextView) dialoglayout.findViewById(R.id.priceInfo);
                                ImageView zonePreview = (ImageView) dialoglayout.findViewById(R.id.zonePreview);

                                // Formatting the parking end time
                                Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.MINUTE, getZoneMaxTime(act.currentZone));
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm (MMM dd.)");
                                final String endTime = sdf.format(calendar.getTime()); // Parking end in HH:mm (MMM dd.)

                                // Setting the values
                                zoneText.setText(context.getResources().getString(R.string.zone) + " " + act.currentZone);
                                lengthText.setText(getZoneMaxTime(act.currentZone) + " " + context.getResources().getString(R.string.minutes));
                                endText.setText(endTime);
                                priceText.setText(context.getResources().getString(R.string.parking_price) + " " + getZonePrice(act.currentZone) + " RSD");

                                // Selects the right parking zone picture
                                switch (act.currentZone) {
                                    case 1: zonePreview.setImageResource(R.drawable.zone1); break;
                                    case 2: zonePreview.setImageResource(R.drawable.zone2); break;
                                    case 3: zonePreview.setImageResource(R.drawable.zone3); break;
                                    case 4: zonePreview.setImageResource(R.drawable.zone4); break;
                                }

                                // Building the AlertDialog
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(act);
                                alertDialog.setView(dialoglayout);
                                alertDialog.setCancelable(false);
                                alertDialog.setPositiveButton(context.getResources().getString(R.string.park), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Making the layout visible
                                        act.parkingBackgroundShow();

                                        // Car enters
                                        act.imageCar.startAnimation(act.anim_car_enter);
                                        act.anim_car_enter.setFillAfter(true);
                                        act.anim_car_enter.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                act.park("send");  // Calls the parking function
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {

                                            }
                                        });
                                    }
                                });
                                alertDialog.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        act.parkingInit("cancel");
                                    }
                                });
                                alertDialog.show();
                            } else {    // If the user not requested the parking ticket preview
                                // Making the layout visible
                                act.parkingBackgroundShow();

                                // Car enters
                                act.imageCar.startAnimation(act.anim_car_enter);
                                act.anim_car_enter.setFillAfter(true);
                                act.anim_car_enter.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        act.park("send");  // Calls the parking function
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                break;
            // Initialising parking cancel
            case "cancel":
                // Deactivating the dim
                act.backDimmer.startAnimation(act.anim_fade_out);
                act.dimActive = false;
                act.park("cancel");

                act.anim_fade_out.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        act.backDisabled = false; // Enabling back button

                        // Restoring the original state of the layout
                        act.backDimmer.setVisibility(View.GONE);
                        act.parkingBackground.setVisibility(View.INVISIBLE);
                        act.btnPark.startAnimation(act.anim_anticipate_rotate_zoom_in); // Button comes back
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                break;
            // Initialising parking finish
            case "finish":
                act.park("finish");

                // Parking process layout changes
                act.layoutHandler.appBackgroundColorChange(act.parkingBackground, 300, 46, 125, 50); // Background turns to green
                act.textParkingScreen.setText(act.getResources().getString(R.string.success)); // Setting the text

                // Car leaves
                act.imageCar.startAnimation(act.anim_car_leave);
                act.anim_car_leave.setFillAfter(true);

                act.anim_car_leave.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // Deactivating the dim
                        act.backDimmer.startAnimation(act.anim_fade_out);
                        act.anim_fade_out.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                act.backDisabled = false; // Enabling back button

                                // Restoring the original state of the layout
                                act.backDimmer.setVisibility(View.GONE);
                                act.dimActive = false;
                                act.parkingBackground.setVisibility(View.INVISIBLE);
                                act.btnPark.startAnimation(act.anim_anticipate_rotate_zoom_in); // Button comes back
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                break;
            case "error":
                // Deactivating the dim
                act.backDimmer.startAnimation(act.anim_fade_out);
                act.park("error");

                act.anim_fade_out.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        act.backDisabled = false; // Enabling back button

                        // Restoring the original state of the layout
                        act.backDimmer.setVisibility(View.GONE);
                        act.dimActive = false;
                        act.parkingBackground.setVisibility(View.INVISIBLE);
                        act.btnPark.startAnimation(act.anim_anticipate_rotate_zoom_in); // Button comes back
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                break;
        }
    }

    public void park(String action, final MainActivity act) {
        switch (action) {
            case "send":
                Log.i("MainActivity", "Parking started");
                act.backDisabled = true; // Disabling back button
                //Switch these to disable SMS sending
                act.smsHandler.sendSms(act.zoneHandler.zoneSmsNumSelector(act), act.currentZone, act.currentLicense); // Sending the sms
                //act.parkingInit("finish");

                break;
            case "cancel":
                Log.i("MainActivity", "Parking cancelled");
                Toast.makeText(act.cont, act.getResources().getString(R.string.parking_cancelled), Toast.LENGTH_SHORT).show();

                break;
            case "finish":
                Log.i("MainActivity", "Parking finished");
                act.parkHandler.postPark(act.currentRegion, act.currentZone, 60); // Uploading the parking data

                // Saving car's state
                String lic = act.currentLicense;
                int id = act.CarHandler.getIdByLicense(lic);
                if (id != -1) {
                    act.CarHandler.saveCarState(id, getZoneMaxTime(act.currentZone), currloc);
                }

                // Saving the last used license number
                sharedprefs.edit().putString("lastlicense", lic).commit();

                // At this point we must start or update the background service
                act.startTimerService(id);

                break;
            case "error":
                Log.i("MainActivity", "Parking error");

                break;
        }
    }

   public boolean getPolys() { // Method to load polygons into array for zone detection
        try {
            db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/ParkingDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
            Cursor d = db.rawQuery("SELECT * FROM regions", null);
            String[] poly = new String[d.getCount()];
            polyzone = new int[d.getCount()];
            region = new int[d.getCount()];
            polyLoc = new ArrayList[d.getCount()];
            for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
                poly[polynum] = d.getString(d.getColumnIndex("location_poly"));
                polyzone[polynum] = d.getInt(d.getColumnIndex("zone_id"));
                region[polynum] = d.getInt(d.getColumnIndex("region_id"));
                polynum++;
            }
            d.close();
            db.close();
            for (int i = 0; i < polynum; i++) { // Boring string operations and throwing into LatLng arraylist
                poly[i] = poly[i].replace("POLYGON((", "");
                poly[i] = poly[i].replace("))", "");
                String vertices[] = poly[i].split(",");
                polyLoc[i] = new ArrayList<>(vertices.length);
                Log.i("Length",Integer.toString(vertices.length));
                for (int j = 0; j < vertices.length; j++) {
                    String verticelatlng[] = vertices[j].split(" ");
                    polyLoc[i].add(j,new LatLng(Double.valueOf(verticelatlng[0]),Double.valueOf(verticelatlng[1])));
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Update DB! (Open ETC Fragment to update!)", Toast.LENGTH_SHORT).show();
            Log.i("Exception", e.toString());
            return false;
        }
        return true;
    }

    public int getRegion(double lat, double lng){
        if(getPolys()) {
            for (int i = 0; i < polynum; i++) {
                if (inRegionFromDouble(lat, lng, polyLoc[i])) {
                    return region[i];
                }
            }
        }
        return -1;
    }

    public boolean inRegionFromDouble(double lat, double lng, ArrayList<LatLng> polyloc){
        LatLng loc = new LatLng(lat,lng);
        return inRegion(loc,polyloc);
    }

    // Ray-Casting thingy for zone detection. (From StackOverflow) Works because maths. (LOL :D - szabee94)
    public boolean inRegion(LatLng location, ArrayList<LatLng> polyLoc){
        if (location==null)
            return false;

        LatLng lastPoint = polyLoc.get(polyLoc.size()-1);
        boolean isInside = false;
        double x = location.longitude;

        for(LatLng point: polyLoc)
        {
            double x1 = lastPoint.longitude;
            double x2 = point.longitude;
            double dx = x2 - x1;

            if (Math.abs(dx) > 180.0)
            {
                // we have, most likely, just jumped the dateline (could do further validation to this effect if needed).  normalise the numbers.
                if (x > 0)
                {
                    while (x1 < 0)
                        x1 += 360;
                    while (x2 < 0)
                        x2 += 360;
                }
                else
                {
                    while (x1 > 0)
                        x1 -= 360;
                    while (x2 > 0)
                        x2 -= 360;
                }
                dx = x2 - x1;
            }

            if ((x1 <= x && x2 > x) || (x1 >= x && x2 < x))
            {
                double grad = (point.latitude - lastPoint.latitude) / dx;
                double intersectAtLat = lastPoint.latitude + ((x - x1) * grad);

                if (intersectAtLat > location.latitude)
                    isInside = !isInside;
            }
            lastPoint = point;
        }

        return isInside;
    }

    public int getZoneByRegion(int region){
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/ParkingDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        int zone = 0;
        Cursor d = db.rawQuery("SELECT zone_id FROM regions WHERE region_id = "+region,null);
        if(d.getCount() > 0) {
            d.moveToFirst();
            zone = d.getInt(d.getColumnIndex("zone_id"));
        }
        d.close();
        db.close();
        return zone;
    }

    public String getRegionName(int region) {
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/ParkingDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        String name = "";
        Cursor d = db.rawQuery("SELECT name FROM regions WHERE region_id = " + region, null);
        if (d.getCount() > 0) {
            d.moveToFirst();
            name = d.getString(d.getColumnIndex("name"));
        }
        d.close();
        db.close();
        return name;
    }

    public double getZonePrice(int zone) {
        double price = 0;
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/ParkingDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        Cursor d = db.rawQuery("SELECT priceperhour FROM zones WHERE zone_id = " + zone, null);
        if (d.getCount() > 0) {
            d.moveToFirst();
            price = d.getDouble(d.getColumnIndex("priceperhour"));
        }
        d.close();
        db.close();
        return price;
    }

    public int getZoneMaxTime(int zone) {
        int maxTime = 0;
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/ParkingDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
        Cursor d = db.rawQuery("SELECT maxtime FROM zones WHERE zone_id = " + zone, null);
        if (d.getCount() > 0) {
            d.moveToFirst();
            maxTime = d.getInt(d.getColumnIndex("maxtime"));
        }
        d.close();
        db.close();
        return maxTime;
    }

    public LatLng getCurrentLocation() {
        if (currloc != null) {
            return new LatLng(currloc.getLatitude(), currloc.getLongitude());
        } else {
            return new LatLng(0, 0);
        }
    }

    //Method to be called from MainActivity
    public void getZone(){
        Location location;
        polynum = 0;
        if(getPolys()) {
            Log.i("Polys","loaded");
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_COARSE);
            crit.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
            crit.setVerticalAccuracy(Criteria.ACCURACY_LOW);
            String best = locationManager.getBestProvider(crit, true);
            try {
                location = locationManager.getLastKnownLocation(best);
                if (location != null) {
                    currloc = location;
                    LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
                    for(int i = 0; i < polynum; i++){
                        if(inRegion(latlng,polyLoc[i])){
                            mHandler.obtainMessage(0,polyzone[i],region[i]).sendToTarget();
                            break;
                        }
                    }
                }
                locationManager.requestLocationUpdates(best, 100, 0, this); //I missed this one out before, soz
            } catch (SecurityException e) {
                Log.i("SecurityException", e.toString());
            }
        }else{
            Log.i("Polys","load failed");
        }
    }

    @Override // Method that gets called on every location change
    public void onLocationChanged(Location location) {
        currloc = location;
        mHandler.obtainMessage(1).sendToTarget();

        //Log.i("Location",location.toString());

        LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
        for(int i = 0; i < polynum; i++){
            if(inRegion(latlng,polyLoc[i])){
                mHandler.obtainMessage(0,polyzone[i],region[i]).sendToTarget();
                break;
            }
        }

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public String md5(String pass) throws Exception //MD5 snippet for token generation
    {
        MessageDigest encr = MessageDigest.getInstance("MD5");
        String md5 = new BigInteger(1, encr.digest(pass.getBytes())).toString(16);
        return String.format("%32s", md5).replace(' ', '0');
    }

   /* public String unzipString(String zippedText) { //Not working right
        int BUFFER_SIZE = 32;
        StringBuilder string = new StringBuilder();
        try {
            InputStream stream = new ByteArrayInputStream(zippedText.getBytes("UTF-8"));
            GZIPInputStream gis = new GZIPInputStream(stream,BUFFER_SIZE);
            byte[] data = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = gis.read(data)) != -1) {
                string.append(new String(data, 0, bytesRead));
            }
            gis.close();
            stream.close();

        } catch (IOException e) {
            Log.i("GZip", e.toString());
        }
        return string.toString();
    }*/

    public void checkForUpdate() {
        retrieveJSON retrieve = new retrieveJSON();
        retrieve.execute("latest");
    }

    public void updateStats() {
        retrieveJSON retrieve = new retrieveJSON();
        retrieve.execute("getstats");
    }

    public void postPark(int region, int zone, int parktime){
        JSONObject parkdata = new JSONObject();
        Class<?> c;
        String serial = "";
        try {
            c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
           Log.i("SerialEx",e.toString());
        }
        try {
            long timestamp = System.currentTimeMillis() / 1000L;
            parkdata.put("uuid", serial);
            parkdata.put("time_parked",timestamp);
            parkdata.put("estimated_leave_time",timestamp+parktime*60);
            parkdata.put("parking_region",region);
            parkdata.put("parking_zone",zone);
            parkdata.put("lat",currloc.getLatitude());
            parkdata.put("lon",currloc.getLongitude());
        }catch (Exception e){
            Log.i("JSONException",e.toString());
        }
        Log.i("JSON",parkdata.toString());
        postJSON postpark = new postJSON();
        postpark.execute(parkdata);
    }

    public class postJSON extends AsyncTask<JSONObject,Void,String>{

        @Override
        protected String doInBackground(JSONObject... params) {
            StringBuilder response = new StringBuilder();
            try {
                String timestamp = Long.toString(System.currentTimeMillis() / 1000L);
                String token = timestamp + "$up4rK";
                String url = "http://supark.axfree.com/api/index.php?do=addpark&token=" + md5(token) + "&timestamp=" + timestamp; //+"&gzip";
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                String urlParameters = "data=" + URLEncoder.encode(params[0].toString(), "UTF-8");
                wr.writeBytes(urlParameters);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }catch (Exception e) {
                Log.i("Exception", e.toString());
            }
            Log.i("Response", response.toString());
            //return unzipString(response.toString());
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("done")){
                if(mHandler != null)
                mHandler.obtainMessage(3,1,0).sendToTarget();
                Log.i("Done", result);
            }else {
                if(mHandler != null)
                mHandler.obtainMessage(3,2,0).sendToTarget();
                Log.i("Done", result);
            }

            super.onPostExecute(result);
        }
    }

    public class retrieveJSON extends AsyncTask<String,Void,String>{
        String JSONString = "";
        @Override
        protected String doInBackground(String... params) {
            StringBuilder response = new StringBuilder();
            try {
                String timestamp = Long.toString(System.currentTimeMillis() / 1000L);
                String token = timestamp + "$up4rK";
                String url = "http://supark.axfree.com/api/index.php?do=" + params[0] + "&token=" + md5(token) + "&timestamp=" + timestamp; //+"&gzip";
                Log.i("url",url);
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                if(mHandler != null)
                mHandler.obtainMessage(10,0,0).sendToTarget();
            }catch (Exception e) {
                Log.i("Exception", e.toString());
                if(mHandler != null){mHandler.obtainMessage(10, 1, 0).sendToTarget();}
            }
            //Log.i("Response",response.toString());
            //return unzipString(response.toString());
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            int i;
            db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/ParkingDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
            JSONString = (!result.isEmpty() ? result : " ");
            Log.i("String",JSONString);
            try{
                JSONObject data = new JSONObject(JSONString);
                if(data.has("time")) {
                    if (data.getLong("time") > lastupdate) {
                        Log.i("Needsupdate", "Yes");
                        retrieveJSON update = new retrieveJSON();
                        update.execute("retrievedb");
                    } else {
                        Log.i("Needsupdate", "No");
                    }
                }else if(data.has("zones")){
                    JSONArray zones = data.getJSONArray("zones");
                    JSONArray regions = data.getJSONArray("regions");
                    db.execSQL("DELETE FROM zones");
                    db.execSQL("DELETE FROM regions");
                    for(i = 0; i < zones.length(); i++){
                        JSONObject zone_temp = zones.getJSONObject(i);
                        ContentValues values_temp = new ContentValues();
                        values_temp.put("zone_id",zone_temp.getInt("zone_id"));
                        values_temp.put("maxtime",zone_temp.getInt("maxtime"));
                        values_temp.put("name",zone_temp.getString("name"));
                        values_temp.put("priceperhour",zone_temp.getDouble("priceperhour"));
                        values_temp.put("phone",zone_temp.getInt("phone"));
                        db.insert("zones",null,values_temp);
                    }
                    for(i = 0; i < regions.length(); i++){
                        JSONObject regions_temp = regions.getJSONObject(i);
                        ContentValues values_temp = new ContentValues();
                        values_temp.put("region_id",regions_temp.getInt("region_id"));
                        values_temp.put("zone_id",regions_temp.getInt("zone_id"));
                        values_temp.put("name",regions_temp.getString("name"));
                        values_temp.put("location_poly",regions_temp.getString("location_poly"));
                        db.insert("regions",null,values_temp);
                    }
                    int lastupd = data.getJSONObject("lastupdate").getInt("time");
                    SharedPreferences.Editor editor = sharedprefs.edit();
                    editor.putInt("lastupdate", lastupd);
                    editor.commit();
                    //getZone();
                    lastupdate = sharedprefs.getInt("lastupdate",0);
                }else if(data.has("regionstats")) {
                    JSONArray regions = data.getJSONArray("regionstats");
                    for(i = 0; i < regions.length(); i++){
                        JSONObject regions_temp = regions.getJSONObject(i);
                        ContentValues values_temp = new ContentValues();
                        values_temp.put("stats_max",regions_temp.getInt("max"));
                        values_temp.put("stats_current",regions_temp.getInt("current"));
                        db.update("regions",values_temp,"region_id = "+regions_temp.getInt("region_id"),null);
                    }
                }
                if(mHandler != null)
                mHandler.obtainMessage(10,2,0).sendToTarget();
            }catch (Exception e){
                if(mHandler != null)
                mHandler.obtainMessage(10,3,0).sendToTarget();
                Log.i("Error",e.toString());
            }
            Log.i("lastupdate",Integer.toString(lastupdate));
            db.close();
            super.onPostExecute(result);
        }
    }
}
