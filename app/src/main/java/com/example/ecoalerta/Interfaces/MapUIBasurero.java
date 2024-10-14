package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ecoalerta.Clases.LocationForegroundService;
import com.example.ecoalerta.Clases.LocationHelper;
import com.example.ecoalerta.Clases.LocationService;
import com.example.ecoalerta.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class MapUIBasurero extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private LocationService locationService;
    private GoogleMap mMap;
    private Marker lastMarker;
    private MapView mapView;
    private String username;
    private LatLng previousLocation = null; // Guardar la ubicación anterior del camión

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map_ui_basurero);

        //===================================================================================
        /**
         * VERIRICADOR DE SESION CADA 10 SEGUNDOS
         */
        // Crear instancia del verificador de estado
        EstadoUsuarioVerificador verificador = new EstadoUsuarioVerificador(this);

        // Iniciar el ciclo de verificación del estado del usuario
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                verificador.verificarEstado();
                handler.postDelayed(this, 10000); // Ejecutar cada 3 segundos
            }
        };
        handler.post(runnable);
        //===================================================================================

        // Obtener el username del Intent
        username = getIntent().getStringExtra("username");

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        locationService = new LocationService(this, fusedLocationClient, db);

        // Iniciar actualizaciones de ubicación
        locationService.startLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMap(); // Actualizar el mapa la primera vez cuando esté listo
    }

    private void updateMap() {
        // Cargar la ubicación desde Firestore y actualizar el mapa
        db.collection("camion_locations").document("jnglikr1u9WHig5DGW2B")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) {
                        return;
                    }

                    List<Double> location = (List<Double>) snapshot.get("ubicacion");
                    if (location != null) {
                        LatLng newLocation = new LatLng(location.get(0), location.get(1));

                        // Calcular la diferencia entre la ubicación actual y la anterior para determinar la dirección
                        if (previousLocation != null) {
                            double deltaLat = newLocation.latitude - previousLocation.latitude;
                            double deltaLng = newLocation.longitude - previousLocation.longitude;

                            // Determinar la dirección de movimiento y cambiar el ícono del camión
                            BitmapDescriptor icon;
                            if (Math.abs(deltaLat) > Math.abs(deltaLng)) {
                                if (deltaLat > 0) {
                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.camion_arriba);
                                } else {
                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.camion_abajo);
                                }
                            } else {
                                if (deltaLng > 0) {
                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.camion_derecha);
                                } else {
                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.camion_izquierda);
                                }
                            }

                            // Detectar movimiento diagonal y actualizar el ícono si es necesario
                            if (Math.abs(deltaLat) > 0 && Math.abs(deltaLng) > 0) {
                                if (deltaLat > 0 && deltaLng > 0) {
                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.camion_diagonal_arriba_derecha);
                                } else if (deltaLat > 0 && deltaLng < 0) {
                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.camion_diagonal_arriba_izquierda);
                                } else if (deltaLat < 0 && deltaLng > 0) {
                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.camion_diagonal_abajo_derecha);
                                } else if (deltaLat < 0 && deltaLng < 0) {
                                    icon = BitmapDescriptorFactory.fromResource(R.drawable.camion_diagonal_abajo_izquierda);
                                }
                            }

                            // Eliminar el último marcador si existe
                            if (lastMarker != null) {
                                lastMarker.remove();
                            }

                            // Agregar un nuevo marcador con el ícono actualizado
                            lastMarker = mMap.addMarker(new MarkerOptions().position(newLocation).title("Ubicación actual").icon(icon));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 18f));

                            // Actualizar la ubicación anterior
                            previousLocation = newLocation;
                        } else {
                            // Si no hay una ubicación anterior, inicialízala
                            previousLocation = newLocation;
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        // Detén el servicio en segundo plano cuando el usuario vuelva a la app
        Intent serviceIntent = new Intent(this, LocationForegroundService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Inicia el servicio en segundo plano cuando la actividad pasa a segundo plano
        Intent serviceIntent = new Intent(this, LocationForegroundService.class);
        startForegroundService(serviceIntent);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detén el servicio si la actividad se destruye
        Intent serviceIntent = new Intent(this, LocationForegroundService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LocationHelper.handlePermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void onLocationPermissionGranted() {
        locationService.startLocationUpdates();
    }

    @Override
    public void onBackPressed() {
        // Detén el servicio en segundo plano antes de salir
        Intent serviceIntent = new Intent(this, LocationForegroundService.class);
        stopService(serviceIntent);

        // Confirmar que el servicio se detiene correctamente
        new Handler().postDelayed(() -> {
            // Asegúrate de que el servicio se haya detenido correctamente
            stopService(serviceIntent);
            super.onBackPressed();
        }, 500); // Esperar un poco para asegurar que el servicio se detenga

        // Obtener el nombre del usuario
        String userName = username; // Reemplaza esto con el nombre real del usuario

        // Mostrar la pantalla de carga antes de volver a UserUI
        Intent cargaIntent = new Intent(MapUIBasurero.this, CargaUI.class);
        startActivity(cargaIntent);

        // Usar Handler para retrasar el inicio de UserUI y permitir que la pantalla de carga se muestre
        new Handler().postDelayed(() -> {
            // Asegúrate de que la pantalla de carga esté cerrada antes de iniciar UserUI
            Intent intent = new Intent(MapUIBasurero.this, BasureroUI.class);
            // Agregar el nombre del usuario al Intent
            intent.putExtra("username", userName);

            startActivity(intent);
            finish(); // Cerrar la actividad actual para que el usuario no vuelva a ella
        }, 500); // Esperar 500 ms antes de iniciar UserUI
    }

    @Override
    protected void onStop() {
        super.onStop();
        //locationService.stopLocationUpdates(); // Detener actualizaciones al destruir la actividad
    }
}