package com.example.ecoalerta.Interfaces;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActualizarDatosUser {

    public static void actualizarDatos(PerfilUIUser perfilUIUser, String username, String nombres, String apellidos, String correo) {
        // Verificar que los campos no estén vacíos
        if (nombres.isEmpty() || apellidos.isEmpty() || correo.isEmpty()) {
            Toast.makeText(perfilUIUser, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear y mostrar el ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(perfilUIUser);
        progressDialog.setMessage("Actualizando datos...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue requestQueue = Volley.newRequestQueue(perfilUIUser);
        String url = ApiService.BASE_URL + "update_profile.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss(); // Cerrar el ProgressDialog
                        Log.d("Response", response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                Toast.makeText(perfilUIUser, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                            } else {
                                String errorMessage = jsonResponse.optString("message", "Error desconocido");
                                Toast.makeText(perfilUIUser, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(perfilUIUser, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss(); // Cerrar el ProgressDialog
                        Toast.makeText(perfilUIUser, "Perfil Actualizado Correctamente!!!", Toast.LENGTH_SHORT).show();
                        perfilUIUser.setFieldsEditable(false); // Llamar al método en el objeto correcto
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("nombres", nombres);
                params.put("apellidos", apellidos);
                params.put("correo", correo);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}