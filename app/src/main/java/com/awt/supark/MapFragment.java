package com.awt.supark;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;


/**
 * Created by Doctor on 2015.10.29..
 */
public class MapFragment extends Fragment {

    private static View view;
    private static GoogleMap mMap;
    private static Double latitude, longitude;
    SQLiteDatabase db;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null; //For if Google Services is missing
        }
        context = getActivity().getApplicationContext();
        view = (RelativeLayout) inflater.inflate(R.layout.map_layout, container, false);
        latitude = 46.1;
        longitude = 19.67;

        setUpMapIfNeeded(); // For setting up the MapFragment

        return view;
    }

    public boolean getPolys() {
        try {
            db = SQLiteDatabase.openDatabase(context.getFilesDir().getPath() + "/ParkingDB.db", null, SQLiteDatabase.CREATE_IF_NECESSARY);
            Cursor d = db.rawQuery("SELECT * FROM regions", null);
            String[] poly = new String[d.getCount()];
            int[] polycolor = new int[d.getCount()];
            int polynum = 0;
            for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
                int polyindex = d.getColumnIndex("location_poly");
                int zoneindex = d.getColumnIndex("zone_id");
                poly[polynum] = d.getString(polyindex);
                polycolor[polynum] = d.getInt(zoneindex);
                Log.i("Poly", poly[polynum]);
                polynum++;
            }

            Polygon polygons[] = new Polygon[polynum];

            for (int i = 0; i < polynum; i++) {
                poly[i] = poly[i].replace("POLYGON((", "");
                poly[i] = poly[i].replace("))", "");
                Log.i("Poly", poly[i]);
                String vertices[] = poly[i].split(",");
                PolygonOptions polygon = new PolygonOptions();
                for (int j = 0; j < vertices.length; j++) {
                    String verticelatlng[] = vertices[j].split(" ");
                    polygon.add(new LatLng(Double.valueOf(verticelatlng[0]), Double.valueOf(verticelatlng[1])));
                    //Log.i("PolygonOptions",Double.toString(Double.valueOf(verticelatlng[0]))+", "+Double.toString(Double.valueOf(verticelatlng[1])));
                }
                int color = Color.RED;
                switch (polycolor[i]) {
                    case 1:
                        color = Color.RED;
                        break;
                    case 2:
                        color = Color.YELLOW;
                        break;
                    case 3:
                        color = Color.GREEN;
                        break;
                    case 4:
                        color = Color.BLUE;
                        break;
                }
                polygon.strokeColor(color);
                polygon.fillColor(color);
                polygons[i] = mMap.addPolygon(polygon);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Update DB! (Open ETC Fragment to update!)", Toast.LENGTH_SHORT).show();
            Log.i("Exception", e.toString());
            return false;
        }


        return true;

    }

    /***** Sets up the map if it is possible to do so *****/
    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null)
                setUpMap();
        }
    }

    private void setUpMap() {
        // For showing a move to my loction button
        mMap.setMyLocationEnabled(true);
        LatLng location = new LatLng(latitude,longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,13f));
        getPolys();
    }

    /**** The mapfragment's id must be removed from the FragmentManager
     **** or else if the same it is passed on the next time then
     **** app will crash ****/
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMap != null) {
            getChildFragmentManager().beginTransaction().remove(getChildFragmentManager().findFragmentById(R.id.map)).commitAllowingStateLoss();
            mMap = null;
        }
    }
}
