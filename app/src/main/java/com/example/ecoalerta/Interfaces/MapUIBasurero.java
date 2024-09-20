package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_ui_basurero);

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
                        lastMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación actual").icon(icon));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationService.stopLocationUpdates(); // Detener actualizaciones al destruir la actividad
    }

    @Override
    protected void onStop() {
        super.onStop();
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            updateStatus(username);
        }
    }

    private void updateStatus(String username) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // URL del archivo PHP
                    URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/update_status.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoOutput(true);

                    // Enviar el nombre de usuario al servidor
                    String postData = "username=" + URLEncoder.encode(username, "UTF-8");
                    OutputStream os = connection.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();

                    // Leer la respuesta del servidor
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Éxito
                    } else {
                        // Error
                    }

                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
        locationService.startLocationUpdates();
    }

    @Override
    public void onBackPressed() {
        // Obtener el nombre del usuario (esto depende de cómo lo estés manejando en tu aplicación)
        String userName = username; // Reemplaza esto con el nombre real del usuario

        // Mostrar la pantalla de carga antes de volver a UserUI
        Intent cargaIntent = new Intent(MapUIBasurero.this, CargaUI.class);
        startActivity(cargaIntent);

        // Usar Handler para retrasar el inicio de UserUI y permitir que la pantalla de carga se muestre
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Asegúrate de que la pantalla de carga esté cerrada antes de iniciar UserUI
                Intent intent = new Intent(MapUIBasurero.this, BasureroUI.class);
                // Agregar el nombre del usuario al Intent
                intent.putExtra("username", userName);

                startActivity(intent);
                finish(); // Cerrar la actividad actual para que el usuario no vuelva a ella
            }
        }, 500); // Esperar 500 ms antes de iniciar UserUI
    }
}