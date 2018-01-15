package com.project.laitit;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.laitit.adapter.NotificationListAdapter;
import com.project.laitit.model.NotificationList;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment{
    Intent intent;
    ProgressDialog progressDialog;
    public String url="http://laitit.com/hce/api/beaconsmsg.php?beaconsmsg=";
    String beconsmsg,auth_key;
    private ArrayList<NotificationList>notificationListArrayList;
    private NotificationListAdapter notificationListAdapter;
    private RecyclerView recyclerView;
    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView=(RecyclerView)view.findViewById(R.id.recylerview);
        notificationListArrayList=new ArrayList<>();
        setHasOptionsMenu(true);
        beconsmsg=getArguments().getString("beconsId");
        auth_key=getArguments().getString("auth_key");
        getNotifications();
        System.out.println("auth_key"+auth_key);
        System.out.println("final_url"+url+beconsmsg);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        getActivity().finish();
                        return true;
                    }
                }
                return false;
            }
        });
        return view;
    }


    private void getNotifications() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET,url+beconsmsg+"&&authkey="+auth_key,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("list");
                            System.out.println("in volley");
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
                            System.out.println("notification array size "+notificationListArrayList.size());
                            notificationListAdapter = new NotificationListAdapter(getActivity(),notificationListArrayList);
                            // AppSession.staffArrayListGlobal=staffArrayList;
                            recyclerView.setAdapter(notificationListAdapter);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            // rv.addItemDecoration(new DividerItemDecoration(getActivity(),GridLayoutManager.HORIZONTAL));
                            RecyclerView.LayoutManager lmanager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                            //RecyclerView.LayoutManager lmanager=new GridLayoutManager(getActivity(),3);

                            recyclerView.setLayoutManager(lmanager);

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

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
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
