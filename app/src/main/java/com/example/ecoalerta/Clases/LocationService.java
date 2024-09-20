package com.example.ecoalerta.Clases;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationService {

    private static final String DOCUMENT_ID = "jnglikr1u9WHig5DGW2B";
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private Context context;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public LocationService(Context context, FusedLocationProviderClient fusedLocationClient, FirebaseFirestore db) {
        this.context = context;
        this.fusedLocationClient = fusedLocationClient;
        this.db = db;

        // Configurar LocationRequest
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000); // Actualiza cada 3 segundos
        locationRequest.setFastestInterval(1000); // El intervalo más rápido
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Configurar LocationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateFirestoreLocation(location.getLatitude(), location.getLongitude());
                }
            }
        };
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // No permisos
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void updateFirestoreLocation(double latitude, double longitude) {
        List<Double> coordinates = Arrays.asList(latitude, longitude);
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("ubicacion", coordinates);

        db.collection("camion_locations").document(DOCUMENT_ID)
                .set(locationData)
                .addOnSuccessListener(aVoid -> System.out.println("Location successfully written!"))
                .addOnFailureListener(e -> System.err.println("Error writing location: " + e));
    }
}
