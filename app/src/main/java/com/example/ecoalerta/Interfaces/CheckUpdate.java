package com.example.ecoalerta.Interfaces;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckUpdate {
    private Context context;
    private String currentVersion;

    // Define una etiqueta para los logs
    private static final String TAG = "CheckUpdate";

    public CheckUpdate(Context context) {
        this.context = context;
        this.currentVersion = getAppVersion(); // Obtén la versión automáticamente
    }

    // Método para obtener la versión actual de la app
    public String getAppVersion() {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "1.1"; // Valor por defecto en caso de error
        }
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
                    // Agregar un log para mostrar el resultado completo recibido del servidor
                    Log.d(TAG, "Resultado del servidor: " + result);

                    String[] data = result.split(",");
                    String serverVersion = data[0];
                    String downloadLink = data[1];

                    // Mostrar la versión del servidor y el enlace de descarga en el log
                    Log.d(TAG, "Versión del servidor: " + serverVersion);
                    Log.d(TAG, "Enlace de descarga: " + downloadLink);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Actualización disponible")
                .setMessage("Hay una nueva versión disponible. ¿Deseas actualizar?")
                .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadAndInstallApp(downloadLink);
                    }
                })
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0); // Finaliza la aplicación
                    }
                });

        // Crear el diálogo y hacerlo no cancelable
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Evita que se cierre con el botón de retroceso
        dialog.setCanceledOnTouchOutside(false); // Evita que se cierre al tocar fuera del diálogo
        dialog.show();
    }

    private void downloadAndInstallApp(final String downloadLink) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Actualizando");
        progressDialog.setMessage("Descargando la nueva versión...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false); // Evita que se cierre al tocar fuera del diálogo
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
                    String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/ECOALERTA_UPDATE.apk";
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
                    closeAppAfterInstall(); // Cierra la aplicación después de iniciar la instalación
                } else {
                    Toast.makeText(context, "Error al descargar la actualización", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }


    private void closeAppAfterInstall() {
        // Usamos finishAffinity() para cerrar todas las actividades de la app
        if (context instanceof Activity) {
            ((Activity) context).finishAffinity();
        }

        // Opcionalmente, si deseas asegurarte de que se cierra todo el proceso
        System.exit(0);  // Cierra el proceso completo de la aplicación
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