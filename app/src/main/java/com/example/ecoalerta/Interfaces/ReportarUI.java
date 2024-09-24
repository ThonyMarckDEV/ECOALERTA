package com.example.ecoalerta.Interfaces;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ecoalerta.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportarUI extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int TAKE_PHOTO_REQUEST = 1;
    private EditText textArea;
    private TextView txvfecha;
    private ImageView imgvReporte;
    private Button btnCargarFoto, btnReportar;
    private Bitmap imageBitmap;  // Almacena la imagen después de tomarla
    private ProgressDialog progressDialog;
    private String idUsuario;
    private String username;
    private String imageUrl;  // Almacenar la URL de la imagen después de subirla



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportar_ui);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        textArea = findViewById(R.id.textArea);
        txvfecha = findViewById(R.id.txvfecha);
        imgvReporte = findViewById(R.id.imgvReporte);
        btnCargarFoto = findViewById(R.id.btnCargarFoto);
        btnReportar = findViewById(R.id.btnReportar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Enviando reporte...");
        progressDialog.setCancelable(false);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        txvfecha.setText(currentDateAndTime);

        username = getIntent().getStringExtra("username");

        // =================================================================================
        /**
         * Verificador Sesion cada 10 seg
         */
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
        // =================================================================================


        if (username != null) {
            new ObtenerIdUsuarioTask().execute(username);
        }

        btnReportar.setOnClickListener(v -> {
            if (idUsuario != null && imageBitmap != null) {
                new EnviarReporteTask().execute();  // Enviar el reporte cuando se presiona el botón
            } else {
                Toast.makeText(this, "No se pudo obtener el idUsuario o la imagen no está seleccionada", Toast.LENGTH_SHORT).show();
            }
        });

        btnCargarFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, TAKE_PHOTO_REQUEST);
                }
            } else {
                Toast.makeText(this, "Necesitas conceder permiso para usar la cámara", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ObtenerIdUsuarioTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            try {
                // URL del servicio que obtiene el idUsuario basado en el username
                URL url = new URL(ApiService.BASE_URL + "get_user_id.php");
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imgvReporte.setImageBitmap(imageBitmap);  // Mostrar la imagen capturada
        }
    }

    // Tarea para enviar el reporte y la imagen al presionar "Enviar Reporte"
    private class EnviarReporteTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                // Convertir la imagen en un array de bytes para subirla
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Subir la imagen primero
                String imageUploadUrl = ApiService.BASE_URL + "upload_image.php";  // URL del script de subida
                URL url = new URL(imageUploadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=*****");
                connection.setRequestProperty("Connection", "Keep-Alive");

                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                dos.writeBytes("--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n");
                dos.writeBytes("\r\n");
                dos.write(imageBytes);
                dos.writeBytes("\r\n");
                dos.writeBytes("--*****--\r\n");

                dos.flush();
                dos.close();

                // Leer la respuesta del servidor después de subir la imagen
                InputStream responseStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parsear la respuesta para obtener la URL de la imagen subida
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("status").equals("success")) {
                    imageUrl = jsonResponse.getString("image_url");  // URL de la imagen subida
                } else {
                    return null;  // Error al subir la imagen
                }

                // Ahora, enviar los datos del reporte junto con la URL de la imagen
                URL reportUrl = new URL(ApiService.BASE_URL + "report_data.php");
                HttpURLConnection reportConnection = (HttpURLConnection) reportUrl.openConnection();
                reportConnection.setRequestMethod("POST");
                reportConnection.setDoOutput(true);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String fechaHora = sdf.format(new Date());
                String descripcion = textArea.getText().toString();

                // Construir los parámetros del reporte con la URL de la imagen
                String postData = "idUsuario=" + idUsuario + "&fecha=" + fechaHora + "&descripcion=" + descripcion + "&imagen_url=" + imageUrl;  // Cambié 'imagen' a 'imagen_url'

                // Enviar los datos del reporte
                OutputStream os = reportConnection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = reportConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "Reporte enviado exitosamente";
                } else {
                    return "Error al enviar el reporte";
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
                Toast.makeText(ReportarUI.this, result, Toast.LENGTH_SHORT).show();
                textArea.setText("");
                imgvReporte.setImageResource(android.R.color.transparent);
                imageBitmap = null;
                imageUrl = null;  // Limpiar la URL después de enviar
                textArea.clearFocus();
            } else {
                Toast.makeText(ReportarUI.this, "Error al enviar reporte", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        String userName = username;
        Intent cargaIntent = new Intent(ReportarUI.this, CargaUI.class);
        startActivity(cargaIntent);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(ReportarUI.this, UserUI.class);
            intent.putExtra("username", userName);
            startActivity(intent);
        }, 500);
    }
}
