package com.example.ecoalerta.Interfaces;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecoalerta.R;
import com.squareup.picasso.Picasso;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.SharedPreferences;

public class GestionarReporteUI extends AppCompatActivity {

    private TextView tvIdReporte, tvIdUsuario, tvFecha, tvDescripcion;
    private ImageView imgvReporte;
    private Button btnRevisar;
    private ProgressDialog progressDialog;  // Declarar el ProgressDialog
    private String idReporte, idUsuario, fecha, descripcion, imagenUrl;
    private String username; // Declara username aquí

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestionar_reporte_ui);

        // Inicializa el username desde el Intent
        username = getIntent().getStringExtra("username");

        // Vincular los componentes UI con el layout
        tvIdReporte = findViewById(R.id.tvIdReporte);
        tvIdUsuario = findViewById(R.id.tvIdUsuario);
        tvFecha = findViewById(R.id.tvFecha);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        imgvReporte = findViewById(R.id.imgvReporte);
        btnRevisar = findViewById(R.id.btnRevisarReporte);

        // Obtener los datos del Intent
        Intent intent = getIntent();
        idReporte = intent.getStringExtra("idReporte");
        idUsuario = intent.getStringExtra("idUsuario");
        fecha = intent.getStringExtra("fecha");
        descripcion = intent.getStringExtra("descripcion");
        imagenUrl = intent.getStringExtra("imagenUrl");

        // Mostrar los datos en la UI
        tvIdReporte.setText("ID Reporte: " + idReporte);
        tvIdUsuario.setText("ID Usuario: " + idUsuario);
        tvFecha.setText("Fecha: " + fecha);
        tvDescripcion.setText("Descripción: " + descripcion);

        // Cargar la imagen en el ImageView usando Picasso
        Picasso.get().load(imagenUrl).into(imgvReporte);

        // Manejar el botón Revisar
        btnRevisar.setOnClickListener(v -> {
            // Mostrar el ProgressDialog
            progressDialog = new ProgressDialog(GestionarReporteUI.this);
            progressDialog.setMessage("Revisando reporte...");
            progressDialog.setCancelable(false); // Evitar que se cancele tocando fuera del diálogo
            progressDialog.show();
            // Llamar a una tarea asíncrona para mover el reporte a reportes_revisados
            new RevisarReporteTask().execute();
        });
    }

    private class RevisarReporteTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(ApiService.BASE_URL + "mover_a_revisados.php?idReporte=" + idReporte);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "Reporte revisado con éxito";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Error al revisar el reporte";
        }

        @Override
        protected void onPostExecute(String result) {
            // Cerrar el ProgressDialog cuando la tarea se complete
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            Toast.makeText(GestionarReporteUI.this, result, Toast.LENGTH_SHORT).show();

            if (result.equals("Reporte revisado con éxito")) {

                // Pasar el username al Intent
                Intent intent = new Intent(GestionarReporteUI.this, AdminUI.class);
                intent.putExtra("username", username);
                startActivity(intent);  // Inicia la actividad AdminUI
                finish();  // Opcional: Cierra la actividad actual
            }
        }
    }
}
