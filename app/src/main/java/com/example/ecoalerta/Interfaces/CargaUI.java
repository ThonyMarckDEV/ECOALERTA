package com.example.ecoalerta.Interfaces;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.Target;
import com.example.ecoalerta.R;

public class CargaUI extends AppCompatActivity {

    private ImageView imgvLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carga);

        imgvLoading = findViewById(R.id.transitiongif);

        // Cargar el GIF y mostrarlo como un drawable animado
        Glide.with(this)
                .asGif()
                .load(R.drawable.ecoalerta)
                .listener(new com.bumptech.glide.request.RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false; // Maneja el error si es necesario
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        // Detener la animación y mostrar el último frame después de un tiempo
                        resource.setLoopCount(1); // Reproducir solo una vez
                        resource.start();
                        return false; // Permitir que Glide maneje el drawable
                    }
                })
                .into(imgvLoading);
    }
}
