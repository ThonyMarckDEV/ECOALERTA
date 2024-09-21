package com.example.ecoalerta.Interfaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class EstadoUsuarioVerificador {
    private Context context;
    private String username;

    public EstadoUsuarioVerificador(Context context) {
        this.context = context;
    }

    public void verificarEstado() {
        // Obtener el nombre de usuario desde SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        username = preferences.getString("username", null);

        if (username != null) {
            // Hacer la petición al servidor para verificar el estado
            new Thread(() -> {
                try {
                    // URL del PHP para obtener el estado
                    String urlString = "https://modern-blindly-kangaroo.ngrok-free.app/PHP/get_user_status.php?username=" + URLEncoder.encode(username, "UTF-8");
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Leer la respuesta del servidor
                    InputStream is = connection.getInputStream();
                    Scanner scanner = new Scanner(is).useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();
                    is.close();

                    // Procesar la respuesta JSON
                    JSONObject jsonResponse = new JSONObject(response);
                    String status = jsonResponse.optString("status", "loggedOff"); // Valor por defecto

                    // Verificar si el estado es loggedOff
                    if (status.equals("loggedOff")) {
                        // Cerrar sesión y redirigir
                        cerrarSesion();
                    }else{
                        Toast.makeText(null, "USUARIO YA LOGEADO", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start(); // Ejecuta en un hilo separado
        }
    }

    private void cerrarSesion() {
        // Limpiar el nombre de usuario en SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("username");
        editor.apply();

        // Redirigir a LoginUI
        Intent intent = new Intent(context, LoginUI.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Necesario si se llama desde un hilo
        context.startActivity(intent);
    }
}
