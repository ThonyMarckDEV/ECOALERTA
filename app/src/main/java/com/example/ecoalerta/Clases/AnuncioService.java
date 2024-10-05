package com.example.ecoalerta.Clases;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.ecoalerta.Interfaces.AnuncioChecker;
import com.example.ecoalerta.R;

public class AnuncioService extends Service {

    private static final String CHANNEL_ID = "ANUNCIO_SERVICE_CHANNEL";
    private Handler handler;
    private AnuncioChecker anuncioChecker;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int idUsuario = intent.getIntExtra("idUsuario", -1);

        // Crear el canal de notificación para el servicio en primer plano
        createNotificationChannel();

        // Iniciar el servicio en primer plano
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("EcoAlerta")
                .setContentText("Verificando anuncios...")
                .setSmallIcon(R.drawable.eco_alerta_logo)
                .build();
        startForeground(1, notification);

        // Iniciar AnuncioChecker en segundo plano
        anuncioChecker = new AnuncioChecker(this, idUsuario);
        anuncioChecker.iniciarVerificacion();

        return START_STICKY;  // Mantiene el servicio en ejecución si es detenido
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (anuncioChecker != null) {
            anuncioChecker.detenerVerificacion();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;  // No vamos a enlazar el servicio, solo ejecutarlo en segundo plano
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal de Servicio de Anuncios",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
