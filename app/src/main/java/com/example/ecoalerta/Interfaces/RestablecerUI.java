package com.example.ecoalerta.Interfaces;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.net.ssl.HttpsURLConnection;
import com.example.ecoalerta.R;

public class RestablecerUI extends AppCompatActivity {

    private EditText editTextEmail, editTextCodigo;
    private Button btnEnviarEmail, btnVerificarCodigo;
    private String codigoGenerado;
    private String correoIngresado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restablecer_ui);


        editTextEmail = findViewById(R.id.edtEmail);
        editTextCodigo = findViewById(R.id.edtCodigo);
        btnEnviarEmail = findViewById(R.id.btnEnviarCodigo);
        btnVerificarCodigo = findViewById(R.id.btnVerificarCode);

        btnEnviarEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correoIngresado = editTextEmail.getText().toString();

                if (!TextUtils.isEmpty(correoIngresado)) {
                    codigoGenerado = generarCodigoAleatorio(); // Generar un código aleatorio
                    enviarEmail(correoIngresado, codigoGenerado); // Enviar el correo con el código
                    editTextEmail.setText("");
                } else {
                    Toast.makeText(RestablecerUI.this, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnVerificarCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String codigoIngresado = editTextCodigo.getText().toString();
                if (codigoIngresado.equals(codigoGenerado)) {
                    Intent intent = new Intent(RestablecerUI.this, NuevaContraUI.class);
                    intent.putExtra("correo", correoIngresado); // Enviar el correo por Intent
                    startActivity(intent);
                } else {
                    Toast.makeText(RestablecerUI.this, "Código incorrecto, inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // Función para generar un código aleatorio de 6 dígitos
    private String generarCodigoAleatorio() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000); // Genera un número aleatorio entre 100000 y 999999
        return String.valueOf(codigo);
    }

    // Función para enviar el email mediante POST al servidor
    private void enviarEmail(final String correo, final String codigo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Aquí debes colocar la URL de tu script PHP
                    String urlString = ApiService.BASE_URL + "enviar_email_restablecer.php";
                    URL url = new URL(urlString);

                    // Parámetros para enviar en la solicitud POST
                    Map<String, String> params = new HashMap<>();
                    params.put("correo", correo);
                    params.put("codigo", codigo);

                    // Crear la conexión HTTP
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    // Escribir los parámetros en el cuerpo de la solicitud
                    StringBuilder postData = new StringBuilder();
                    for (Map.Entry<String, String> param : params.entrySet()) {
                        if (postData.length() != 0) postData.append('&');
                        postData.append(param.getKey()).append('=').append(param.getValue());
                    }

                    byte[] postDataBytes = postData.toString().getBytes("UTF-8");
                    connection.getOutputStream().write(postDataBytes);

                    // Leer la respuesta del servidor
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        // Mostrar el resultado de la solicitud
                        final String responseStr = response.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RestablecerUI.this, responseStr, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RestablecerUI.this, "Error al enviar el correo", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RestablecerUI.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        // Mostrar la pantalla de carga antes de volver a UserUI
        Intent cargaIntent = new Intent(RestablecerUI.this, CargaUI.class);
        startActivity(cargaIntent);

        // Usar Handler para retrasar el inicio de UserUI y permitir que la pantalla de carga se muestre
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Asegúrate de que la pantalla de carga esté cerrada antes de iniciar UserUI
                Intent intent = new Intent(RestablecerUI.this, LoginUI.class);
                startActivity(intent);
            }
        }, 500); // Esperar 500 ms antes de iniciar UserUI
    }
}