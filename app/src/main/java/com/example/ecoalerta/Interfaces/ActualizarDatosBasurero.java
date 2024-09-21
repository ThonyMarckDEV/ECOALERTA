package com.example.ecoalerta.Interfaces;

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

public class ActualizarDatosBasurero {

    public static void actualizarDatos(PerfilUIBasurero perfilUIBasurero, String username, String nombres, String apellidos, String correo) {
        // Verificar que los campos no estén vacíos
        if (nombres.isEmpty() || apellidos.isEmpty() || correo.isEmpty()) {
            Toast.makeText(perfilUIBasurero, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(perfilUIBasurero);
        String url = "https://modern-blindly-kangaroo.ngrok-free.app/PHP/update_profile.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                Toast.makeText(perfilUIBasurero, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                            } else {
                                String errorMessage = jsonResponse.optString("message", "Error desconocido");
                                Toast.makeText(perfilUIBasurero, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(perfilUIBasurero, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(perfilUIBasurero, "Perfil Actualizado Correctamente!!!", Toast.LENGTH_SHORT).show();
                        perfilUIBasurero.setFieldsEditable(false); // Llamar al método en el objeto correcto
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
