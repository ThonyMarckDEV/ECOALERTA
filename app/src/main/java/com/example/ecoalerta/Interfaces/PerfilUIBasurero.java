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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class PerfilUIBasurero extends AppCompatActivity {

    private String username;
    private EditText txtNombresBasurero, txtApellidosBasurero, txtCorreoBasurero;
    private ImageView imgvPerfilBasurero;
    private ImageView imgvLoadingBasurero;
    private Button btnEditarCorreoBasurero, btnEditarNombresBasurero, btnEditarApellidosBasurero, btnActualizaPerfilBasurero;

    // Variables para controlar la editabilidad de los campos
    private boolean isEmailEditable = false;
    private boolean areNamesEditable = false;
    private boolean areSurnamesEditable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_ui_basurero);

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

        // Referencias a los campos EditText y otros elementos
        txtCorreoBasurero = findViewById(R.id.txtCorreoPerfilBasurero);
        txtNombresBasurero = findViewById(R.id.txtNombresPerfilBasurero);
        txtApellidosBasurero = findViewById(R.id.txtApellidosPerfilBasurero);
        imgvPerfilBasurero = findViewById(R.id.imgvPerfilBasurero);
        imgvLoadingBasurero = findViewById(R.id.imgvLoadingBasurero);

        btnEditarCorreoBasurero = findViewById(R.id.btnEditarCorreoBasurero);
        btnEditarNombresBasurero = findViewById(R.id.btnEditarNombresBasurero);
        btnEditarApellidosBasurero = findViewById(R.id.btnEditarApellidosBasurero);
        btnActualizaPerfilBasurero = findViewById(R.id.btnActualizaPerfilBasurero);

//===================================================================================
        // Usar CLASE PerfilImagenLoader para cargar la imagen de perfil
        /**
         * SE USO LA CLASE PerfilImagenLoader PARA CARGAR LA FOTO
         */
        PerfilImagenLoader perfilLoader = new PerfilImagenLoader(this, imgvLoadingBasurero, imgvPerfilBasurero);
        perfilLoader.cargarImagen(username);
//===================================================================================


//===================================================================================
        // Usar CLASE ObtenerProfileTask para cargar la imagen de perfil
        /**
         * SE USO LA CLASE ObtenerProfileTask PARA CARGAR LA FOTO
         */
        new ObtenerProfileTask(txtNombresBasurero, txtApellidosBasurero, txtCorreoBasurero).execute(username);
//===================================================================================

// Deshabilitar los campos al inicio
        setFieldsEditable(false);

// Hacer los campos editables o no al presionar los botones correspondientes
        btnEditarCorreoBasurero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEmailEditable = !isEmailEditable; // Alternar el estado
                txtCorreoBasurero.setEnabled(isEmailEditable); // Habilitar o deshabilitar campo de correo
            }
        });

        btnEditarNombresBasurero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                areNamesEditable = !areNamesEditable; // Alternar el estado
                txtNombresBasurero.setEnabled(areNamesEditable); // Habilitar o deshabilitar campo de nombres
            }
        });

        btnEditarApellidosBasurero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                areSurnamesEditable = !areSurnamesEditable; // Alternar el estado
                txtApellidosBasurero.setEnabled(areSurnamesEditable); // Habilitar o deshabilitar campo de apellidos
            }
        });

        // Mostrar el GIF de carga
        if (imgvLoadingBasurero != null) {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.loadingperfil)
                    .into(imgvLoadingBasurero);
        }

        //===================================================================================
        // Usar CLASE actualizarDatos para cargar la imagen de perfil
        /**
         * SE USO LA CLASE actualizarDatos PARA ACTUALIZAR DATOS DE LOS TXT
         */
        // Actualizar perfil al hacer clic en el botón de actualizar
        btnActualizaPerfilBasurero.setOnClickListener(view -> {
            String nombres = txtNombresBasurero.getText().toString();
            String apellidos = txtApellidosBasurero.getText().toString();
            String correo = txtCorreoBasurero.getText().toString();
            setFieldsEditable(false);
            ActualizarDatosBasurero.actualizarDatos(PerfilUIBasurero.this, username, nombres, apellidos, correo);
        });
        //===================================================================================
    }

    public void setFieldsEditable(boolean editable) {
        txtNombresBasurero.setEnabled(editable);
        txtApellidosBasurero.setEnabled(editable);
        txtCorreoBasurero.setEnabled(editable);
    }


    @Override
    public void onBackPressed() {
        // Hacer la solicitud para obtener el rol del usuario
        redirigir();
    }

    private void redirigir() {
        // Iniciar la actividad de carga
        Intent cargaIntent = new Intent(PerfilUIBasurero.this, CargaUI.class);
        startActivity(cargaIntent);

        // Usar un Handler para esperar un breve periodo y luego redirigir
        new Handler().postDelayed(() -> {
            // Crear la intención para redirigir a UserUI
            Intent intent = new Intent(PerfilUIBasurero.this, UserUI.class); // Clase para el rol Usuario

            // Agregar el username como extra
            intent.putExtra("username", username);
            startActivity(intent);

            // Cerrar la actividad de carga y la actual
            finish();
        }, 2000); // Esperar 2000 ms (2 segundos)
    }

}