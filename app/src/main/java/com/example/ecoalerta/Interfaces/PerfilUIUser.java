package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ecoalerta.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PerfilUIUser extends AppCompatActivity {

    private String username;
    private EditText txtNombres, txtApellidos, txtCorreo;
    private ImageView imgvPerfil;
    private ImageView imgvLoading;
    private Button btnEditarCorreo, btnEditarNombres, btnEditarApellidos, btnActualizaPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_ui_user);

        // Obtener el username del Intent
        username = getIntent().getStringExtra("username");

        // Referencias a los campos EditText y otros elementos
        txtCorreo = findViewById(R.id.txtCorreoPerfil);
        txtNombres = findViewById(R.id.txtNombresPerfil);
        txtApellidos = findViewById(R.id.txtApellidosPerfil);
        imgvPerfil = findViewById(R.id.imgvPerfil);
        imgvLoading = findViewById(R.id.imgvLoading);

        btnEditarCorreo = findViewById(R.id.btnEditarCorreo);
        btnEditarNombres = findViewById(R.id.btnEditarNombres);
        btnEditarApellidos = findViewById(R.id.btnEditarApellidos);
        btnActualizaPerfil = findViewById(R.id.btnActualizaPerfil);

        // Cargar imagen de perfil
        cargarImagenPerfil(username);

        // Hacer la solicitud POST para obtener los datos del perfil
        new GetProfileTask().execute(username);

        // Deshabilitar los campos al inicio
          setFieldsEditable(false);

        // Hacer los campos editables al presionar los botones correspondientes
        btnEditarCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtCorreo.setEnabled(true); // Habilitar campo de correo
            }
        });

        btnEditarNombres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtNombres.setEnabled(true); // Habilitar campo de nombres
            }
        });

        btnEditarApellidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtApellidos.setEnabled(true); // Habilitar campo de apellidos
            }
        });

        // Mostrar el GIF de carga
        if (imgvLoading != null) {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.loadingperfil)
                    .into(imgvLoading);
        }

        // Actualizar perfil al hacer clic en el botón de actualizar
        btnActualizaPerfil.setOnClickListener(view -> {
            String nombres = txtNombres.getText().toString();
            String apellidos = txtApellidos.getText().toString();
            String correo = txtCorreo.getText().toString();
            enviarDatos(username,nombres,apellidos,correo);
        });
    }

    private void enviarDatos(String username, String nombres, String apellidos, String correo) {
        // Verificar que los campos no estén vacíos
        if (nombres.isEmpty() || apellidos.isEmpty() || correo.isEmpty()) {
            Toast.makeText(PerfilUIUser.this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "https://modern-blindly-kangaroo.ngrok-free.app/PHP/update_profile.php"; // Cambia a la URL de tu servidor PHP

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response); // Imprime la respuesta en el log
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                Toast.makeText(PerfilUIUser.this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                                setFieldsEditable(false);
                            } else {
                                String errorMessage = jsonResponse.optString("message", "Error desconocido");
                                Toast.makeText(PerfilUIUser.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(PerfilUIUser.this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Manejar el error
                        Toast.makeText(PerfilUIUser.this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                        setFieldsEditable(false);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("nombres", nombres);
                params.put("apellidos", apellidos);
                params.put("correo", correo);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void setFieldsEditable(boolean editable) {
        txtNombres.setEnabled(editable);
        txtApellidos.setEnabled(editable);
        txtCorreo.setEnabled(editable);
    }

    private void cargarImagenPerfil(String username) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/get_profile_picture.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);

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

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("status").equals("success")) {
                    String perfilBase64 = jsonResponse.getString("perfil");
                    byte[] decodedString = Base64.decode(perfilBase64.split(",")[1], Base64.DEFAULT);
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    runOnUiThread(() -> {
                        if (imgvLoading != null) {
                            imgvLoading.setVisibility(View.GONE);
                        }

                        Glide.with(PerfilUIUser.this)
                                .load(bitmap)
                                .apply(RequestOptions.circleCropTransform())
                                .into(imgvPerfil);
                    });
                } else {
                    String errorMessage = jsonResponse.optString("message", "Error desconocido");
                    System.err.println("Error del servidor: " + errorMessage);
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
        }).start();
    }

    @Override
    public void onBackPressed() {
        // Hacer la solicitud para obtener el rol del usuario
        redirigir();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Glide.with(this).clear(imgvPerfil);

        String username = getIntent().getStringExtra("username");
        if (username != null) {
            updateStatus(username);
        }
    }

    private void updateStatus(String username) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // URL del archivo PHP
                    URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/update_status.php");
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

    private void redirigir() {
        // Iniciar la actividad de carga
        Intent cargaIntent = new Intent(PerfilUIUser.this, CargaUI.class);
        startActivity(cargaIntent);

        // Usar un Handler para esperar un breve periodo y luego redirigir
        new Handler().postDelayed(() -> {
            // Crear la intención para redirigir a UserUI
            Intent intent = new Intent(PerfilUIUser.this, UserUI.class); // Clase para el rol Usuario

            // Agregar el username como extra
            intent.putExtra("username", username);
            startActivity(intent);

            // Cerrar la actividad de carga y la actual
            finish();
        }, 2000); // Esperar 2000 ms (2 segundos)
    }


    private class GetProfileTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String result = "";
            try {
                String username = params[0];
                URL url = new URL("https://modern-blindly-kangaroo.ngrok-free.app/PHP/perfil.php");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String postData = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                result = response.toString();
            } catch (Exception e) {
                Log.e("PerfilUI", "Error: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("error")) {
                    Toast.makeText(PerfilUIUser.this, jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                } else {
                    txtNombres.setText(jsonObject.getString("nombres"));
                    txtApellidos.setText(jsonObject.getString("apellidos"));
                    txtCorreo.setText(jsonObject.getString("correo"));
                }
            } catch (JSONException e) {
                Log.e("PerfilUI", "Error al parsear el JSON: " + e.getMessage());
            }
        }
    }
}
