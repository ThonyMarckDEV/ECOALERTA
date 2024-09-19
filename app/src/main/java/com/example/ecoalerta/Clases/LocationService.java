package com.example.ecoalerta.Clases;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Para manejar la obtención y actualización de la ubicación en firebase ok.
 */
public class LocationService {

    private static final String DOCUMENT_ID = "jnglikr1u9WHig5DGW2B";
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private Context context;

    public LocationService(Context context, FusedLocationProviderClient fusedLocationClient, FirebaseFirestore db) {
        this.context = context;
        this.fusedLocationClient = fusedLocationClient;
        this.db = db;
    }

    public void updateLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permisos no concedidos, puedes solicitar permisos aquí si es necesario
            return;
        }
        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                List<Double> coordinates = Arrays.asList(latitude, longitude);

                Map<String, Object> locationData = new HashMap<>();
                locationData.put("ubicacion", coordinates);

                db.collection("camion_locations").document(DOCUMENT_ID)
                        .set(locationData)
                        .addOnSuccessListener(aVoid ->
                                System.out.println("Location successfully written!"))
                        .addOnFailureListener(e ->
                                System.err.println("Error writing location: " + e));
            }
        });
    }
}
