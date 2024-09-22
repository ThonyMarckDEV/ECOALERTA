package com.example.ecoalerta.Interfaces;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecoalerta.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class EnviarNotificacionUI extends AppCompatActivity {

    private EditText etTituloNotificacion, etMensajeNotificacion;
    private Button btnEnviarNotificacion;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enviar_notificacion_ui);

        etTituloNotificacion = findViewById(R.id.etTituloNotificacion);
        etMensajeNotificacion = findViewById(R.id.etMensajeNotificacion);
        btnEnviarNotificacion = findViewById(R.id.btnEnviarNotificacion);

        btnEnviarNotificacion.setOnClickListener(v -> {
            String titulo = etTituloNotificacion.getText().toString();
            String mensaje = etMensajeNotificacion.getText().toString();

            if (!titulo.isEmpty() && !mensaje.isEmpty()) {
                new EnviarAnuncioTask().execute(titulo, mensaje);
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class EnviarAnuncioTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Mostrar el ProgressDialog
            progressDialog = new ProgressDialog(EnviarNotificacionUI.this);
            progressDialog.setMessage("Enviando notificación...");
            progressDialog.setCancelable(false);  // El diálogo no se puede cancelar manualmente
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String titulo = params[0];
            String mensaje = params[1];

            try {
                URL url = new URL(ApiService.BASE_URL + "guardar_anuncio.php");  // Cambia con tu URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Enviar los parámetros
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write("titulo=" + titulo + "&mensaje=" + mensaje);
                writer.flush();
                writer.close();
                os.close();

                // Leer la respuesta
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // Ocultar el ProgressDialog
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            Log.d("Resultado del anuncio", result != null ? result : "Respuesta nula");

            if (result != null && result.contains("success")) {
                Toast.makeText(EnviarNotificacionUI.this, "Anuncio enviado", Toast.LENGTH_SHORT).show();

                // Limpiar los campos de texto
                etTituloNotificacion.setText("");
                etMensajeNotificacion.setText("");
            } else {
                Toast.makeText(EnviarNotificacionUI.this, "Error al enviar el anuncio", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
