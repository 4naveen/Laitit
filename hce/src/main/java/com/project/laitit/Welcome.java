package com.project.laitit;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.project.laitit.util.AppPrefs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Welcome extends AppCompatActivity {

    Context context;
    AppPrefs appPrefs;
    Intent intet;
    BootReceiver bootReceiver;
    public static Welcome activity;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //initialize class
        context = getApplicationContext();
        appPrefs = new AppPrefs(context);
        activity=Welcome.this;

        String alarm = Context.ALARM_SERVICE;
        AlarmManager am = (AlarmManager) getSystemService(alarm);
        Intent intent = new Intent("REFRESH_THIS");
        PendingIntent pi = PendingIntent.getBroadcast(this, 123456789, intent, 0);
        int type = AlarmManager.RTC_WAKEUP;
        long interval = 1000 * 50;
        am.setInexactRepeating(type, System.currentTimeMillis(), interval, pi);
         bootReceiver=new BootReceiver();
          // printKeyHash(Welcome.this);
      /*  if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{INTERNET, READ_CONTACTS, GET_ACCOUNTS, BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_COARSE_LOCATION, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }*/

        Thread background = new Thread() {
            public void run() {
                try {
                    sleep(5 * 500);

                  /* try {
                        PackageInfo info = getPackageManager().getPackageInfo(
                                "com.activity.laitit", PackageManager.GET_SIGNATURES);
                        for (Signature signature : info.signatures) {
                            MessageDigest md = MessageDigest.getInstance("SHA");
                            md.update(signature.toByteArray());
                            Log.d("KeyHash:",Base64.encodeToString(md.digest(), Base64.DEFAULT));
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                    } catch (NoSuchAlgorithmException e) {

                    }*/
                    //send temporary on pos device

                    String first_name = appPrefs.getFirstName();
                    String emil = appPrefs.getUserEmail();
                    if(first_name.length() > 0 && emil.length() > 0){
                        intet = new Intent(Welcome.this, MainActivity.class);
                        startActivity(intet);
                        finish();
                    }else{
                        intet = new Intent(Welcome.this, SocialLogin.class);
                        startActivity(intet);
                        finish();
                    }

                } catch (Exception e){
                    e.getStackTrace();
                }
            }
        };
        // start thread
        background.start();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.v("my_tag", "onResume called");
       // Toast.makeText(this, "onResume called", Toast.LENGTH_SHORT).show();

      /*  LocalBroadcastManager.getInstance(this).registerReceiver(bootReceiver,
                new IntentFilter("com.project.bootbroadcastreceiver.BROADCAST_ACTION"));*/

    }

    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        }
        catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }

}
