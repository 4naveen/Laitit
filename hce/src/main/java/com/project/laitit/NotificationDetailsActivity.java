package com.project.laitit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.project.laitit.util.AppPrefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class NotificationDetailsActivity extends AppCompatActivity {
    AppPrefs appPrefs;
    public String url="http://laitit.com/hce/api/beaconsmsgdetails.php?msg_id=";
    String id;
    private static String file_path,fileName,file_uri;
    TextView title,time,description,file_name;
    ImageView download,back;
    ProgressDialog progressDialog,pDialog;
    public static final int progress_bar_type = 0;
    LinearLayout download_layout;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);
        appPrefs = new AppPrefs(getApplicationContext());
       /* getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);*/
        title=(TextView)findViewById(R.id.title);
        time=(TextView)findViewById(R.id.time);
        description=(TextView)findViewById(R.id.description);
        file_name=(TextView)findViewById(R.id.file_name);
        download=(ImageView)findViewById(R.id.download);
        back=(ImageView)findViewById(R.id.back);
        download_layout=(LinearLayout)findViewById(R.id.download_layout);
      /*  Drawable d = getResources().getDrawable(R.drawable.notification_bg);
        getSupportActionBar().setBackgroundDrawable(d);*/
        id=getIntent().getStringExtra("id");
        System.out.println("msgid-"+id);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading, please wait");
        progressDialog.setTitle("Searching Beacon details");
        progressDialog.show();
        getNotificationDetails(id);
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    private void getNotificationDetails(String id) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url+id+"&&authkey="+appPrefs.getAuthKey(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject details=jsonObject.getJSONObject("details");
                            title.setText(details.getString("title"));
                            time.setText(details.getString("time").toUpperCase());
                            description.setText(details.getString("description"));
                            file_path=details.getString("file_path");
                            if (file_path.equalsIgnoreCase(""))
                            {
                                download_layout.setVisibility(View.GONE);
                            }
                            else {
                                fileName=details.getString("file_path").substring(details.getString("file_path").lastIndexOf("/")+1);
                                file_name.setText(fileName);
                            }
                            System.out.println(file_path);
                            System.out.println(fileName);
                               progressDialog.dismiss();
                            download.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new DownloadFileFromURL().execute(file_path);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (String.valueOf(error) != null) {

                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(NotificationDetailsActivity.this);
        requestQueue.add(stringRequest);
    }


    private class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();
                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                // Output stream
                file_uri="/sdcard/"+System.currentTimeMillis()+""+fileName;
                if (new File(file_uri).exists()){
                    new File(file_uri).delete();
                }
                OutputStream output = new FileOutputStream(file_uri);
                System.out.println("internal file url"+file_path);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
           sendNotification("Download Complete");

        }
    }


    private void sendNotification(String messageBody) {
        Intent intent= openFile(new File(file_uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(messageBody)
                .setContentText("Click to Open file")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.logo_eoi);
            notificationBuilder.setColor(getResources().getColor(R.color.textColorBlue));
        } else {
            notificationBuilder.setSmallIcon(R.drawable.logo_eoi_color);
        }
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    private Intent openFile(File url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        System.out.println("url--"+url.getAbsolutePath());
        try {

            Uri uri = Uri.fromFile(url);

            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
                System.out.println("doc");
            } else if (url.toString().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
                System.out.println("pdf");

            } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                System.out.println("ppt");

            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
                System.out.println("xls");

            }
            else if (url.toString().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
                System.out.println("trf");

            }
            else if (url.toString().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
                System.out.println("gif");

            } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
                System.out.println("jpg");

            } else if (url.toString().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
                System.out.println("txt");

            }
            else {
                intent.setDataAndType(uri, "*/*");
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Toast.makeText(context, "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
        return intent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                boolean courseLocationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (courseLocationAccepted) {

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(INTERNET)) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener)
    {
        new AlertDialog.Builder(NotificationDetailsActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
