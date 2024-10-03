package com.example.ecoalerta.Interfaces;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ecoalerta.R;

public class RutaDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ruta_details);


        // Recuperamos el identificador de la ruta seleccionada
        int rutaSeleccionada = getIntent().getIntExtra("ruta_seleccionada", 0);

        // Referencia al ImageView donde mostraremos la imagen de la ruta
        ImageView imageViewRuta = findViewById(R.id.imageViewRuta);

        // Cambiamos la imagen según la ruta seleccionada
        switch (rutaSeleccionada) {
            case 1:
                imageViewRuta.setImageResource(R.drawable.ruta1_imagen);
                break;
            case 2:
                imageViewRuta.setImageResource(R.drawable.ruta2_imagen);
                break;
            case 3:
                imageViewRuta.setImageResource(R.drawable.ruta3_imagen);
                break;
            case 4:
                imageViewRuta.setImageResource(R.drawable.ruta4_imagen);
                break;
            default:
                // Imagen por defecto si no hay ruta seleccionada
                imageViewRuta.setImageResource(R.drawable.ruta);
                break;
        }
    }
}