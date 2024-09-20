package com.example.ecoalerta.Interfaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
    private Marker userMarker;
    private MapView mapView;
    private LatLng currentUserLatLng;
    private boolean isInitialCameraPositionSet = false;
    private MediaPlayer proximitySoundPlayer;
    private boolean isSoundPlaying = false;
    private String username; // Añadir esta línea para guardar el nombre del usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_ui_basurero);

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

        // Update location every 3 seconds
        final Handler handler = new Handler();
        final Runnable updateLocationTask = new Runnable() {
            @Override
            public void run() {
                if (mMap != null) {
                    updateUserLocation();
                    updateBasureroLocation();
                }
                handler.postDelayed(this, 3000); // Update every 3 seconds
            }
        };
        handler.post(updateLocationTask);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            updateUserLocation();
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (proximitySoundPlayer != null) {
            if (proximitySoundPlayer.isPlaying()) {
                proximitySoundPlayer.stop();
            }
            proximitySoundPlayer.release();
            proximitySoundPlayer = null;
        }

        // Actualizar el estado del usuario a "logged_off" en el servidor
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Obtener el nombre de usuario del Intent
                    String username = getIntent().getStringExtra("username");

                    // URL del archivo PHP
                    URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/update_status.php"); // Cambia esta URL a la URL correcta
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
                            if (userMarker != null) {
                                userMarker.setPosition(userLatLng);
                            } else {
                                userMarker = mMap.addMarker(new MarkerOptions()
                                        .position(userLatLng)
                                        .title("Your Location")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            }
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
                URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/send_email.php"); // Cambia esta URL a la URL correcta
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
                            LatLng latLng = new LatLng(location.get(0), location.get(1));
                            if (basureroMarker != null) {
                                basureroMarker.setPosition(latLng);
                            } else {
                                basureroMarker = mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title("Basurero")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.camion_icono)));
                            }
                        }
                    } else {
                        Log.e("MapUIUser", "Failed to fetch basurero location");
                    }
                });
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
                finish(); // Cerrar la actividad actual para que el usuario no vuelva a ella
            }
        }, 500); // Esperar 500 ms antes de iniciar UserUI
    }


}
