package com.example.hoply;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment {

    double latitude;
    double longitude;

    /**
     * Constructor for making the mapfragment which displays the google map. Added extra arguments to
     * take the lat and long of a location
     * @param latitude the latitude of a location
     * @param longitude the longitude of a location
     */
    public MapFragment(double latitude, double longitude){
        super();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize view
        View view=inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize map fragment
        SupportMapFragment supportMapFragment=(SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);

        // Actions to take when map is ready
        supportMapFragment.getMapAsync(googleMap -> {
            int nightModeFlags =  view.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES)
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.darkmap));
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            LatLng latLng = new LatLng(latitude, longitude);
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,13 ));

        });
        // Return view
        return view;
    }
}