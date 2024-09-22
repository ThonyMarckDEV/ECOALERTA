package com.example.ecoalerta.Interfaces;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class VerificadorDeAnuncio {

    private Context context;
    private String username;
    private int idUsuario;
    private AnuncioChecker anuncioChecker;

    public VerificadorDeAnuncio(Context context, String username) {
        this.context = context;
        this.username = username;
    }

    public void iniciarVerificacion() {
        obtenerIdUsuario(username);
    }

    /**
     * VERIFICADOR DE ANUNCIO
     */
    private void obtenerIdUsuario(String username) {
        new ObtenerIdUsuarioTask().execute(username);
    }

    // AsyncTask para obtener el idUsuario
    private class ObtenerIdUsuarioTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            String username = params[0];
            try {
                // URL de tu PHP
                URL url = new URL(ApiService.BASE_URL + "get_user_id.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Enviar el username al servidor
                String postData = "username=" + URLEncoder.encode(username, "UTF-8");
                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                // Leer la respuesta del servidor
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String response = reader.readLine(); // Leer la respuesta (idUsuario)

                reader.close();
                connection.disconnect();

                // Retornar el idUsuario
                return Integer.parseInt(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer id) {
            if (id != null) {
                idUsuario = id;
                // Iniciar el AnuncioChecker con el idUsuario
                iniciarAnuncioChecker(idUsuario);
            } else {
                // Manejar error, puedes mostrar algún mensaje
                Toast.makeText(context, "Error al obtener idUsuario", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void iniciarAnuncioChecker(int idUsuario) {
        // Inicializar el AnuncioChecker con idUsuario
        anuncioChecker = new AnuncioChecker(context, idUsuario);

        // Iniciar la verificación de anuncios
        anuncioChecker.iniciarVerificacion();
    }
}