package com.glalintechnologies.roadway;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {
    private LatLng position;        // Marker position (latitude, longitude)
    private String title;           // Marker title (Operator name)
    private String snippet;         // Marker snippet (Operator details)
    private BitmapDescriptor icon;  // Marker icon (from drawable resource)
    private Operator operator;      // Associated Operator object
    private Context context;        // Context for generating custom views

    // Main Constructor
    public ClusterMarker(Context context, double lat, double lng, String operatorName, String description, int iconResId, Operator operator) {
        this.context = context;
        this.position = new LatLng(lat, lng);
        this.title = operatorName != null ? operatorName : "Unknown Operator";
        this.snippet = description != null ? description : "No details available";
        this.icon = BitmapDescriptorFactory.fromResource(iconResId); // Convert drawable to BitmapDescriptor
        this.operator = operator;
    }

    // Constructor that takes Operator object and icon resource
    public ClusterMarker(Context context, Operator operator, int drawableResId) {
        this.context = context;

        if (operator != null && operator.getLocations() != null && operator.getLocations().getL() != null) {
            double lat = operator.getLocations().getLatitude();
            double lng = operator.getLocations().getLongitude();
            this.position = new LatLng(lat, lng);
        } else {
            this.position = new LatLng(0, 0); // Default to (0,0) if location data is missing
        }

        this.title = operator != null ? operator.getOperatorName() : "Unknown Operator";
        this.snippet = operator != null ? operator.getDescription() : "No details available";
        this.icon = BitmapDescriptorFactory.fromResource(drawableResId); // Convert drawable to BitmapDescriptor
        this.operator = operator;
    }

    // Override methods from ClusterItem
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return null;
    }

    // Getters
    public BitmapDescriptor getIcon() {
        return icon;
    }

    public Operator getOperator() {
        return operator;
    }

    // Setters
    public void setPosition(LatLng position) {
        this.position = position;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public void setIcon(Context context, int drawableResId) {
        this.icon = BitmapDescriptorFactory.fromResource(drawableResId);
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    // Custom view for the marker
    public View getMarkerView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_marker_view, null); // Replace with your custom marker layout

        // Customize the view with data
        TextView titleView = view.findViewById(R.id.marker_title);
        TextView snippetView = view.findViewById(R.id.marker_snippet);
        ImageView iconView = view.findViewById(R.id.custom_marker_view);

        titleView.setText(title);
        snippetView.setText(snippet);
        iconView.setImageResource(R.drawable.ic_marker); // Replace with your marker icon resource

        return view;
    }
}



