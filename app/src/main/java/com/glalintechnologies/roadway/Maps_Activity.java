package com.glalintechnologies.roadway;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class Maps_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Example coordinate array
        String[] coordinates = {"-1.2921,36.8219", "-1.286389,36.817223", "-1.28333,36.81667"};

        // Parse coordinates and create LatLng list
        List<LatLng> latLngList = new ArrayList<>();
        for (String coord : coordinates) {
            String[] parts = coord.split(",");
            double lat = Double.parseDouble(parts[0]);
            double lng = Double.parseDouble(parts[1]);
            LatLng latLng = new LatLng(lat, lng);
            latLngList.add(latLng);

            // Add marker for each coordinate
            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker at: " + coord));
        }

        // Add polyline connecting the points
        //mMap.addPolyline(new PolylineOptions().addAll(latLngList).width(5).color(0xFF0000FF));

        // Move the camera to the first coordinate
        if (!latLngList.isEmpty()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(0), 14));
        }
    }
}
