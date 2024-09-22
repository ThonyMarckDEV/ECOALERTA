package com.example.ecoalerta.Interfaces;


import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ObtenerProfileTask extends AsyncTask<String, Void, String> {
    private EditText txtNombres;
    private EditText txtApellidos;
    private EditText txtCorreo;

    public ObtenerProfileTask(EditText txtNombres, EditText txtApellidos, EditText txtCorreo) {
        this.txtNombres = txtNombres;
        this.txtApellidos = txtApellidos;
        this.txtCorreo = txtCorreo;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        try {
            String username = params[0];
            URL url = new URL(ApiService.BASE_URL + "perfil.php");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            String postData = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
            writer.write(postData);
            writer.flush();
            writer.close();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            result = response.toString();
        } catch (Exception e) {
            Log.e("ObtenerProfileTask", "Error: " + e.getMessage());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("error")) {
                Toast.makeText(txtNombres.getContext(), jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
            } else {
                txtNombres.setText(jsonObject.getString("nombres"));
                txtApellidos.setText(jsonObject.getString("apellidos"));
                txtCorreo.setText(jsonObject.getString("correo"));
            }
        } catch (JSONException e) {
            Log.e("ObtenerProfileTask", "Error al parsear el JSON: " + e.getMessage());
        }
    }
}
