package com.glalintechnologies.roadway2;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "LocationServiceChannel";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GeoFire geoFire;
    private DatabaseReference geoFireRef;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!isUserAuthenticated()) {
            Log.e(TAG, "No user is authenticated. Stopping the service.");
            stopSelf();
            return;
        }

        initializeGeoFire();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = createLocationRequest();

        initializeLocationCallback();

        createNotificationChannel();
        startForegroundNotification();

        startLocationUpdates(locationRequest);
    }

    private boolean isUserAuthenticated() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void initializeGeoFire() {
        geoFireRef = FirebaseDatabase.getInstance().getReference("geolocations");
        geoFire = new GeoFire(geoFireRef);
    }

    private LocationRequest createLocationRequest() {
        return LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void initializeLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null || locationResult.getLocations().isEmpty()) {
                    Log.w(TAG, "No location result received.");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateLocationToGeoFire(location);
                    }
                }
            }
        };
    }

    private void startForegroundNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service Active")
                .setContentText("Your location is being tracked.")
                .setSmallIcon(R.drawable.excavator2) // Replace with your icon
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    private void startLocationUpdates(LocationRequest locationRequest) {
        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permissions are not granted. Stopping service.");
            stopSelf();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void updateLocationToGeoFire(Location location) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.e(TAG, "User ID is null. Cannot update location.");
            return;
        }
        geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> {
            if (error != null) {
                Log.e(TAG, "Error updating location to GeoFire: " + error.getMessage());
            } else {
                Log.i(TAG, "Location updated successfully for user: " + userId);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        Log.i(TAG, "Location Service destroyed.");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Channel for location tracking service");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

