package com.example.ecoalerta.Interfaces;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecoalerta.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportarUI extends AppCompatActivity {
    private static final int PICK_IMAGE = 1; // Request code para seleccionar imagen
    private EditText textArea;
    private TextView txvfecha;
    private ImageView imgvReporte;
    private Button btnCargarFoto, btnReportar;
    private Bitmap imageBitmap;
    private ProgressDialog progressDialog;
    private String idUsuario; // Para almacenar el idUsuario
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportar_ui);

        textArea = findViewById(R.id.textArea);
        txvfecha = findViewById(R.id.txvfecha);
        imgvReporte = findViewById(R.id.imgvReporte);
        btnCargarFoto = findViewById(R.id.btnCargarFoto);
        btnReportar = findViewById(R.id.btnReportar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Enviando reporte...");
        progressDialog.setCancelable(false);

        // Mostrar la fecha y hora actual en el TextView
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        txvfecha.setText(currentDateAndTime);


        // Obtener el username pasado desde UserUI
         username = getIntent().getStringExtra("username");

        // Si hay un username, buscar el idUsuario
        if (username != null) {
            new ObtenerIdUsuarioTask().execute(username);
        }

        btnReportar.setOnClickListener(v -> {
            if (idUsuario != null) {
                new EnviarReporteTask().execute();
            } else {
                Toast.makeText(this, "No se pudo obtener el idUsuario", Toast.LENGTH_SHORT).show();
            }
        });

        // Cargar imagen desde la galería al hacer clic en el botón
        btnCargarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imgvReporte.setImageBitmap(imageBitmap); // Mostrar la imagen seleccionada en el ImageView
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ObtenerIdUsuarioTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            try {
                URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/get_user_id.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Escribir los parámetros del POST
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write("username=" + username);
                writer.flush();
                writer.close();
                os.close();

                // Leer la respuesta
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Leer el cuerpo de la respuesta (donde debería estar el idUsuario)
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    return result.toString(); // Esto debería ser el idUsuario
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.isEmpty()) {
                idUsuario = result.trim();  // Asegúrate de que no haya espacios o saltos de línea extraños
            } else {
                Toast.makeText(ReportarUI.this, "Error al obtener idUsuario", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // AsyncTask para enviar el reporte
    private class EnviarReporteTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/report_data.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Obtener la fecha y hora actual
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String fechaHora = sdf.format(new Date());

                // Obtener la descripción (texto del EditText)
                String descripcion = textArea.getText().toString();

                // Convertir la imagen a Base64
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                String imagenBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // Crear parámetros del POST
                Map<String, String> postData = new HashMap<>();
                postData.put("idUsuario", idUsuario);
                postData.put("fecha", fechaHora);
                postData.put("descripcion", descripcion);
                postData.put("imagen", imagenBase64);

                // Escribir los parámetros del POST
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postData));
                writer.flush();
                writer.close();
                os.close();

                // Leer la respuesta
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return connection.getResponseMessage();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result != null) {
                Toast.makeText(ReportarUI.this, "Reporte enviado exitosamente", Toast.LENGTH_SHORT).show();
                textArea.setText("");
                imgvReporte.setImageResource(android.R.color.transparent);
                imageBitmap = null;
                textArea.clearFocus();
            } else {
                Toast.makeText(ReportarUI.this, "Error al enviar reporte", Toast.LENGTH_SHORT).show();
                textArea.setText("");
                imgvReporte.setImageResource(android.R.color.transparent);
                imageBitmap = null;
                textArea.clearFocus();
            }
        }
    }

    private String getPostDataString(Map<String, String> params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }
        return result.toString();
    }

    @Override
    public void onBackPressed() {
        // Obtener el nombre del usuario (esto depende de cómo lo estés manejando en tu aplicación)
        String userName = username; // Reemplaza esto con el nombre real del usuario

        // Mostrar la pantalla de carga antes de volver a UserUI
        Intent cargaIntent = new Intent(ReportarUI.this, CargaUI.class);
        startActivity(cargaIntent);

        // Usar Handler para retrasar el inicio de UserUI y permitir que la pantalla de carga se muestre
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Asegúrate de que la pantalla de carga esté cerrada antes de iniciar UserUI
                Intent intent = new Intent(ReportarUI.this, UserUI.class);
                // Agregar el nombre del usuario al Intent
                intent.putExtra("username", userName);

                startActivity(intent);
            }
        }, 500); // Esperar 500 ms antes de iniciar UserUI
    }
}
