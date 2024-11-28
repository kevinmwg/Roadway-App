package com.glalintechnologies.roadway2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class OperatorsregistrationActivity extends AppCompatActivity {

    private EditText etOperatorName, etContactNumber, etVehicleModel, etLicensePlate, etDescription, etEmergencyContact;
    private Spinner spinnerVehicleType, spinnerServiceType;
    private Switch switchActiveStatus;
    private Button btnRegisterVehicle;

    private FusedLocationProviderClient locationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private DatabaseReference databaseReference;
    private GeoFire geoFire;
    private Handler locationHandler;
    private Runnable locationRunnable;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long LOCATION_UPDATE_INTERVAL = 10000; // 10 seconds
    private String operatorId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operatorsregistration); // Ensure this layout file exists

        // Initialize Firebase Database and GeoFire
        databaseReference = FirebaseDatabase.getInstance().getReference("operators");
        geoFire = new GeoFire(databaseReference.child("locations"));

        // Initialize Location Provider
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Views
        etOperatorName = findViewById(R.id.et_operator_name);
        etContactNumber = findViewById(R.id.et_contact_number);
        etVehicleModel = findViewById(R.id.et_vehicle_model);
        etLicensePlate = findViewById(R.id.et_license_plate);
        etDescription = findViewById(R.id.et_description);
        etEmergencyContact = findViewById(R.id.et_emergency_contact);
        spinnerVehicleType = findViewById(R.id.spinner_vehicle_type);
        spinnerServiceType = findViewById(R.id.spinner_service_type);
        switchActiveStatus = findViewById(R.id.switch_active_status);
        btnRegisterVehicle = findViewById(R.id.btn_register_vehicle);

        // Generate unique ID for the operator
        operatorId = databaseReference.push().getKey();

        // Set up Location Tracking
        setupLocationTracking();

        // Button Click Listener
        btnRegisterVehicle.setOnClickListener(view -> {
            showAuthenticationDialog(); // Prompt user for authentication
            storeOperatorData(); // Save operator details to Firebase
            requestLocationPermission(); // Request location permissions
            startLocationService(); // Start location service for real-time updates
        });
    }

    // Starts the background location tracking service
    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    // Stores operator data in Firebase Database
    private void storeOperatorData() {
        // Retrieve input values with trimming
        String operatorName = etOperatorName.getText().toString().trim();
        String contactNumber = etContactNumber.getText().toString().trim();
        String vehicleModel = etVehicleModel.getText().toString().trim();
        String licensePlate = etLicensePlate.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String emergencyContact = etEmergencyContact.getText().toString().trim();
        String vehicleType = spinnerVehicleType.getSelectedItem() != null ? spinnerVehicleType.getSelectedItem().toString() : "Unknown";
        String serviceType = spinnerServiceType.getSelectedItem() != null ? spinnerServiceType.getSelectedItem().toString() : "Unknown";
        boolean isActive = switchActiveStatus != null && switchActiveStatus.isChecked();

        // Validate required fields
        if (operatorName.isEmpty() || contactNumber.isEmpty() || vehicleModel.isEmpty() || licensePlate.isEmpty()) {
            Snackbar.make(findViewById(R.id.btn_register_vehicle), "Please fill out all required fields", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Prepare data to store
        Map<String, Object> operatorData = new HashMap<>();
        operatorData.put("operatorName", operatorName);
        operatorData.put("contactNumber", contactNumber);
        operatorData.put("vehicleModel", vehicleModel);
        operatorData.put("licensePlate", licensePlate);
        operatorData.put("description", description != null ? description : "No description provided");
        operatorData.put("emergencyContact", emergencyContact != null ? emergencyContact : "No emergency contact provided");
        operatorData.put("vehicleType", vehicleType);
        operatorData.put("serviceType", serviceType);
        operatorData.put("isActive", isActive);

        // Ensure operatorId is valid before writing to the database
        if (operatorId != null && !operatorId.isEmpty()) {
            databaseReference.child(operatorId).setValue(operatorData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "Data stored successfully");
                        Snackbar.make(findViewById(R.id.btn_register_vehicle), "Registration successful!", Snackbar.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Error storing data", e);
                        Snackbar.make(findViewById(R.id.btn_register_vehicle), "Error storing data: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("Firebase", "Operator ID is null or empty");
            Snackbar.make(findViewById(R.id.btn_register_vehicle), "Error: Operator ID is missing", Snackbar.LENGTH_SHORT).show();
        }
    }


    // Sets up location tracking using GeoFire
    private void setupLocationTracking() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Fallback to a lower priority if high accuracy is unavailable
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocationInDatabase(location.getLatitude(), location.getLongitude());
                } else {
                    Log.w("LocationCallback", "Location is null");
                }
            }
        };

        locationHandler = new Handler();
        locationRunnable = () -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
                        .addOnSuccessListener(unused -> Log.d("LocationTracking", "Location updates started"))
                        .addOnFailureListener(e -> Log.e("LocationTracking", "Failed to start location updates", e));
            } else {
                Log.e("LocationTracking", "Missing location permissions");
            }
            locationHandler.postDelayed(locationRunnable, LOCATION_UPDATE_INTERVAL);
        };

        // Immediately request location updates once permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            requestLocationPermission(); // Ensure permissions are requested if not already granted
        }
    }

    // Updates operator's location in Firebase using GeoFire
    private void updateLocationInDatabase(double latitude, double longitude) {
        if (operatorId != null && !operatorId.isEmpty()) {
            geoFire.setLocation(operatorId, new GeoLocation(latitude, longitude), (key, error) -> {
                if (error != null) {
                    Log.e("GeoFire", "Error saving location: ", error.toException());
                } else {
                    Log.d("GeoFire", "Location saved successfully");
                }
            });
        } else {
            Log.e("GeoFire", "Operator ID is null or empty; cannot save location");
        }
    }


    // Requests location permissions
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Handles permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(findViewById(R.id.btn_register_vehicle), "Location Permission Granted", Snackbar.LENGTH_SHORT).show();
        }
    }

    // Shows the authentication dialog
    private void showAuthenticationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Registration")
                .setMessage("Please authenticate to complete the registration.")
                .setPositiveButton("Authenticate", (dialogInterface, i) -> {
                    Intent authIntent = new Intent(OperatorsregistrationActivity.this, RoadwayAuth.class);
                    startActivity(authIntent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    Snackbar.make(findViewById(R.id.btn_register_vehicle), "Registration cancelled", Snackbar.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }
}








