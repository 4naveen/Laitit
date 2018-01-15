package com.project.laitit;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.project.laitit.adapter.NotificationListAdapter;
import com.project.laitit.model.NotificationList;
import com.project.laitit.util.AppPrefs;
import com.project.laitit.util.MyVolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NotificationService extends Service {
    private BeaconManager beaconManager;
    private BeaconRegion region;
    Snackbar snackbar;
    public String url="http://laitit.com/hce/api/notificationmsg.php?beaconsmsg=";
    String beconsmsg;
    AppPrefs appPrefs;
    int counter;
    public NotificationService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = new BeaconManager(this);
        region = new BeaconRegion("ranged region", null, null, null);
        System.out.println("oncreate in service");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        connectToService();
        System.out.println("onsatrat in service");
        appPrefs = new AppPrefs(getApplicationContext());
        return START_STICKY;
    }
    private void connectToService() {
        //SystemRequirementsChecker.checkWithDefaultDialogs(Welcome.activity);
        beaconManager.setBackgroundScanPeriod(10000,50000);
        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, final List<Beacon> beacons) {
                  new Handler().post(new Runnable(){
                        @Override
                        public void run() {
                            String beaconsId="";
                            if (!beacons.isEmpty()) {
                                for (int i = 0; i < beacons.size(); i++) {
                                    if (i < beacons.size() - 1) {
                                        beaconsId = beacons.get(i).getUniqueKey() + ",";
                                        System.out.println(beacons.get(i).getMacAddress());
                                    } else {
                                        beaconsId = beaconsId + beacons.get(i).getUniqueKey();
                                        System.out.println(beacons.get(i).getMacAddress());

                                        // System.out.println(beacons.get(i).get);
                                    }
                                         beconsmsg=beaconsId;
                                }
                                if (beconsmsg!=null)
                                {
                                    getNotifications();
                                    sendMessageToActivity(getApplicationContext(),beaconsId);

                                }
                                System.out.println("beaconsId in service--"+beaconsId);
                                AppPrefs.BEACONS=beaconsId;
                                // Toast.makeText(getApplicationContext(),"beacons address-"+beaconsId,Toast.LENGTH_LONG).show();
                                // System.out.println("number of beacons"+beacons.size());
                                System.out.println(beacons.size()+""+beaconsId);
                            }

                        }
                    });
            }
        });
       // SystemRequirementsChecker.checkWithDefaultDialogs();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("")
                .setContentText(messageBody)
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
    private void getNotifications() {

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url + beconsmsg + "&&authkey=" + appPrefs.getAuthKey(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    JSONArray jsonArray = jsonObject.getJSONArray("list");
                                    if (jsonArray.length() != 0) {
                                        counter++;
                                        if (counter == 1) {
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                if (i == jsonArray.length() - 1) {
                                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                                    AppPrefs.last_modified = jsonObject1.getString("time");
                                                    System.out.println("last modified" + AppPrefs.last_modified);
                                                }
                                            }
                                            sendNotification("Tiene " + jsonArray.length() + " mensaje nuevo por abrir");
                                        }
                                        if (counter > 1) {
                                            String current_last_modified = "";
                                            System.out.println("counter" + counter);
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                if (i == jsonArray.length() - 1) {
                                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                                    current_last_modified = jsonObject1.getString("time");
                                                    System.out.println(" current last modified" + current_last_modified);
                                                }

                                            }
                                            if (!current_last_modified.equalsIgnoreCase(AppPrefs.last_modified)) {
                                                counter = 0;
                                                AppPrefs.last_modified = current_last_modified;
                                                sendNotification("Tiene " + jsonArray.length() + " mensaje nuevo por abrir");

                                            }
                                        }
                                    }
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
               /* RequestQueue requestQueue = Volley.newRequestQueue(this);
                requestQueue.add(stringRequest);*/

                MyVolleySingleton.getInstance(this).getRequestQueue().add(stringRequest);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.stopRanging(region);
        beaconManager.disconnect();
    }
    private  void sendMessageToActivity(Context context, String msg) {
        Intent intent = new Intent("BeaconId");
        // You can also include some extra data.
        intent.putExtra("id", msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
