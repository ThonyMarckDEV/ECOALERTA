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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_ui);

        // Inicializar elementos
        txtWelcome = findViewById(R.id.txtWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        imgvPerfil = findViewById(R.id.imgvPerfil);
        imgvLoading = findViewById(R.id.imgvLoading); // Inicializar el ImageView para el GIF
        Button btnVerMapa = findViewById(R.id.btnVerMapa); // Inicializar el botón para ver mapa

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
            cargarImagenPerfil(username);
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

        // Configurar el botón de ver mapa
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
    }

    private void cargarImagenPerfil(String username) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/get_profile_picture.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoOutput(true);

                    // Enviar el nombre de usuario al servidor
                    String postData = "username=" + URLEncoder.encode(username, "UTF-8");
                    OutputStream os = connection.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = connection.getInputStream();
                    } else {
                        inputStream = connection.getErrorStream();
                    }

                    Scanner in = new Scanner(inputStream);
                    StringBuilder response = new StringBuilder();
                    while (in.hasNextLine()) {
                        response.append(in.nextLine());
                    }
                    in.close();

                    try {
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        if (jsonResponse.getString("status").equals("success")) {
                            String perfilBase64 = jsonResponse.getString("perfil");

                            // Decodificar Base64 a byte array
                            byte[] decodedString = Base64.decode(perfilBase64.split(",")[1], Base64.DEFAULT);

                            // Convertir byte array a Bitmap
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Ocultar el GIF de carga
                                    if (imgvLoading != null) {
                                        imgvLoading.setVisibility(View.GONE);
                                    }

                                    // Usar Glide para cargar la imagen en el ImageView con la transformación circular
                                    Glide.with(UserUI.this)
                                            .load(bitmap)
                                            .apply(RequestOptions.circleCropTransform())
                                            .into(imgvPerfil);
                                }
                            });
                        } else {
                            String errorMessage = jsonResponse.optString("message", "Error desconocido");
                            System.err.println("Error del servidor: " + errorMessage);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.err.println("La respuesta no es un JSON válido: " + response.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        // Mostrar la pantalla de carga antes de volver a LoginUI
        Intent cargaIntent = new Intent(UserUI.this, CargaUI.class);
        startActivity(cargaIntent);

        // Usar Handler para retrasar el inicio de LoginUI y permitir que la pantalla de carga se muestre
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Asegúrate de que la pantalla de carga esté cerrada antes de iniciar LoginUI
                Intent intent = new Intent(UserUI.this, LoginUI.class);
                startActivity(intent);
                finish(); // Cerrar la actividad actual para que el usuario no vuelva a ella
            }
        }, 500); // Esperar 500 ms antes de iniciar LoginUI

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Actualizar el estado del usuario a "logged_off" en el servidor
        String username = getIntent().getStringExtra("username");
        if (username != null) {
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
    }
}