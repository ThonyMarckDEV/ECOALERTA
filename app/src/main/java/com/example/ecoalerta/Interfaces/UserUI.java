package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ecoalerta.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class UserUI extends AppCompatActivity {

    private TextView txtWelcome;
    private Button btnLogout;
    private ImageView imgvPerfil;
    private ImageView imgvLoading; // Agregar esta línea
    private String username; // Declara username aquí

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_ui);

        // Inicializa el username desde el Intent
        username = getIntent().getStringExtra("username");

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

        // Inicializar elementos
        txtWelcome = findViewById(R.id.txtWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        imgvPerfil = findViewById(R.id.imgvPerfil);
        imgvLoading = findViewById(R.id.imgvLoading); // Inicializar el ImageView para el GIF
        Button btnVerMapa = findViewById(R.id.btnVerMapa); // Inicializar el botón para ver mapa
        Button btnPerfil = findViewById(R.id.btnPerfil); // Inicializar el botón para ver mapa
        Button btnReporte = findViewById(R.id.btnReporte); // Inicializar el botón para ver mapa
        Button btnContactoMuni = findViewById(R.id.btnContactoMuni); // Inicializar el botón para contactomuni

        // Obtener el username del Intent
        String username = getIntent().getStringExtra("username");

        // Mostrar el GIF de carga
        if (imgvLoading != null) {
            Glide.with(this)
                    .asGif() // Asegúrate de que Glide maneje GIFs
                    .load(R.drawable.loadingperfil) // Nombre del archivo GIF en res/drawable
                    .into(imgvLoading);
        }

        // Mostrar un mensaje de bienvenida con el username
        if (username != null) {
            txtWelcome.setText("Bienvenido, " + username);
            // Usar CLASE PerfilImagenLoader para cargar la imagen de perfil
            PerfilImagenLoader perfilLoader = new PerfilImagenLoader(this, imgvLoading, imgvPerfil);
            perfilLoader.cargarImagen(username);
        } else {
            txtWelcome.setText("Bienvenido, Usuario");
        }

        // Configurar el botón de logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar la pantalla de carga
                Intent cargaIntent = new Intent(UserUI.this, CargaUI.class);
                startActivity(cargaIntent);

                // Para limpiar el nombre de usuario cuando el usuario cierra sesión
                SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove("username"); // O editor.clear() para borrar todos los datos
                editor.apply();

                // Actualizar el estado del usuario a "logged_off" en el servidor
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // URL del archivo PHP
                            URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/update_status.php"); // Cambia esta URL a la URL correcta
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            connection.setDoOutput(true);

                            // Enviar el nombre de usuario al servidor
                            String postData = "username=" + username;
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

                // Redirigir a LoginUI después de un breve retraso para mostrar la pantalla de carga
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(UserUI.this, LoginUI.class);
                        startActivity(intent);
                        finish(); // Cerrar la actividad actual para que no se pueda volver a ella
                    }
                }, 500); // Esperar 500 ms antes de iniciar LoginUI
            }
        });

        // Configurar el botón verMapa
        if (username != null) {
            btnVerMapa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent = new Intent(UserUI.this, MapUIUser.class);
                    mapIntent.putExtra("username", username); // Pasar el nombre de usuario
                    startActivity(mapIntent);
                }
            });
        }

        // Configurar el botón perfil
        if (username != null) {
            btnPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent = new Intent(UserUI.this, PerfilUIUser.class);
                    mapIntent.putExtra("username", username); // Pasar el nombre de usuario
                    startActivity(mapIntent);
                }
            });
        }

        // Configurar el botón reporte
        if (username != null) {
            btnReporte.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent = new Intent(UserUI.this, ReportarUI.class);
                    mapIntent.putExtra("username", username); // Pasar el nombre de usuario
                    startActivity(mapIntent);
                }
            });
        }

        // Configurar el botón contacto muni
        if (username != null) {
            btnContactoMuni.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent = new Intent(UserUI.this, MunicipalidadContactUI.class);
                    mapIntent.putExtra("username", username); // Pasar el nombre de usuario
                    startActivity(mapIntent);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent cargaIntent = new Intent(UserUI.this, CargaUI.class);
        startActivity(cargaIntent);

        // Actualizar el estado del usuario a "logged_off" en el servidor
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/update_status.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoOutput(true);

                    // Usa la variable username aquí
                    String postData = "username=" + username;
                    OutputStream os = connection.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();

                    // Limpiar el nombre de usuario cuando el usuario cierra sesión
                    SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("username");
                    editor.apply();

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

        // Redirigir a LoginUI después de un breve retraso
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
