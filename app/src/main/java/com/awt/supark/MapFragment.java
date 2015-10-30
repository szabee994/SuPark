package com.awt.supark;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



/**
 * Created by Doctor on 2015.10.29..
 */
public class MapFragment extends Fragment {

    private static View view;
    private static GoogleMap mMap;
    private static Double latitude, longitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null; //For if Google Services is missing
        }
        view = (RelativeLayout) inflater.inflate(R.layout.map_layout, container, false);
        latitude = 46.1;
        longitude = 19.67;

        setUpMapIfNeeded(); // For setting up the MapFragment

        return view;
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

    private static void setUpMap() {
        // For showing a move to my loction button
        mMap.setMyLocationEnabled(true);
        LatLng location = new LatLng(latitude,longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,13f));
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
