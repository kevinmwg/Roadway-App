package com.glalintechnologies.roadway2;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private MapView mapView;
    private Switch locationToggle;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference databaseReference;
    private GeoFire geoFire;
    private Marker currentLocationMarker;

    private static final String OPERATOR_ID = "operator_123"; // Replace with dynamic ID if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Realtime Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("operators_locations");
        geoFire = new GeoFire(databaseReference);

        // Initialize views
        mapView = findViewById(R.id.mapView);
        locationToggle = findViewById(R.id.locationToggle);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // MapView setup
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize BottomNavigationView and set listener
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.nav_home) {
                return true; // Stay on the current activity
            } else if (item.getItemId() == R.id.nav_dashboard) {
                intent = new Intent(MainActivity.this, nav_dashboard.class); // Replace with your dashboard activity
            } else if (item.getItemId() == R.id.nav_services) {
                intent = new Intent(MainActivity.this, nav_services.class); // Replace with your services activity
            }
            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Show user type selection dialog
        showUserTypeDialog();

        // Toggle listener for location sharing
        locationToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startLocationSharing();
            } else {
                stopLocationSharing();
            }
        });
    }

    private void showUserTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select User Type");
        builder.setMessage("Are you a Client, a New Operator, or an Existing Operator?");
        builder.setCancelable(false);
        builder.setPositiveButton("Client", (dialog, which) -> {
            // Redirect to Dashboard
            Intent intent = new Intent(MainActivity.this, nav_dashboard.class);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("New Operator", (dialog, which) -> {
            // Redirect to Registration Form
            Intent intent = new Intent(MainActivity.this, OperatorsregistrationActivity.class); // Replace with your registration activity
            startActivity(intent);
            finish();
        });
        builder.setNeutralButton("Existing Operator", (dialog, which) -> {
            // Stay in current activity
            Toast.makeText(MainActivity.this, "Please enable location sharing to proceed.", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void startLocationSharing() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        // Get location updates and save to Firebase
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, this::updateLocation);
    }

    private void updateLocation(Location location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            // Update marker on map
            if (currentLocationMarker != null) {
                currentLocationMarker.remove();
            }
            currentLocationMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Operator Location"));
            googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15));

            // Update location in Firebase using GeoFire
            geoFire.setLocation(OPERATOR_ID, new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> {
                if (error != null) {
                    Toast.makeText(MainActivity.this, "Error saving location: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Location shared successfully", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Unable to fetch location. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationSharing() {
        // Remove location from Firebase
        geoFire.removeLocation(OPERATOR_ID, (key, error) -> {
            if (error != null) {
                Toast.makeText(MainActivity.this, "Error removing location: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Location sharing stopped", Toast.LENGTH_SHORT).show();
            }
        });

        // Remove marker from map
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
            currentLocationMarker = null;
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationSharing();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}