package com.example.ecoalerta.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.ecoalerta.R;
import com.squareup.picasso.Picasso;  // Asegúrate de tener Picasso o Glide

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import android.widget.ProgressBar;


// Clase principal para mostrar los reportes
public class ListarReportesUI extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReporteAdapter reporteAdapter;
    private List<Reporte> reporteList;
    private String url = ApiService.BASE_URL + "listarReportes.php"; // Cambia por tu URL PHP
    private ProgressBar progressBar; // Añadir ProgressBar
    private TextView tvNoReportes;  // Nuevo TextView para mostrar el mensaje cuando no haya reportes
    private String username; // Declara username aquí

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listar_reportes_ui);


        // Inicializa el username desde el Intent
        username = getIntent().getStringExtra("username");

        recyclerView = findViewById(R.id.recyclerViewReportes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar ProgressBar
        progressBar = findViewById(R.id.progressBar);
        tvNoReportes = findViewById(R.id.tvNoReportes);  // Vincular el TextView

        reporteList = new ArrayList<>();
        reporteAdapter = new ReporteAdapter(reporteList);
        recyclerView.setAdapter(reporteAdapter);

        cargarReportes();
    }


    private void cargarReportes() {
        // Mostrar el ProgressBar antes de iniciar la carga
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE); // Ocultar el RecyclerView temporalmente
        tvNoReportes.setVisibility(View.GONE); // Ocultar el mensaje por si estaba visible

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        Reporte reporte = new Reporte(
                                jsonObject.getString("idReporte"),
                                jsonObject.getString("idUsuario"),
                                jsonObject.getString("fecha"),
                                jsonObject.getString("descripcion"),
                                jsonObject.getString("imagen_url") // Obtener la URL de la imagen
                        );
                        reporteList.add(reporte);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                progressBar.setVisibility(View.GONE); // Ocultar el ProgressBar

                if (reporteList.isEmpty()) {
                    // Mostrar el mensaje de "No hay reportes" si la lista está vacía
                    tvNoReportes.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE); // Asegurar que el RecyclerView esté oculto
                } else {
                    // Mostrar el RecyclerView si hay reportes
                    tvNoReportes.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    reporteAdapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ListarReportesUI", "Error: " + error.toString());
                Toast.makeText(ListarReportesUI.this, "Error al cargar reportes", Toast.LENGTH_SHORT).show();

                // Mostrar el mensaje de error en lugar de los reportes
                tvNoReportes.setText("Error al cargar reportes");
                tvNoReportes.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        queue.add(jsonArrayRequest);
    }

    // Clase interna Reporte
    public class Reporte {
        private String idReporte, idUsuario, fecha, descripcion, imagenUrl;

        public Reporte(String idReporte, String idUsuario, String fecha, String descripcion, String imagenUrl) {
            this.idReporte = idReporte;
            this.idUsuario = idUsuario;
            this.fecha = fecha;
            this.descripcion = descripcion;
            this.imagenUrl = imagenUrl;
        }

        public String getIdReporte() { return idReporte; }
        public String getIdUsuario() { return idUsuario; }
        public String getFecha() { return fecha; }
        public String getDescripcion() { return descripcion; }
        public String getImagenUrl() { return imagenUrl; }
    }



    // Clase interna ReporteAdapter
    public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder> {
        private List<Reporte> reporteList;

        public ReporteAdapter(List<Reporte> reporteList) {
            this.reporteList = reporteList;
        }

        public class ReporteViewHolder extends RecyclerView.ViewHolder {
            TextView tvIdReporte, tvIdUsuario, tvFecha, tvDescripcion;
            ImageView imgvReporte;
            Button btnVerReporte;

            public ReporteViewHolder(View itemView) {
                super(itemView);
                tvIdReporte = itemView.findViewById(R.id.tvIdReporte);
                tvIdUsuario = itemView.findViewById(R.id.tvIdUsuario);
                tvFecha = itemView.findViewById(R.id.tvFecha);
                tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
                imgvReporte = itemView.findViewById(R.id.imgvReporte);
                btnVerReporte = itemView.findViewById(R.id.btnVerReporteCompleto);  // Asegúrate de que coincida con el XML
            }
        }

        @Override
        public ReporteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_reporte, parent, false);
            return new ReporteViewHolder(view);
        }


        // Añadir este código al onBindViewHolder dentro de ListarReportesUI
        @Override
        public void onBindViewHolder(ReporteViewHolder holder, int position) {
            Reporte reporte = reporteList.get(position);
            holder.tvIdReporte.setText("ID Reporte: " + reporte.getIdReporte());
            holder.tvIdUsuario.setText("ID Usuario: " + reporte.getIdUsuario());
            holder.tvFecha.setText("Fecha: " + reporte.getFecha());
            holder.tvDescripcion.setText("Descripción: " + reporte.getDescripcion());

            // Usar Picasso para cargar la imagen desde la URL
            Picasso.get().load(reporte.getImagenUrl()).into(holder.imgvReporte);

            // Manejar el clic del botón "Ver Reporte"
            holder.btnVerReporte.setOnClickListener(v -> {
                Intent intent = new Intent(ListarReportesUI.this, GestionarReporteUI.class);
                intent.putExtra("idReporte", reporte.getIdReporte());
                intent.putExtra("idUsuario", reporte.getIdUsuario());
                intent.putExtra("fecha", reporte.getFecha());
                intent.putExtra("descripcion", reporte.getDescripcion());
                intent.putExtra("imagenUrl", reporte.getImagenUrl());
                intent.putExtra("username", username);
                startActivity(intent);  // Inicia la actividad
            });
        }

        @Override
        public int getItemCount() {
            return reporteList.size();
        }

    }
}
