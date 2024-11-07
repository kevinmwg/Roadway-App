package com.glalintechnologies.roadway;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationService extends Service {

    public static final String ACTION_START = "com.glalintechnologies.roadway.LocationService.START";
    public static final String ACTION_STOP = "com.glalintechnologies.roadway.LocationService.STOP";
    private static final long LOCATION_UPDATE_INTERVAL = 10000; // 10 seconds
    private static final String CHANNEL_ID = "location_channel";

    private FusedLocationProviderClient locationProviderClient;
    private LocationCallback locationCallback;
    private DatabaseReference databaseReference;
    private String operatorId;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("operators");
        operatorId = "your_unique_operator_id"; // Set this to a fixed ID or retrieve it dynamically

        // Set up location client and callback
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocationInDatabase(location.getLatitude(), location.getLongitude());
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                startForegroundService();
            } else if (ACTION_STOP.equals(action)) {
                stopForegroundService();
            }
        }
        return START_NOT_STICKY;
    }

    private void startForegroundService() {
        // Create notification channel and start service as foreground immediately
        createNotificationChannel();
        startForeground(1, createNotification());

        // Check for location permissions after starting foreground
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        // Start location updates
        startLocationUpdates();
    }

    private void stopForegroundService() {
        locationProviderClient.removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateLocationInDatabase(double latitude, double longitude) {
        if (operatorId != null) {
            databaseReference.child(operatorId).child("location").child("latitude").setValue(latitude);
            databaseReference.child(operatorId).child("location").child("longitude").setValue(longitude);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationProviderClient.removeLocationUpdates(locationCallback);
    }

    // Method to create a notification for foreground service
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Tracking location in background")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    // Method to create a notification channel for devices running Android Oreo and above
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}









