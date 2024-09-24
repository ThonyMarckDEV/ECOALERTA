package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecoalerta.Interfaces.ApiService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ecoalerta.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class UserUI extends AppCompatActivity {

    private TextView txtWelcome;
    private Button btnDeslogear;
    private ImageView imgvPerfil;
    private ImageView imgvLoading; // Agregar esta línea
    private String username; // Declara username aquí

    private AnuncioChecker anuncioChecker;
    private int idUsuario; // Variable para almacenar el idUsuario
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_ui);

        // Inicializa el username desde el Intent
        username = getIntent().getStringExtra("username");

        // Verificador de anuncio
        VerificadorDeAnuncio verificadorAnuncio = new VerificadorDeAnuncio(this, username);
        verificadorAnuncio.iniciarVerificacion();

        // Verificador de sesión cada 10 segundos
        EstadoUsuarioVerificador verificador = new EstadoUsuarioVerificador(this);
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                verificador.verificarEstado();
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);

        // Inicializar elementos
        txtWelcome = findViewById(R.id.txtWelcome);
        btnDeslogear = findViewById(R.id.btnLogout);
        imgvPerfil = findViewById(R.id.imgvPerfil);
        imgvLoading = findViewById(R.id.imgvLoading);
        Button btnVerMapa = findViewById(R.id.btnVerMapa);
        Button btnPerfil = findViewById(R.id.btnPerfil);
        Button btnReporte = findViewById(R.id.btnReporte);
        Button btnContactoMuni = findViewById(R.id.btnContactoMuni);

        // Mostrar el GIF de carga
        if (imgvLoading != null) {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.loadingperfil)
                    .into(imgvLoading);
        }

        // Mensaje de bienvenida
        if (username != null) {
            txtWelcome.setText("Bienvenido, " + username);
            PerfilImagenLoader perfilLoader = new PerfilImagenLoader(this, imgvLoading, imgvPerfil);
            perfilLoader.cargarImagen(username);
        } else {
            txtWelcome.setText("Bienvenido, Usuario");
        }

        btnDeslogear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar la pantalla de carga
                Intent cargaIntent = new Intent(UserUI.this, CargaUI.class);
                startActivity(cargaIntent);
                SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove("username");
                editor.apply();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(ApiService.BASE_URL + "update_status.php");
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

                            // Verificar el código de respuesta
                            int responseCode = connection.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                Log.d("Deslogear", "Estado actualizado exitosamente");
                            } else {
                                Log.d("Deslogear", "Error al actualizar estado: " + responseCode);
                            }

                            connection.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Deslogear", "Error en la solicitud: " + e.getMessage());
                        }
                    }
                }).start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(UserUI.this, LoginUI.class);
                        startActivity(intent);
                        finish();
                    }
                }, 500);
            }
        });

        // Configurar el botón verMapa
        btnVerMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocationPermissions();
            }
        });

        // Configurar el botón perfil
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(UserUI.this, PerfilUIUser.class);
                mapIntent.putExtra("username", username);
                startActivity(mapIntent);
            }
        });

        // Configurar el botón reporte
        btnReporte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(UserUI.this, ReportarUI.class);
                mapIntent.putExtra("username", username);
                startActivity(mapIntent);
            }
        });

        // Configurar el botón contacto muni
        btnContactoMuni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(UserUI.this, MunicipalidadContactUI.class);
                mapIntent.putExtra("username", username);
                startActivity(mapIntent);
            }
        });
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkLocationEnabled();
        }
    }

    private void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        } else {
            openMap();
        }
    }

    private void openMap() {
        Intent mapIntent = new Intent(UserUI.this, MapUIUser.class);
        mapIntent.putExtra("username", username);
        startActivity(mapIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationEnabled();
            } else {
                Toast.makeText(this, "Se necesita permiso de ubicación para continuar.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent cargaIntent = new Intent(UserUI.this, CargaUI.class);
        startActivity(cargaIntent);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(ApiService.BASE_URL + "update_status.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoOutput(true);
                    String postData = "username=" + username;
                    OutputStream os = connection.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();
                    SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("username");
                    editor.apply();
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(UserUI.this, LoginUI.class);
                startActivity(intent);
                finish();
            }
        }, 500);
    }
}
