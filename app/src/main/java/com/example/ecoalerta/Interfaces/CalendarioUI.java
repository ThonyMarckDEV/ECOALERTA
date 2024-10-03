package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ecoalerta.R;

public class CalendarioUI extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendario_ui);

        // Asignar OnClickListener a cada botón
        Button btnRuta1 = findViewById(R.id.btn_ruta1);
        Button btnRuta2 = findViewById(R.id.btn_ruta2);
        Button btnRuta3 = findViewById(R.id.btn_ruta3);
        Button btnRuta4 = findViewById(R.id.btn_ruta4);

        // Listener para la primera ruta
        btnRuta1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarioUI.this, RutaDetailsActivity.class);
                intent.putExtra("ruta_seleccionada", 1); // Pasamos el identificador de la ruta
                startActivity(intent);
            }
        });

        // Listener para la segunda ruta
        btnRuta2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarioUI.this, RutaDetailsActivity.class);
                intent.putExtra("ruta_seleccionada", 2); // Pasamos el identificador de la ruta
                startActivity(intent);
            }
        });

        // Listener para la tercera ruta
        btnRuta3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarioUI.this, RutaDetailsActivity.class);
                intent.putExtra("ruta_seleccionada", 3); // Pasamos el identificador de la ruta
                startActivity(intent);
            }
        });

        // Listener para la cuarta ruta
        btnRuta4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarioUI.this, RutaDetailsActivity.class);
                intent.putExtra("ruta_seleccionada", 4); // Pasamos el identificador de la ruta
                startActivity(intent);
            }
        });
    }
}