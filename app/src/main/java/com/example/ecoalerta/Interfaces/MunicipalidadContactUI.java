package com.example.ecoalerta.Interfaces;

import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ecoalerta.R;

public class MunicipalidadContactUI extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_municipalidad_contact_ui);

        // =================================================================================
        /**
         * Verificador Sesion cada 10 seg
         */
        EstadoUsuarioVerificador verificador = new EstadoUsuarioVerificador(this);
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                verificador.verificarEstado();
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);
        // =================================================================================
    }
}