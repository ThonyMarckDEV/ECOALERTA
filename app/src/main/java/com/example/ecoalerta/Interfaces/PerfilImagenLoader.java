package com.example.ecoalerta.Interfaces; // Cambia esto al paquete correspondiente

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ecoalerta.R; // Asegúrate de importar tu recurso de imagen por defecto

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class PerfilImagenLoader {
    private Context context;
    private ImageView imgvLoading;
    private ImageView imgvPerfil;

    public PerfilImagenLoader(Context context, ImageView imgvLoading, ImageView imgvPerfil) {
        this.context = context;
        this.imgvLoading = imgvLoading;
        this.imgvPerfil = imgvPerfil;
    }

    public void cargarImagen(String username) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(ApiService.BASE_URL + "get_profile_picture.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);

                // Enviar el nombre de usuario al servidor
                String postData = "username=" + URLEncoder.encode(username, "UTF-8");
                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                inputStream = (responseCode == HttpURLConnection.HTTP_OK) ? connection.getInputStream() : connection.getErrorStream();

                Scanner in = new Scanner(inputStream);
                StringBuilder response = new StringBuilder();
                while (in.hasNextLine()) {
                    response.append(in.nextLine());
                }
                in.close();

                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.getString("status").equals("success")) {
                        String perfilBase64 = jsonResponse.getString("perfil");

                        // Verificar si el servidor devuelve una imagen vacía
                        if (perfilBase64 != null && !perfilBase64.isEmpty()) {
                            // Decodificar Base64 a byte array
                            byte[] decodedString = Base64.decode(perfilBase64.split(",")[1], Base64.DEFAULT);

                            // Convertir byte array a Bitmap
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            ((AppCompatActivity) context).runOnUiThread(() -> {
                                if (!((AppCompatActivity) context).isFinishing() && !((AppCompatActivity) context).isDestroyed()) {
                                    // Ocultar el GIF de carga
                                    if (imgvLoading != null) {
                                        imgvLoading.setVisibility(View.GONE);
                                    }

                                    // Usar Glide para cargar la imagen en el ImageView con la transformación circular
                                    Glide.with(context)
                                            .load(bitmap)
                                            .apply(RequestOptions.circleCropTransform())
                                            .into(imgvPerfil);
                                }
                            });
                        } else {
                            // Si no hay imagen, cargar la imagen por defecto
                            cargarImagenPorDefecto();
                        }
                    } else {
                        // Si hay un error en la respuesta, cargar la imagen por defecto
                        cargarImagenPorDefecto();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Si hay un error en el formato del JSON, cargar la imagen por defecto
                    cargarImagenPorDefecto();
                }

            } catch (Exception e) {
                e.printStackTrace();
                // Si hay una excepción, cargar la imagen por defecto
                cargarImagenPorDefecto();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void cargarImagenPorDefecto() {
        ((AppCompatActivity) context).runOnUiThread(() -> {
            if (!((AppCompatActivity) context).isFinishing() && !((AppCompatActivity) context).isDestroyed()) {
                // Ocultar el GIF de carga
                if (imgvLoading != null) {
                    imgvLoading.setVisibility(View.GONE);
                }

                // Cargar la imagen por defecto con Glide
                Glide.with(context)
                        .load(R.drawable.default_perfil) // Imagen por defecto en drawable
                        .apply(RequestOptions.circleCropTransform())
                        .into(imgvPerfil);
            }
        });
    }
}
