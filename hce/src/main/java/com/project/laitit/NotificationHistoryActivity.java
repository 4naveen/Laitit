package com.project.laitit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.project.laitit.adapter.NotificationListAdapter;
import com.project.laitit.model.NotificationList;
import com.project.laitit.util.AppPrefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationHistoryActivity extends AppCompatActivity {
    AppPrefs appPrefs;
    Intent intent;
    public String history_url="http://laitit.com/hce/api/notification_history.php?authkey=";
    ProgressDialog progressDialog;
    private ArrayList<NotificationList> notificationListArrayList;
    private NotificationListAdapter notificationListAdapter;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        appPrefs = new AppPrefs(getApplicationContext());
        System.out.println("auth key in history"+appPrefs.getAuthKey());
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("Notification History");
        }
        Drawable d = getResources().getDrawable(R.drawable.notification_bg);
        getSupportActionBar().setBackgroundDrawable(d);
        recyclerView=(RecyclerView)findViewById(R.id.recylerview);
        notificationListArrayList=new ArrayList<>();
      /*  progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading, please wait");
        progressDialog.setTitle("Connecting Server...");
        progressDialog.show();
        progressDialog.setCancelable(false);*/
          // getNotificationHistory();

        new NotificationHistoryTask().execute();

    }
/*
    private void getNotificationHistory() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET,url+appPrefs.getAuthKey(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("list");
                            System.out.println(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                NotificationList  notificationList=new NotificationList();
                                notificationList.setTitle(object.getString("title"));
                                notificationList.setId(object.getString("id"));
                                String day_time=object.getString("created");
                                notificationList.setTime(day_time.substring(11,16));
                                notificationList.setDay(day_time.substring(0,10));
                                notificationList.setThumbnailUrl(object.getString("file_path"));
                                notificationList.setDescription(object.getString("description"));
                                notificationListArrayList.add(notificationList);
                            }
                            notificationListAdapter = new NotificationListAdapter(NotificationHistoryActivity.this,notificationListArrayList);
                            // AppSession.staffArrayListGlobal=staffArrayList;
                            recyclerView.setAdapter(notificationListAdapter);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            // rv.addItemDecoration(new DividerItemDecoration(getActivity(),GridLayoutManager.HORIZONTAL));
                            RecyclerView.LayoutManager lmanager = new LinearLayoutManager(NotificationHistoryActivity.this, LinearLayoutManager.VERTICAL, false);
                            //RecyclerView.LayoutManager lmanager=new GridLayoutManager(getActivity(),3);

                            recyclerView.setLayoutManager(lmanager);
                             progressDialog.dismiss();
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
        RequestQueue requestQueue = Volley.newRequestQueue(NotificationHistoryActivity.this);
        requestQueue.add(stringRequest);
    }
*/


    public class NotificationHistoryTask extends AsyncTask<String, Void, String> {
        String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(NotificationHistoryActivity.this);
            progressDialog.setMessage("Loading, please wait");
            progressDialog.setTitle("Connecting server");
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... params) {
            URL url;
            HttpURLConnection connection;
            try {
                url = new URL(history_url+appPrefs.getAuthKey());
                System.out.println("url to connect---"+history_url+appPrefs.getAuthKey());

                connection = (HttpURLConnection) url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder buffer = new StringBuilder();
                String temp;
                while ((temp = br.readLine()) != null) {
                    buffer.append(temp);
                }
                response = buffer.toString();
            } catch (IOException e) {

                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("list");
                System.out.println(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    NotificationList  notificationList=new NotificationList();
                    notificationList.setTitle(object.getString("title"));
                    notificationList.setId(object.getString("id"));
                    String day_time=object.getString("created");
                    notificationList.setTime(day_time.substring(11,16));
                    notificationList.setDay(day_time.substring(0,10));
                    notificationList.setThumbnailUrl(object.getString("file_path"));
                    notificationList.setDescription(object.getString("description"));
                    notificationListArrayList.add(notificationList);
                }
                notificationListAdapter = new NotificationListAdapter(NotificationHistoryActivity.this,notificationListArrayList);
                // AppSession.staffArrayListGlobal=staffArrayList;
                recyclerView.setAdapter(notificationListAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                // rv.addItemDecoration(new DividerItemDecoration(getActivity(),GridLayoutManager.HORIZONTAL));
                RecyclerView.LayoutManager lmanager = new LinearLayoutManager(NotificationHistoryActivity.this, LinearLayoutManager.VERTICAL, false);
                //RecyclerView.LayoutManager lmanager=new GridLayoutManager(getActivity(),3);

                recyclerView.setLayoutManager(lmanager);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
    }

}
