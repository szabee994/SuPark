package com.awt.supark;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;


import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

/**
 * Created by Doctor on 27/10/2015.
 */
public class ParkingDataHandler implements LocationListener{
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
    Location currloc;

    public void throwHandler(Handler mHandl){ //Handler init for communication with MainActivity
        mHandler = mHandl;
    }

    public ParkingDataHandler(Context cont){
        context = cont;
        Log.i("Context",context.toString());
        db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath()+"/ParkingDB.db",null, SQLiteDatabase.CREATE_IF_NECESSARY);
        db.execSQL("CREATE TABLE IF NOT EXISTS `zones` (\n" +
                "  `zone_id` int(2) NOT NULL,\n" +
                "  `maxtime` int(11) NOT NULL,\n" +
                "  `name` varchar(100) NOT NULL,\n" +
                "  `priceperhour` double NOT NULL,\n" +
                "  `phone` int(4) NOT NULL,\n" +
                "  PRIMARY KEY (`zone_id`)\n" +
                ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS `regions` (\n" +
                "  `region_id` int(3) NOT NULL,\n" +
                "  `zone_id` int(2) NOT NULL,\n" +
                "  `name` varchar(100) NOT NULL,\n" +
                "  `location_poly` varchar(1000) NOT NULL,\n" +
                "  PRIMARY KEY (`region_id`)\n" +
                ")");
        sharedprefs = PreferenceManager.getDefaultSharedPreferences(context);
        lastupdate = sharedprefs.getInt("lastupdate",0);
        Log.i("lastupdate",Integer.toString(lastupdate));
    }

   public boolean getPolys() { //Method to load polygons into array for zone detection
        try {
            db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/ParkingDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
            Cursor d = db.rawQuery("SELECT * FROM regions", null);
            String[] poly = new String[d.getCount()];
            polyzone = new int[d.getCount()];
            region = new int[d.getCount()];
            polyLoc = new ArrayList[d.getCount()];
            for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
                int regionidindex = d.getColumnIndex("region_id");
                int polyindex = d.getColumnIndex("location_poly");
                int zoneindex = d.getColumnIndex("zone_id");
                poly[polynum] = d.getString(polyindex);
                polyzone[polynum] = d.getInt(zoneindex);
                region[polynum] = d.getInt(regionidindex);
                polynum++;
            }
            for (int i = 0; i < polynum; i++) { //Boring string operations and throwing into LatLng arraylist
                poly[i] = poly[i].replace("POLYGON((", "");
                poly[i] = poly[i].replace("))", "");
                String vertices[] = poly[i].split(",");
                polyLoc[i] = new ArrayList<LatLng>(vertices.length);
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

    //Ray-Casting thingy for zone detection. (From StackOverflow) Works because maths.
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

    //Method to be called from MainActivity
    public void getZone(){
        polynum = 0;
        if(getPolys()) {
            Log.i("Polys","loaded");
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_FINE);
            String best = locationManager.getBestProvider(crit,false);
            try {
                locationManager.requestLocationUpdates(best, 1000, 0, this);
            } catch (SecurityException e) {
                Log.i("SecurityException", e.toString());
            }
        }else{
            Log.i("Polys","load failed");
        }
    }

    @Override //Method that gets called on every location change
    public void onLocationChanged(Location location) {
        currloc = location;
        mHandler.obtainMessage(1).sendToTarget();

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

    public String unzipString(String zippedText) { //Not working right
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
    }

    public void checkForUpdate() {
        retrieveJSON retrieve = new retrieveJSON();
        retrieve.execute("latest");
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
        String JSONString = "";
        @Override
        protected String doInBackground(JSONObject... params) {
            StringBuffer response = new StringBuffer();
            try {
                String timestamp = Long.toString(System.currentTimeMillis() / 1000L);
                String token = timestamp + "$up4rK";
                String url = "http://supark.host-ed.me/index.php?do=addpark&token="+md5(token)+"&timestamp="+timestamp; //+"&gzip";
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
            //Log.i("Response",response.toString());
            //return unzipString(response.toString());
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("done")){
                mHandler.obtainMessage(3,1,0).sendToTarget();
                Log.i("Done", result);
            }else {
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
            StringBuffer response = new StringBuffer();
            try {
                String timestamp = Long.toString(System.currentTimeMillis() / 1000L);
                String token = timestamp + "$up4rK";
                String url = "http://supark.host-ed.me/index.php?do="+params[0]+"&token="+md5(token)+"&timestamp="+timestamp; //+"&gzip";
                Log.i("url",url);
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }catch (Exception e) {
                Log.i("Exception", e.toString());
            }
            //Log.i("Response",response.toString());
            //return unzipString(response.toString());
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            int i;
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
                }
                if(data.has("zones")){
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
                    lastupdate = sharedprefs.getInt("lastupdate",0);
                }
            }catch (Exception e){
                Log.i("Error",e.toString());
            }
            Log.i("lastupdate",Integer.toString(lastupdate));
            super.onPostExecute(result);
        }
    }
}