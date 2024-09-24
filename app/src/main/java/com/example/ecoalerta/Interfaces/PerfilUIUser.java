package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ecoalerta.R;

public class PerfilUIUser extends AppCompatActivity {

    private String username;
    private EditText txtNombres, txtApellidos, txtCorreo;
    private ImageView imgvPerfil;
    private ImageView imgvLoading;
    private Button btnEditarCorreo, btnEditarNombres, btnEditarApellidos, btnActualizaPerfil,btnfaq;

    // Variables para controlar la editabilidad de los campos
    private boolean isEmailEditable = false;
    private boolean areNamesEditable = false;
    private boolean areSurnamesEditable = false;

    private AnuncioChecker anuncioChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_ui_user);


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
        VerificadorDeAnuncio verificadorAnuncio = new VerificadorDeAnuncio(this, username);
        verificadorAnuncio.iniciarVerificacion();
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

        //===================================================================================
        // Usar CLASE actualizarDatos para cargar la imagen de perfil
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
