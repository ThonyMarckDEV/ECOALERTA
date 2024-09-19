package com.example.ecoalerta.Clases;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecoalerta.Interfaces.MapUI;


/**
    *Esta clase maneja la configuración y solicitud de permisos para el GPS.
 * */
public class LocationHelper {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public static boolean checkLocationPermission(AppCompatActivity activity) {
        return activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(AppCompatActivity activity) {
        activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    public static void handlePermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, MapUI activity) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                ((MapUI) activity).onLocationPermissionGranted();
            }
        }
    }
}
