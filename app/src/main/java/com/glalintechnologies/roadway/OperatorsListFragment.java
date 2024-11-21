package com.glalintechnologies.roadway;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;

public class OperatorsListFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private ClusterManager<ClusterMarker> clusterManager;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("operators");

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Set initial map position
        LatLng initialPosition = new LatLng(-1.286389, 36.817223); // Nairobi, Kenya
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 10));

        // Initialize ClusterManager
        clusterManager = new ClusterManager<>(requireContext(), googleMap);

        // Set custom renderer
        IconGenerator iconGenerator = new IconGenerator(requireContext());
        MyClusterManagerRenderer renderer = new MyClusterManagerRenderer(
                requireContext(), googleMap, clusterManager, iconGenerator, 100, 100);
        clusterManager.setRenderer(renderer);

        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);

        // Fetch data from Firebase
        fetchOperatorsData();
    }

    private void fetchOperatorsData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clusterManager.clearItems(); // Clear existing items

                for (DataSnapshot operatorSnapshot : dataSnapshot.getChildren()) {
                    try {
                        // Parse operator object
                        Operator operator = operatorSnapshot.getValue(Operator.class);

                        // Get location data
                        DataSnapshot locationSnapshot = operatorSnapshot.child("locations").child("l");
                        if (locationSnapshot.exists() && locationSnapshot.getChildrenCount() == 2) {
                            double lat = locationSnapshot.child("0").getValue(Double.class);
                            double lng = locationSnapshot.child("1").getValue(Double.class);

                            // Add ClusterMarker
                            ClusterMarker clusterMarker = new ClusterMarker(
                                    requireContext(),
                                    lat,
                                    lng,
                                    operator.getOperatorName(),
                                    operator.getDescription(),
                                    R.layout.ic_marker,// Replace with your marker icon resource
                                    operator
                            );
                            clusterManager.addItem(clusterMarker);
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // Log parsing errors
                    }
                }

                clusterManager.cluster(); // Refresh the clusters
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
}

