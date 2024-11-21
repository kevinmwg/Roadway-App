package com.glalintechnologies.roadway;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GeoFire geoFire;
    private DatabaseReference geoFireRef;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase Realtime Database reference for GeoFire
        geoFireRef = FirebaseDatabase.getInstance().getReference("geolocations");
        geoFire = new GeoFire(geoFireRef);

        // Set up location client and request
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)  // 5 seconds interval
                .setFastestInterval(3000) // 3 seconds fastest interval
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Define location callback to receive location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateLocationToGeoFire(location);
                    }
                }
            }
        };

        // Start foreground notification
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service Active")
                .setContentText("Your location is being tracked")
                .setSmallIcon(R.drawable.excavator2) // Add your icon here
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);


        // Start location updates
        startLocationUpdates(locationRequest);
    }

    // Start location updates
    private void startLocationUpdates(LocationRequest locationRequest) {
        if (fusedLocationClient != null && locationCallback != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    // Stop location updates
    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    // Update location to Firebase Realtime Database using GeoFire
    private void updateLocationToGeoFire(Location location) {
        String userId = "user_id"; // Replace with actual user ID
        geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()),
                (key, error) -> {
                    if (error != null) {
                        Log.e(TAG, "GeoFire location update failed: " + error.getMessage());
                    } else {
                        Log.d(TAG, "GeoFire location updated for user: " + userId);
                    }
                });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Create notification channel for Android O and higher
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}











