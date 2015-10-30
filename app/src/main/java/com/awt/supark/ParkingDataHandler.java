package com.awt.supark;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;

/**
 * Created by Doctor on 27/10/2015.
 */
public class ParkingDataHandler {
    SQLiteDatabase db;
    SharedPreferences sharedprefs;
    int lastupdate;
    Context context;

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
                "  `lat1` double NOT NULL,\n" +
                "  `lon1` double NOT NULL,\n" +
                "  `lat2` double NOT NULL,\n" +
                "  `lon2` double NOT NULL,\n" +
                "  PRIMARY KEY (`region_id`)\n" +
                ")");
        sharedprefs = context.getSharedPreferences("ParkingPrefs",Context.MODE_PRIVATE);
        lastupdate = sharedprefs.getInt("lastupdate",0);
        Log.i("lastupdate",Integer.toString(lastupdate));
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

    public class retrieveJSON extends AsyncTask<String,Void,String>{
        String JSONString = "";
        @Override
        protected String doInBackground(String... params) {
            StringBuffer response = new StringBuffer();
            try {
                String timestamp = Long.toString(System.currentTimeMillis() / 1000L);
                String token = timestamp + "$up4rK";
                String url = "http://192.168.1.150/index.php?do="+params[0]+"&token="+md5(token)+"&timestamp="+timestamp; //+"&gzip";
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
                        values_temp.put("lat1",regions_temp.getDouble("lat1"));
                        values_temp.put("lon1",regions_temp.getDouble("lon1"));
                        values_temp.put("lat2",regions_temp.getDouble("lat2"));
                        values_temp.put("lon2", regions_temp.getDouble("lon2"));
                        db.insert("regions",null,values_temp);
                    }
                    int lastupd = data.getJSONObject("lastupdate").getInt("time");
                    sharedprefs.edit().putInt("lastupdate",lastupd);
                    sharedprefs.edit().apply();
                    lastupdate = lastupd;
                }
            }catch (Exception e){
                Log.i("Error",e.toString());
            }
            Log.i("lastupdate",Integer.toString(lastupdate));
            super.onPostExecute(result);
        }
    }
}
