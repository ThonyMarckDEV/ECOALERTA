package com.example.ecoalerta.Interfaces;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckUpdate {
    private Context context;
    private String currentVersion = "1.0"; // Cambia esta variable según la versión actual de la app

    public CheckUpdate(Context context) {
        this.context = context;
    }

    // Método para verificar si hay una actualización
    public void checkForUpdate() {
        new CheckUpdateTask().execute(ApiService.BASE_URL + "checkUpdate.php");
    }

    private class CheckUpdateTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                StringBuilder result = new StringBuilder();
                int data;
                while ((data = in.read()) != -1) {
                    result.append((char) data);
                }
                urlConnection.disconnect();
                return result.toString();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.isEmpty()) {
                try {
                    String[] data = result.split(",");
                    String serverVersion = data[0];
                    String downloadLink = data[1];

                    if (!serverVersion.equals(currentVersion)) {
                        showUpdateDialog(downloadLink);
                    } else {
                        Toast.makeText(context, "La app está actualizada", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "Error al verificar la actualización", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showUpdateDialog(final String downloadLink) {
        new AlertDialog.Builder(context)
                .setTitle("Actualización disponible")
                .setMessage("Hay una nueva versión disponible. ¿Deseas actualizar?")
                .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadAndInstallApp(downloadLink);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void downloadAndInstallApp(final String downloadLink) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Actualizando");
        progressDialog.setMessage("Descargando la nueva versión...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.show();

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL(downloadLink);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    int fileLength = connection.getContentLength();

                    InputStream input = new BufferedInputStream(url.openStream());
                    String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/app_update.apk";
                    FileOutputStream output = new FileOutputStream(filePath);

                    byte[] data = new byte[4096];
                    int total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        if (fileLength > 0)
                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();
                    return filePath;
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                progressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(String filePath) {
                progressDialog.dismiss();
                if (filePath != null) {
                    installApp(filePath);
                } else {
                    Toast.makeText(context, "Error al descargar la actualización", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void installApp(String filePath) {
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(intent);
    }

}