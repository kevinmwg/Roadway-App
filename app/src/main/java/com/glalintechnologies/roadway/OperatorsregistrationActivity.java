package com.glalintechnologies.roadway;

import android.Manifest;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
    private Handler locationHandler;
    private Runnable locationRunnable;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long LOCATION_UPDATE_INTERVAL = 10000; // 10 seconds
    private String operatorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operatorsregistration);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("operators");

        // Initialize Location Provider
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize views
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

        // Set up location request and callback
        setupLocationTracking();

        btnRegisterVehicle.setOnClickListener(view -> {
            // Inform user about location collection
            Snackbar.make(view, "Location tracking enabled to help connect with clients", Snackbar.LENGTH_LONG).show();
            storeOperatorData();
            requestLocationPermission();
            startLocationService(); // Start the LocationService in the background
        });
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void storeOperatorData() {
        String operatorName = etOperatorName.getText() != null ? etOperatorName.getText().toString().trim() : "";
        String contactNumber = etContactNumber.getText() != null ? etContactNumber.getText().toString().trim() : "";
        String vehicleModel = etVehicleModel.getText() != null ? etVehicleModel.getText().toString().trim() : "";
        String licensePlate = etLicensePlate.getText() != null ? etLicensePlate.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String emergencyContact = etEmergencyContact.getText() != null ? etEmergencyContact.getText().toString().trim() : "";
        String vehicleType = spinnerVehicleType.getSelectedItem() != null ? spinnerVehicleType.getSelectedItem().toString() : "";
        String serviceType = spinnerServiceType.getSelectedItem() != null ? spinnerServiceType.getSelectedItem().toString() : "";
        boolean isActive = switchActiveStatus.isChecked();

        // Ensure required fields are filled out
        if (operatorName.isEmpty() || contactNumber.isEmpty() || vehicleModel.isEmpty() || licensePlate.isEmpty()) {
            Snackbar.make(findViewById(R.id.btn_register_vehicle), "Please fill out all required fields", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Create a map to store data
        Map<String, Object> operatorData = new HashMap<>();
        operatorData.put("operatorName", operatorName);
        operatorData.put("contactNumber", contactNumber);
        operatorData.put("vehicleModel", vehicleModel);
        operatorData.put("licensePlate", licensePlate);
        operatorData.put("description", description);
        operatorData.put("emergencyContact", emergencyContact);
        operatorData.put("vehicleType", vehicleType);
        operatorData.put("serviceType", serviceType);
        operatorData.put("isActive", isActive);

        // Store data in Firebase Realtime Database under unique ID
        if (operatorId != null) {
            databaseReference.child(operatorId).setValue(operatorData)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Data stored successfully"))
                    .addOnFailureListener(e -> Log.e("Firebase", "Error storing data", e));
        } else {
            Log.e("Firebase", "Operator ID is null");
        }
    }

    private void setupLocationTracking() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocationInDatabase(location.getLatitude(), location.getLongitude());
                }
            }
        };

        locationHandler = new Handler();
        locationRunnable = () -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
            locationHandler.postDelayed(locationRunnable, LOCATION_UPDATE_INTERVAL);
        };
    }

    private void updateLocationInDatabase(double latitude, double longitude) {
        if (operatorId != null) {
            databaseReference.child(operatorId).child("location").child("latitude").setValue(latitude);
            databaseReference.child(operatorId).child("location").child("longitude").setValue(longitude);
        } else {
            Log.e("Firebase", "Operator ID is null, unable to update location");
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationTracking();
        }
    }

    private void startLocationTracking() {
        locationHandler.post(locationRunnable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationTracking();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationProviderClient.removeLocationUpdates(locationCallback);
        locationHandler.removeCallbacks(locationRunnable);
    }
}


