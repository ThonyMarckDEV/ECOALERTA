package com.example.ecoalerta.Interfaces;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import com.example.ecoalerta.R;

public class NuevaContraUI extends AppCompatActivity {

    private EditText edtNuevaContra;
    private Button btnActualizarContra;
    private String correoRecibido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nueva_contra_ui);

        edtNuevaContra = findViewById(R.id.edtNuevaContra);
        btnActualizarContra = findViewById(R.id.btnActualizarContra);

        // Recibir el correo desde el Intent
        correoRecibido = getIntent().getStringExtra("correo");

        btnActualizarContra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nuevaContra = edtNuevaContra.getText().toString();

                if (!TextUtils.isEmpty(nuevaContra)) {
                    actualizarContrasena(correoRecibido, nuevaContra);
                } else {
                    Toast.makeText(NuevaContraUI.this, "Por favor ingresa una nueva contraseña", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // Función para enviar la nueva contraseña al servidor mediante POST
    private void actualizarContrasena(final String correo, final String nuevaContra) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Aquí debes colocar la URL de tu script PHP
                    String urlString = ApiService.BASE_URL + "actualizar_contra.php";
                    URL url = new URL(urlString);

                    // Parámetros para enviar en la solicitud POST
                    Map<String, String> params = new HashMap<>();
                    params.put("correo", correo);
                    params.put("nueva_contra", nuevaContra);

                    // Crear la conexión HTTP
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    // Escribir los parámetros en el cuerpo de la solicitud
                    StringBuilder postData = new StringBuilder();
                    for (Map.Entry<String, String> param : params.entrySet()) {
                        if (postData.length() != 0) postData.append('&');
                        postData.append(URLEncoder.encode(param.getKey(), "UTF-8")).append('=')
                                .append(URLEncoder.encode(param.getValue(), "UTF-8"));
                    }

                    byte[] postDataBytes = postData.toString().getBytes("UTF-8");
                    connection.getOutputStream().write(postDataBytes);

                    // Leer la respuesta del servidor
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Mostrar el resultado de la solicitud en el UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NuevaContraUI.this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();

                            // Regresar al LoginUI
                            Intent intent = new Intent(NuevaContraUI.this, LoginUI.class);
                            startActivity(intent);
                            finish(); // Cierra esta actividad para que no pueda volver atrás
                        }
                    });

                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NuevaContraUI.this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}