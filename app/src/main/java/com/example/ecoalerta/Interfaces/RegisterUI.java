package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecoalerta.R;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterUI extends AppCompatActivity {

    private EditText txtUsername, txtNombres, txtApellidos, txtEmail, txtPassword;
    private Button btnRegistrarse;
    private Intent cargaIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Referencias a los campos del formulario
        txtUsername = findViewById(R.id.txtUsernameRegister);
        txtNombres = findViewById(R.id.txtNombresRegister);
        txtApellidos = findViewById(R.id.txtApellidosRegister);
        txtEmail = findViewById(R.id.txtEmailRegister);
        txtPassword = findViewById(R.id.txtPasswordRegister);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);

        // Inicializar la Intent para la pantalla de carga
        cargaIntent = new Intent(RegisterUI.this, CargaUI.class);

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validar campos si es necesario antes de registrar
                new RegisterTask().execute(
                        txtUsername.getText().toString(),
                        txtNombres.getText().toString(),
                        txtApellidos.getText().toString(),
                        txtEmail.getText().toString(),
                        txtPassword.getText().toString()
                );
            }
        });
    }

    private class RegisterTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Mostrar la pantalla de carga
            startActivity(cargaIntent);
        }

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String nombres = params[1];
            String apellidos = params[2];
            String email = params[3];
            String password = params[4];

            try {
                URL url = new URL(ApiService.BASE_URL + "register.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Enviar los datos al servidor
                String postData = "username=" + username +
                        "&nombres=" + nombres +
                        "&apellidos=" + apellidos +
                        "&email=" + email +
                        "&password=" + password;

                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "Registro exitoso";
                } else {
                    return "Error en el registro";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Excepción: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Ocultar la pantalla de carga
            Intent intent = new Intent(RegisterUI.this, LoginUI.class);
            startActivity(intent);

            // Opcional: cerrar la actividad actual
            finish();

            // Mostrar el resultado
            Toast.makeText(RegisterUI.this, result, Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onBackPressed() {
        // Mostrar la pantalla de carga antes de volver a LoginUI
        Intent cargaIntent = new Intent(RegisterUI.this, CargaUI.class);
        startActivity(cargaIntent);

        // Usar Handler para retrasar el inicio de LoginUI y permitir que la pantalla de carga se muestre
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Asegúrate de que la pantalla de carga esté cerrada antes de iniciar LoginUI
                Intent intent = new Intent(RegisterUI.this, LoginUI.class);
                startActivity(intent);
                finish(); // Cerrar la actividad actual para que el usuario no vuelva a ella
            }
        }, 500); // Esperar 500 ms antes de iniciar LoginUI

        // Opcional: Si quieres que el back button no vuelva a la actividad actual
        // finish(); // Descomenta esta línea si quieres cerrar la actividad actual
    }
}
