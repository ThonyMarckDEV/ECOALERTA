package com.example.ecoalerta.Interfaces;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ecoalerta.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class MapUIUser extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final float PROXIMITY_RADIUS_METERS = 25; // Adjust as needed
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private GoogleMap mMap;
    private Marker basureroMarker;
    private MapView mapView;
    private LatLng currentUserLatLng;
    private boolean isInitialCameraPositionSet = false;
    private MediaPlayer proximitySoundPlayer;
    private boolean isSoundPlaying = false;
    private String username; // Añadir esta línea para guardar el nombre del usuario
    private boolean isLocationPromptShown = false;
    private Handler handlerubi; // Mover el Handler a nivel de clase
    private Runnable updateLocationTask; // Mover el Runnable a nivel de clase
    private boolean isUpdatingLocation = false; // Flag para controlar la actualización de ubicación
    private LatLng previousLocation = null;


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

        // Inicializar el Handler y Runnable para actualizar la ubicación
        handlerubi = new Handler();
        updateLocationTask = new Runnable() {
            @Override
            public void run() {
                if (mMap != null && isUpdatingLocation) { // Solo actualizar si se permite
                    updateUserLocation();
                    updateBasureroLocation();
                }
                handlerubi.postDelayed(this, 3000); // Update every 3 seconds
            }
        };
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

        // Obtener el nombre de usuario del Intent
        username = getIntent().getStringExtra("username");

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        // Load proximity sound
        proximitySoundPlayer = MediaPlayer.create(this, R.raw.notibasura); // Replace with your sound file
        proximitySoundPlayer.setLooping(true); // Set looping to true

        // Inicializar el Handler y Runnable para actualizar la ubicación
        handlerubi = new Handler();
        updateLocationTask = new Runnable() {
            @Override
            public void run() {
                if (mMap != null && isUpdatingLocation) { // Solo actualizar si se permite
                    updateUserLocation();
                    updateBasureroLocation();
                }
                handlerubi.postDelayed(this, 3000); // Update every 3 seconds
            }
        };
    }

    private void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (!isLocationPromptShown) { // Solo mostrar si no se ha mostrado antes
                Toast.makeText(this, "Por favor, activa la ubicación", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                isLocationPromptShown = true; // Marcar como mostrado
            }
        } else {
            // Si la ubicación está habilitada, actualizar la ubicación
            updateUserLocation();
            isLocationPromptShown = false; // Reiniciar para futuras verificaciones
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkLocationEnabled(); // Verificar si la ubicación está habilitada
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // Load basurero location from Firestore and update map
        updateBasureroLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        // Reiniciar el estado para mostrar el prompt de ubicación nuevamente si es necesario
        isLocationPromptShown = false;

        // Verificar si la ubicación está habilitada cada vez que se reanuda la actividad
        checkLocationEnabled();

        isUpdatingLocation = true; // Permitir actualización de ubicación al reanudar
        handlerubi.post(updateLocationTask); // Iniciar la tarea de actualización de ubicación
    }


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

        // Detener el sonido de proximidad si está sonando
        if (isSoundPlaying) {
            proximitySoundPlayer.pause();
            proximitySoundPlayer.seekTo(0); // Reiniciar el sonido
            isSoundPlaying = false; // Marcar como no sonando
        }

        // Detener la actualización de ubicación
        isUpdatingLocation = false; // No permitir actualizaciones mientras la actividad está en pausa
        handlerubi.removeCallbacks(updateLocationTask); // Detener el Handler
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    updateUserLocation();
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean emailSent = false; // Variable para controlar el envío del correo

    private void updateUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            currentUserLatLng = userLatLng;

                            // Set camera position only once
                            if (!isInitialCameraPositionSet) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserLatLng, 18f)); // Set zoom level here
                                isInitialCameraPositionSet = true;
                            }

                            // Check proximity to basurero
                            if (basureroMarker != null) {
                                LatLng basureroLatLng = basureroMarker.getPosition();
                                float[] results = new float[1];
                                Location.distanceBetween(userLatLng.latitude, userLatLng.longitude,
                                        basureroLatLng.latitude, basureroLatLng.longitude, results);
                                float distanceInMeters = results[0];

                                if (distanceInMeters <= PROXIMITY_RADIUS_METERS) {
                                    if (!isSoundPlaying) {
                                        proximitySoundPlayer.start();
                                        isSoundPlaying = true;
                                    }
                                    // Notify PHP script about proximity if email hasn't been sent yet
                                    if (!emailSent) {
                                        notifyProximity();
                                        emailSent = true; // Set flag to true to prevent further emails
                                    }
                                } else {
                                    if (isSoundPlaying) {
                                        proximitySoundPlayer.pause();
                                        proximitySoundPlayer.seekTo(0);
                                        isSoundPlaying = false;
                                    }
                                    // Reset emailSent flag if the user moves out of the proximity
                                    emailSent = false;
                                }
                            }
                        }else {
                            // Si no se obtiene una ubicación, puede ser que la ubicación esté desactivada
                            checkLocationEnabled();
                        }
                    });
        }
    }

    private void notifyProximity() {
        new Thread(() -> {
            try {
                // Obtener el nombre de usuario del Intent
                String username = getIntent().getStringExtra("username");

                // URL del archivo PHP
                URL url = new URL(ApiService.BASE_URL + "send_email.php"); // Cambia esta URL a la URL correcta
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
                    // Éxito: leer la respuesta del servidor
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Imprimir la respuesta del servidor en log
                    System.out.println("Respuesta del servidor: " + response.toString());
                } else {
                    System.out.println("Error en la conexión: Código " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateBasureroLocation() {
        db.collection("camion_locations").document("jnglikr1u9WHig5DGW2B")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Double> location = (List<Double>) task.getResult().get("ubicacion");
                        if (location != null) {
                            LatLng newLocation = new LatLng(location.get(0), location.get(1));

                            // Lógica para cambiar la imagen del camión
                            if (previousLocation != null) {
                                // Calcular la diferencia en latitud y longitud
                                double deltaLat = newLocation.latitude - previousLocation.latitude;
                                double deltaLng = newLocation.longitude - previousLocation.longitude;

                                // Determinar la dirección de movimiento
                                if (Math.abs(deltaLat) > Math.abs(deltaLng)) {
                                    if (deltaLat > 0) {
                                        // El camión se mueve hacia arriba
                                        updateCamionMarker(R.drawable.camion_arriba);
                                    } else {
                                        // El camión se mueve hacia abajo
                                        updateCamionMarker(R.drawable.camion_abajo);
                                    }
                                } else {
                                    if (deltaLng > 0) {
                                        // El camión se mueve hacia la derecha
                                        updateCamionMarker(R.drawable.camion_derecha);
                                    } else {
                                        // El camión se mueve hacia la izquierda
                                        updateCamionMarker(R.drawable.camion_izquierda);
                                    }
                                }

                                // Detectar movimiento diagonal
                                if (Math.abs(deltaLat) > 0 && Math.abs(deltaLng) > 0) {
                                    if (deltaLat > 0 && deltaLng > 0) {
                                        updateCamionMarker(R.drawable.camion_diagonal_arriba_derecha);
                                    } else if (deltaLat > 0 && deltaLng < 0) {
                                        updateCamionMarker(R.drawable.camion_diagonal_arriba_izquierda);
                                    } else if (deltaLat < 0 && deltaLng > 0) {
                                        updateCamionMarker(R.drawable.camion_diagonal_abajo_derecha);
                                    } else if (deltaLat < 0 && deltaLng < 0) {
                                        updateCamionMarker(R.drawable.camion_diagonal_abajo_izquierda);
                                    }
                                }
                            }

                            // Actualizar la posición anterior
                            previousLocation = newLocation;

                            // Actualizar la posición del marcador con la nueva ubicación
                            if (basureroMarker != null) {
                                basureroMarker.setPosition(newLocation);
                            } else {
                                basureroMarker = mMap.addMarker(new MarkerOptions()
                                        .position(newLocation)
                                        .title("Camión de basura"));
                            }
                        }
                    } else {
                        Log.e("MapUIUser", "Failed to fetch basurero location");
                    }
                });
    }


    private void updateCamionMarker(int imageResource) {
        if (basureroMarker != null) {
            basureroMarker.setIcon(BitmapDescriptorFactory.fromResource(imageResource));
        }
    }


    @Override
    public void onBackPressed() {
        // Obtener el nombre del usuario (esto depende de cómo lo estés manejando en tu aplicación)
        String userName = username; // Reemplaza esto con el nombre real del usuario

        // Mostrar la pantalla de carga antes de volver a UserUI
        Intent cargaIntent = new Intent(MapUIUser.this, CargaUI.class);
        startActivity(cargaIntent);

        // Usar Handler para retrasar el inicio de UserUI y permitir que la pantalla de carga se muestre
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Asegúrate de que la pantalla de carga esté cerrada antes de iniciar UserUI
                Intent intent = new Intent(MapUIUser.this, UserUI.class);
                // Agregar el nombre del usuario al Intent
                intent.putExtra("username", userName);

                startActivity(intent);
            }
        }, 500); // Esperar 500 ms antes de iniciar UserUI
    }

}
