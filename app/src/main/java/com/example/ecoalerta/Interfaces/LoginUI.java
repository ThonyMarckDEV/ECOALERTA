package com.example.ecoalerta.Interfaces;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ecoalerta.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class LoginUI extends AppCompatActivity {

    private Intent cargaIntent;
    private EditText txtUsername, txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Inicializar la Intent para la pantalla de carga
        cargaIntent = new Intent(LoginUI.this, CargaUI.class);

        // Referencias a los ImageView
        ImageView gifImageView1 = findViewById(R.id.gifImageView1);
        ImageView gifImageView2 = findViewById(R.id.gifImageView2);
        ImageView gifImageView3 = findViewById(R.id.gifImageView3);
        ImageView gifImageView4 = findViewById(R.id.gifImageView4);
        ImageView gifImageView5 = findViewById(R.id.gifImageView5);

        // Cargar el GIF en cada ImageView usando Glide
        Glide.with(this).asGif().load(R.drawable.flor).into(gifImageView1);
        Glide.with(this).asGif().load(R.drawable.flor).into(gifImageView2);
        Glide.with(this).asGif().load(R.drawable.flor).into(gifImageView3);
        Glide.with(this).asGif().load(R.drawable.flor).into(gifImageView4);
        Glide.with(this).asGif().load(R.drawable.flor).into(gifImageView5);

        // Inicializar campos de entrada y botones
        txtUsername = findViewById(R.id.txtUserNameLogin);
        txtPassword = findViewById(R.id.txtPasswordLogin);
        Button btnLogin = findViewById(R.id.btnLogearse); // Asumiendo que este es el id del botón de inicio de sesión
        Button btnMapa = findViewById(R.id.btnMapa);
        TextView lblNuevo = findViewById(R.id.lblNuevo);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = txtUsername.getText().toString();
                String password = txtPassword.getText().toString();

                // Validar campos de entrada
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginUI.this, "Por favor ingrese ambos campos", Toast.LENGTH_LONG).show();
                    return;
                }

                // Ejecutar el LoginTask con las credenciales
                new LoginTask().execute(username, password);
            }
        });

        btnMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irMapa();
            }
        });

        lblNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irRegister();
            }
        });
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Mostrar la pantalla de carga
            startActivity(cargaIntent);
        }

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];

            try {
                URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/login.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Enviar las credenciales al servidor
                String postData = "username=" + URLEncoder.encode(username, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8");
                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                // Leer la respuesta del servidor
                InputStream is = connection.getInputStream();
                Scanner scanner = new Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();
                is.close();

                // Imprimir respuesta para depuración
                System.out.println("Server Response: " + response);

                // Limpiar la respuesta si es necesario
                if (response.startsWith("Conexion Exitosa")) {
                    // Extraer solo el JSON de la respuesta
                    response = response.substring(response.indexOf("{"));
                }

                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return "{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                // Intenta parsear la respuesta como JSON
                JSONObject jsonResponse = new JSONObject(result);

                String status = jsonResponse.getString("status");
                if (status.equals("success")) {
                    String rol = jsonResponse.getString("rol");
                    String username = jsonResponse.getString("username");

                    Intent intent;
                    switch (rol) {
                        case "Usuario":
                            intent = new Intent(LoginUI.this, UserUI.class);
                            break;
                        case "Admin":
                            intent = new Intent(LoginUI.this, AdminUI.class);
                            break;
                        case "Basurero":
                            intent = new Intent(LoginUI.this, BasureroUI.class);
                            break;
                        default:
                            Toast.makeText(LoginUI.this, "Rol desconocido", Toast.LENGTH_LONG).show();
                            return;
                    }

                    // Pasar el nombre de usuario a la siguiente interfaz
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish(); // Cerrar la actividad actual

                } else {
                    // Mostrar error si las credenciales son incorrectas
                    Toast.makeText(LoginUI.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(LoginUI.this, "Error al procesar la respuesta: " + result, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void irMapa() {
        // Mostrar la pantalla de carga antes de iniciar MapUI
        startActivity(cargaIntent);

        // Usar Handler para retrasar el inicio de la nueva actividad y permitir que la pantalla de carga se muestre
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoginUI.this, MapUI.class);
                startActivity(intent);
                finish(); // Cerrar la actividad actual para que el usuario no vuelva a ella
            }
        }, 500); // Esperar 500 ms antes de iniciar MapUI
    }

    private void irRegister() {
        // Mostrar la pantalla de carga antes de iniciar RegisterUI
        startActivity(cargaIntent);

        // Usar Handler para retrasar el inicio de la nueva actividad y permitir que la pantalla de carga se muestre
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoginUI.this, RegisterUI.class);
                startActivity(intent);
                finish(); // Cerrar la actividad actual para que el usuario no vuelva a ella
            }
        }, 500); // Esperar 500 ms antes de iniciar RegisterUI
    }
}
