package com.project.laitit;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

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
import com.project.laitit.util.AppPrefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationListService extends IntentService {
    private BeaconManager beaconManager;
    private BeaconRegion region;
    Snackbar snackbar;
    String beconsmsg,lastBeaconsId;
    AppPrefs appPrefs;
    protected static final String name = "detection_is";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public NotificationListService() {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = new BeaconManager(this);
        region = new BeaconRegion("ranged region", null, null, null);
        System.out.println("oncreate in service");

    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        connectToService();

    }

    private void connectToService() {
        SystemRequirementsChecker.checkWithDefaultDialogs(MainActivity.activity);
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

                                    sendMessageToActivity(getApplicationContext(),beaconsId);

                                }
                                System.out.println("beaconsId in listservice--"+beaconsId);
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
    private  void sendMessageToActivity(Context context, String msg) {
        Intent intent = new Intent("BeaconId");
        // You can also include some extra data.
        intent.putExtra("id", msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.stopRanging(region);
        beaconManager.disconnect();
    }
}
