package com.example.ecoalerta.Clases;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.ecoalerta.Clases.LocationService;
import com.example.ecoalerta.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocationForegroundService extends Service {

    public static final String CHANNEL_ID = "LocationForegroundServiceChannel";
    private LocationService locationService;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializa FusedLocationProviderClient y Firestore
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Inicializa el servicio de ubicación
        locationService = new LocationService(this, fusedLocationClient, db);
        locationService.startLocationUpdates(); // Inicia las actualizaciones de ubicación
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("EcoAlerta")
                .setContentText("Compartiendo ubicación en segundo plano.")
                .setSmallIcon(R.drawable.eco_alerta_logo)
                .build();

        startForeground(1, notification); // Comienza el servicio en primer plano
        return START_STICKY;  // El servicio seguirá corriendo hasta que lo detengas explícitamente
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationService.stopLocationUpdates(); // Detén las actualizaciones de ubicación cuando se destruya el servicio
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Ubicación en segundo plano",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}

