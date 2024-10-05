package com.example.ecoalerta.Interfaces;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ecoalerta.Clases.AnuncioService;
import com.example.ecoalerta.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PerfilUIUser extends AppCompatActivity {

    private static final int SELECT_PICTURE = 1;
    private Uri selectedImageUri;
    private Bitmap imageBitmap;  // Declaramos imageBitmap aquí a nivel de clase

    private String username;
    private EditText txtNombres, txtApellidos, txtCorreo;
    private ImageView imgvPerfil;
    private ImageView imgvLoading;
    private Button btnEditarCorreo, btnEditarNombres, btnEditarApellidos, btnActualizaPerfil,btnfaq,btnSubirFoto;

    // Variables para controlar la editabilidad de los campos
    private boolean isEmailEditable = false;
    private boolean areNamesEditable = false;
    private boolean areSurnamesEditable = false;

    private ProgressDialog progressDialog; // Para mostrar el diálogo de progreso
    private AnuncioChecker anuncioChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_ui_user);

        // Inicializar el ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Actualizando foto de perfil...");
        progressDialog.setCancelable(false);

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

        // Obtener el username del Intent
        username = getIntent().getStringExtra("username");

        //===================================================================================
        /**
         * VERIRICADOR DE ANUNCIO
         */
        // En tu actividad o fragmento
        Intent serviceIntent = new Intent(this, AnuncioService.class);
        serviceIntent.putExtra("idUsuario", username);  // Pasar el idUsuario o username al servicio
        startForegroundService(serviceIntent);  // Para Android O y superior
        //===================================================================================

        // Referencias a los campos EditText y otros elementos
        txtCorreo = findViewById(R.id.txtCorreoPerfil);
        txtNombres = findViewById(R.id.txtPasswordPerfil);
        txtApellidos = findViewById(R.id.txtApellidosPerfil);
        imgvPerfil = findViewById(R.id.imgvPerfil);
        imgvLoading = findViewById(R.id.imgvLoading);

        btnEditarCorreo = findViewById(R.id.btnEditarCorreo);
        btnEditarNombres = findViewById(R.id.btnEditarNombres);
        btnEditarApellidos = findViewById(R.id.btnEditarApellidos);
        btnActualizaPerfil = findViewById(R.id.btnActualizaPerfil);
        btnSubirFoto = findViewById(R.id.btnSubirFotoPerfil);
        btnfaq = findViewById(R.id.btnFAQ);
//===================================================================================
        // Usar CLASE PerfilImagenLoader para cargar la imagen de perfil
        /**
         * SE USO LA CLASE PerfilImagenLoader PARA CARGAR LA FOTO
         */
        PerfilImagenLoader perfilLoader = new PerfilImagenLoader(this, imgvLoading, imgvPerfil);
        perfilLoader.cargarImagen(username);
//===================================================================================


//===================================================================================
        // Usar CLASE ObtenerProfileTask para cargar los datos
        /**
         * SE USO LA CLASE ObtenerProfileTask cargar los datos
         */
        new ObtenerProfileTask(txtNombres, txtApellidos, txtCorreo).execute(username);
//===================================================================================

// Deshabilitar los campos al inicio
        setFieldsEditable(false);

// Hacer los campos editables o no al presionar los botones correspondientes
        btnEditarCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEmailEditable = !isEmailEditable; // Alternar el estado
                txtCorreo.setEnabled(isEmailEditable); // Habilitar o deshabilitar campo de correo
            }
        });

        btnEditarNombres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                areNamesEditable = !areNamesEditable; // Alternar el estado
                txtNombres.setEnabled(areNamesEditable); // Habilitar o deshabilitar campo de nombres
            }
        });

        btnEditarApellidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                areSurnamesEditable = !areSurnamesEditable; // Alternar el estado
                txtApellidos.setEnabled(areSurnamesEditable); // Habilitar o deshabilitar campo de apellidos
            }
        });

        // Mostrar el GIF de carga
        if (imgvLoading != null) {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.loadingperfil)
                    .into(imgvLoading);
        }


        btnSubirFoto.setOnClickListener(v -> {
            // Abrir galería para seleccionar una imagen
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), SELECT_PICTURE);
        });

        //===================================================================================
        // Usar CLASE actualizarDatos para ACTUALIZAR DATOS DE LOS TXT
        /**
         * SE USO LA CLASE actualizarDatos PARA ACTUALIZAR DATOS DE LOS TXT
         */
        // Actualizar perfil al hacer clic en el botón de actualizar
        btnActualizaPerfil.setOnClickListener(view -> {
            String nombres = txtNombres.getText().toString();
            String apellidos = txtApellidos.getText().toString();
            String correo = txtCorreo.getText().toString();
            setFieldsEditable(false);
            ActualizarDatosUser.actualizarDatos(PerfilUIUser.this, username, nombres, apellidos, correo);
        });
        //===================================================================================

        // Configurar el botón reporte
        if (username != null) {
            btnfaq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent = new Intent(PerfilUIUser.this, FAQUI.class);
                    startActivity(mapIntent);
                }
            });
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            try {
                // Convertir la imagen seleccionada en un Bitmap
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                // Mostrar ProgressDialog y llamar al AsyncTask para subir la imagen
                progressDialog.show();
                new EnviarImagenTask().execute();

                // Llamar al AsyncTask para subir la imagen
                new EnviarImagenTask().execute();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al seleccionar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class EnviarImagenTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                // Convertir la imagen en un array de bytes
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Subir la imagen al servidor
                String imageUploadUrl = ApiService.BASE_URL + "update_foto_perfil.php";  // Cambia por tu URL

                URL url = new URL(imageUploadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=*****");

                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                dos.writeBytes("--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n");
                dos.writeBytes("\r\n");
                dos.write(imageBytes);
                dos.writeBytes("\r\n");
                dos.writeBytes("--*****--\r\n");

                dos.flush();
                dos.close();

                // Leer la respuesta del servidor
                InputStream responseStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(responseStream);
                StringBuilder response = new StringBuilder();
                int data;
                while ((data = reader.read()) != -1) {
                    response.append((char) data);
                }
                reader.close();

                // Parsear la respuesta JSON para obtener la URL de la imagen
                JSONObject jsonResponse = new JSONObject(response.toString());
                String imageUrl = jsonResponse.getString("image_url");

                // Si la imagen fue subida correctamente, actualiza la URL en la base de datos
                return actualizarURLPerfil(imageUrl);

            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Ocultar el ProgressDialog cuando se completa el proceso
            progressDialog.dismiss();

            if ("success".equals(result)) {
                Toast.makeText(PerfilUIUser.this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();
                finish();  // Cierra la actividad actual después de iniciar la nueva
            } else {
                Toast.makeText(PerfilUIUser.this, "Error al subir la imagen ", Toast.LENGTH_SHORT).show();
            }
        }

        // Método para enviar la URL de la imagen al servidor para actualizar el perfil
        private String actualizarURLPerfil(String imageUrl) {
            try {
                // URL del servidor para actualizar el perfil del usuario
                String updateUrl = ApiService.BASE_URL + "upload_url_img.php";  // Cambia por tu URL

                URL url = new URL(updateUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Crear los parámetros POST
                String postData = "username=" + username + "&imagen_url=" + imageUrl;

                // Enviar los datos al servidor
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                dos.writeBytes(postData);
                dos.flush();
                dos.close();

                // Leer la respuesta del servidor
                InputStream responseStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(responseStream);
                StringBuilder response = new StringBuilder();
                int data;
                while ((data = reader.read()) != -1) {
                    response.append((char) data);
                }
                reader.close();

                // Parsear la respuesta JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getString("status");

            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
        }

    }


    public void setFieldsEditable(boolean editable) {
        txtNombres.setEnabled(editable);
        txtApellidos.setEnabled(editable);
        txtCorreo.setEnabled(editable);
    }

    @Override
    public void onBackPressed() {
        // Hacer la solicitud para obtener el rol del usuario
        redirigir();
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
}
