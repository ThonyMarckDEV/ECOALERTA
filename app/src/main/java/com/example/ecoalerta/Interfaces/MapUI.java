package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class MapUI extends AppCompatActivity implements OnMapReadyCallback {

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
        setContentView(R.layout.activity_map_ui);

        // Obtener el username del Intent
        username = getIntent().getStringExtra("username");

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        locationService = new LocationService(this, fusedLocationClient, db); // Pasar el contexto aquí


        new CheckUserRoleTask().execute(username);

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
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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
        LocationHelper.handlePermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void onLocationPermissionGranted() {
        locationService.updateLocation();
    }

    @Override
    public void onBackPressed() {
        // Obtener el nombre del usuario (esto depende de cómo lo estés manejando en tu aplicación)
        String userName = username; // Reemplaza esto con el nombre real del usuario

        // Mostrar la pantalla de carga antes de volver a UserUI
        Intent cargaIntent = new Intent(MapUI.this, CargaUI.class);
        startActivity(cargaIntent);

        // Usar Handler para retrasar el inicio de UserUI y permitir que la pantalla de carga se muestre
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Asegúrate de que la pantalla de carga esté cerrada antes de iniciar UserUI
                Intent intent = new Intent(MapUI.this, BasureroUI.class);
                // Agregar el nombre del usuario al Intent
                intent.putExtra("username", userName);

                startActivity(intent);
                finish(); // Cerrar la actividad actual para que el usuario no vuelva a ella
            }
        }, 500); // Esperar 500 ms antes de iniciar UserUI
    }

    private class CheckUserRoleTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String result = "";

            try {
                String urlString = "https://modern-blindly-kangaroo.ngrok-free.app/PHP/get_user_rol.php?username=" + URLEncoder.encode(username, "UTF-8");
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                result = stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("rol")) {
                    String rol = jsonObject.getString("rol");
                    if ("Basurero".equals(rol)) {
                        // Ejecuta la lógica si el rol es Basurero
                        if (LocationHelper.checkLocationPermission(MapUI.this)) {
                            locationService.updateLocation();
                        } else {
                            LocationHelper.requestLocationPermission(MapUI.this);
                        }
                    }
                } else if (jsonObject.has("error")) {
                    Log.e("CheckUserRoleTask", jsonObject.getString("error"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
