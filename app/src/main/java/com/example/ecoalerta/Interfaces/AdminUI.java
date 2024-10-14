package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ecoalerta.R;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdminUI extends AppCompatActivity {
    private TextView txtWelcomeADMIN;
    private Button btnLogout;
    private ImageView imgvPerfilADMIN;
    private ImageView imgvLoadingADMIN; // Agregar esta línea
    private String username; // Declara username aquí

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_ui);

        // Llamamos a la clase UpdateChecker para verificar actualizaciones
        CheckUpdate updateChecker = new CheckUpdate(this);

        // Llamamos a checkForUpdate y pasamos un nuevo UpdateListener con los métodos implementados
        updateChecker.checkForUpdate();

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
        txtWelcomeADMIN = findViewById(R.id.txtWelcomeADMIN);
        btnLogout = findViewById(R.id.btnLogoutADMIN);
        imgvPerfilADMIN = findViewById(R.id.imgvPerfilAdmin);
        imgvLoadingADMIN = findViewById(R.id.imgvLoadingAdmin); // Inicializar el ImageView para el GIF
        Button btnEnviarNoti = findViewById(R.id.btnEnviarNotificacion); // Inicializar el botón para ver mapa
        Button btnGestionarRepo = findViewById(R.id.btnGestionarReportes); // Inicializar el botón para ver mapa

        // Obtener el username del Intent
        String username = getIntent().getStringExtra("username");

        // Mostrar el GIF de carga
        if (imgvLoadingADMIN != null) {
            Glide.with(this)
                    .asGif() // Asegúrate de que Glide maneje GIFs
                    .load(R.drawable.loadingperfil) // Nombre del archivo GIF en res/drawable
                    .into(imgvLoadingADMIN);
        }

        // Mostrar un mensaje de bienvenida con el username
        if (username != null) {
            txtWelcomeADMIN.setText("Bienvenido, " + username);
            // Usar CLASE PerfilImagenLoader para cargar la imagen de perfil
            PerfilImagenLoader perfilLoader = new PerfilImagenLoader(this, imgvLoadingADMIN, imgvPerfilADMIN);
            perfilLoader.cargarImagen(username);
        } else {
            txtWelcomeADMIN.setText("Bienvenido, Usuario");
        }

        // Configurar el botón de logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar la pantalla de carga
                Intent cargaIntent = new Intent(AdminUI.this, CargaUI.class);
                startActivity(cargaIntent);

                // Actualizar el estado del usuario a "logged_off" en el servidor
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(ApiService.BASE_URL + "update_status.php");
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
                        Intent intent = new Intent(AdminUI.this, LoginUI.class);
                        startActivity(intent);
                        finish();
                    }
                }, 500);
            }
        });

        // Configurar el botón listar reportes
        if (username != null) {
            btnGestionarRepo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent = new Intent(AdminUI.this, ListarReportesUI.class);
                    mapIntent.putExtra("username", username);
                    startActivity(mapIntent);
                }
            });
        }

        // Configurar el botón EnviarNoti
        if (username != null) {
            btnEnviarNoti.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent = new Intent(AdminUI.this, EnviarNotificacionUI.class);
                    mapIntent.putExtra("username", username); // Pasar el nombre de usuario
                    startActivity(mapIntent);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent cargaIntent = new Intent(AdminUI.this, CargaUI.class);
        startActivity(cargaIntent);

        // Actualizar el estado del usuario a "logged_off" en el servidor
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(ApiService.BASE_URL + "update_status.php");
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
                Intent intent = new Intent(AdminUI.this, LoginUI.class);
                startActivity(intent);
                finish();
            }
        }, 500);
    }
}