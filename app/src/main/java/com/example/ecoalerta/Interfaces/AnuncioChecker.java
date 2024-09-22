package com.example.ecoalerta.Interfaces;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.ecoalerta.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AnuncioChecker {

    private Context context;
    private Handler handler;
    private Runnable runnable;
    private int idUsuario; // Variable para el idUsuario
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ANUNCIOS";
    private boolean anuncioMostrado = false;

    // Constructor que recibe idUsuario
    public AnuncioChecker(Context context, int idUsuario) {
        this.context = context;
        this.idUsuario = idUsuario; // Guardar el idUsuario
        this.handler = new Handler();
    }

    public void iniciarVerificacion() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!anuncioMostrado) {
                    new VerificarAnuncioTask().execute();
                    handler.postDelayed(this, 10000);  // Ejecutar cada 10 segundos
                }
            }
        };
        handler.post(runnable);
    }

    public void detenerVerificacion() {
        handler.removeCallbacks(runnable);  // Detener la verificación
    }

    private class VerificarAnuncioTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(ApiService.BASE_URL + "obtener_anuncio.php?id_usuario=" + idUsuario);  // Usar el idUsuario
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

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
            if (result != null && !result.contains("no_data")) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String titulo = jsonObject.getString("titulo");
                    String mensaje = jsonObject.getString("mensaje");

                    mostrarNotificacion(titulo, mensaje);

                    anuncioMostrado = true;  // Detener el ciclo de verificación
                    detenerVerificacion();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void mostrarNotificacion(String titulo, String mensaje) {
        new Handler(Looper.getMainLooper()).post(() -> {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                Log.e("Notificación", "No se pudo obtener el NotificationManager");
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notificaciones de Anuncio", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            PendingIntent emptyIntent = PendingIntent.getActivity(
                    context,
                    0,
                    new Intent(),
                    PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.eco_alerta_logo)
                    .setContentTitle(titulo)
                    .setContentText(mensaje)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(emptyIntent)
                    .setAutoCancel(true);

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        });
    }
}

