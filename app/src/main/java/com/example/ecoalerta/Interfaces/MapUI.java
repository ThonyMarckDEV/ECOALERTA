package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecoalerta.Clases.LocationHelper;
import com.example.ecoalerta.Clases.LocationService;
import com.example.ecoalerta.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MapUI extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private LocationService locationService;
    private GoogleMap mMap;
    private Marker lastMarker;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_ui);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        locationService = new LocationService(this, fusedLocationClient, db); // Pasar el contexto aquí

        if (LocationHelper.checkLocationPermission(this)) {
            locationService.updateLocation();
        } else {
            LocationHelper.requestLocationPermission(this);
        }

        // Update location every 3 seconds
        final Handler handler = new Handler();
        final Runnable updateLocationTask = new Runnable() {
            @Override
            public void run() {
                locationService.updateLocation();
                handler.postDelayed(this, 3000); // Update every 3 seconds
            }
        };
        handler.post(updateLocationTask);

        // Obtener el username del Intent
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            // Usa el username aquí si es necesario
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Load location from Firestore and update map
        db.collection("camion_locations").document("jnglikr1u9WHig5DGW2B")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) {
                        return;
                    }
                    // Cargar el icono personalizado desde los recursos
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.camion_icono);

                    List<Double> location = (List<Double>) snapshot.get("ubicacion");
                    if (location != null) {
                        LatLng latLng = new LatLng(location.get(0), location.get(1));

                        // Eliminar el último marcador si existe
                        if (lastMarker != null) {
                            lastMarker.remove();
                        }

                        // Agregar un nuevo marcador
                        lastMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location").icon(icon));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                    }
                });
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
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LocationHelper.handlePermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void onLocationPermissionGranted() {
        locationService.updateLocation();
    }

    @Override
    public void onBackPressed() {
        // Mostrar la pantalla de carga antes de volver a LoginUI
        Intent cargaIntent = new Intent(MapUI.this, CargaUI.class);
        startActivity(cargaIntent);

        // Usar Handler para retrasar el inicio de LoginUI y permitir que la pantalla de carga se muestre
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Asegúrate de que la pantalla de carga esté cerrada antes de iniciar LoginUI
                Intent intent = new Intent(MapUI.this, LoginUI.class);
                startActivity(intent);
                finish(); // Cerrar la actividad actual para que el usuario no vuelva a ella
            }
        }, 500); // Esperar 500 ms antes de iniciar LoginUI

        // Opcional: Si quieres que el back button no vuelva a la actividad actual
        // finish(); // Descomenta esta línea si quieres cerrar la actividad actual
    }
}