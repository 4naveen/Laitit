package com.project.laitit;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Context context;
    AppPrefs appPrefs;
    private static final int REQUEST_ERROR_RECOVER = 43;
    private static final int PERMISSION_REQUEST_CODE = 200;
    ProgressDialog progressDialog;
    Intent intent;
    public String url="http://laitit.com/hce/api/beaconsmsg.php?beaconsmsg=";
    String beconsmsg,auth_key;
    private ArrayList<NotificationList> notificationListArrayList;
    private NotificationListAdapter notificationListAdapter;
    private RecyclerView recyclerView;
    public static MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activity=MainActivity.this;
        appPrefs = new AppPrefs(getApplicationContext());
        auth_key=appPrefs.getAuthKey();
       // startService(new Intent(MainActivity.this,NotificationListService.class));
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{INTERNET, READ_CONTACTS, GET_ACCOUNTS, BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_COARSE_LOCATION, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
        recyclerView=(RecyclerView)findViewById(R.id.recylerview);
        notificationListArrayList=new ArrayList<>();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("BeaconId"));
       // System.out.println("auth_key"+appPrefs.getAuthKey());
        //permission

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Drawable d = getResources().getDrawable(R.drawable.notification_bg);
        getSupportActionBar().setBackgroundDrawable(d);
        TextView titleView = new TextView(MainActivity.this);
        TextView subTitleView = new TextView(MainActivity.this);
        LayoutParams layoutparams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LinearLayout lv = new LinearLayout(MainActivity.this);
        lv.setOrientation(LinearLayout.VERTICAL);
        lv.setPadding(2, 2, 2, 2);

        //Set TExt
        titleView.setLayoutParams(layoutparams);
        titleView.setText("Notificaciones");
        titleView.setTextColor(Color.WHITE);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTextSize(30);
        titleView.setPadding(10, 20, 10, 20);
        lv.addView(titleView);

        //Set Persentage
        //subTitleView.setLayoutParams(layoutparams);
        DateFormat dateFormat = new SimpleDateFormat("dd-MMMM");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        subTitleView.setText((dateFormat.format(date)));
        subTitleView.setTextColor(Color.WHITE);
        subTitleView.setGravity(Gravity.CENTER);
        subTitleView.setTextSize(20);
        subTitleView.setPadding(10, 20, 10, 20);
        lv.addView(subTitleView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading, please wait");
        progressDialog.setTitle("Searching nearby beacons...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(lv);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_nofications) {
            //call notification
            intent = new Intent(getApplicationContext(), NotificationHistoryActivity.class);
            startActivity(intent);
           // fragment = new NotificationFragment();

        } /* else if (id == R.id.nav_ponentes) {
            //call ponentes pages
            intent = new Intent(getApplicationContext(), Ponentes.class);
            startActivity(intent);
        }else if (id == R.id.nav_location) {
            // location  pages
            intent = new Intent(getApplicationContext(), Location.class);
            startActivity(intent);
        }*/ else if (id == R.id.nav_info) {
            //call info pages
            intent = new Intent(getApplicationContext(), InfoActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean internetAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean contactsAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean accountAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean bluetoothAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean bluetoothadminAccepted = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean courseLocationAccepted = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                    boolean sdCardAccepted = grantResults[6] == PackageManager.PERMISSION_GRANTED;
                    if (internetAccepted && contactsAccepted && accountAccepted && bluetoothAccepted && bluetoothadminAccepted && courseLocationAccepted&&sdCardAccepted) {
                        //android.widget.Toast.makeText(getBaseContext(), "All Permission Granted.", Toast.LENGTH_LONG).show();
                        //Snackbar.make(view, "Permission Granted, Now you can access location data and camera.", Snackbar.LENGTH_LONG).show();
                    } else {
                        // android.widget.Toast.makeText(getBaseContext(), "Permission Denied.", Toast.LENGTH_LONG).show();
                        //Snackbar.make(view, "Permission Denied, You cannot access location data and camera.", Snackbar.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(INTERNET)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{INTERNET, READ_CONTACTS, GET_ACCOUNTS, BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_COARSE_LOCATION,WRITE_EXTERNAL_STORAGE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // beaconManager.disconnect();
       // unregisterReceiver(mMessageReceiver);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("id");
            System.out.println("message from serevice"+message);
            if (!message.equalsIgnoreCase(""))
            {
                AppPrefs.BEACONS=message;
                beconsmsg=message;
                getNotificationsList();
            }

            // Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    };
    private void getNotificationsList() {
        /*new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {*/
        StringRequest stringRequest = new StringRequest(Request.Method.GET,url+beconsmsg+"&&authkey="+auth_key,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("list");
                            System.out.println("in volley");
                            notificationListArrayList.clear();
                            if (jsonArray.length()!=0) {

                                for (int i = 0; i <jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    NotificationList notificationList = new NotificationList();
                                    notificationList.setTitle(object.getString("title"));
                                    notificationList.setId(object.getString("id"));
                                    String day_time = object.getString("created");

                                    notificationList.setTime(day_time.substring(11, 16));
                                    notificationList.setDay(day_time.substring(0, 10));
                                    notificationList.setThumbnailUrl(object.getString("file_path"));
                                    notificationList.setDescription(object.getString("description"));
                                    notificationListArrayList.add(notificationList);
                                }
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                System.out.println("notification array size " + notificationListArrayList.size());
                                notificationListAdapter = new NotificationListAdapter(MainActivity.this, notificationListArrayList);
                                // AppSession.staffArrayListGlobal=staffArrayList;
                                recyclerView.setAdapter(notificationListAdapter);
                                recyclerView.setItemAnimator(new DefaultItemAnimator());
                                // rv.addItemDecoration(new DividerItemDecoration(getActivity(),GridLayoutManager.HORIZONTAL));
                                RecyclerView.LayoutManager lmanager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
                                //RecyclerView.LayoutManager lmanager=new GridLayoutManager(getActivity(),3);
                                recyclerView.setLayoutManager(lmanager);
                            }
                            else {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
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

      /*  RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);*/
        /*    }
        },10000);*/

        MyVolleySingleton.getInstance(this).getRequestQueue().add(stringRequest);

        // requestQueue.getCache().clear();

    }
}
