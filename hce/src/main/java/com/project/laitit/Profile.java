package com.project.laitit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.project.laitit.util.AppPrefs;
import com.project.laitit.util.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {
    Context context;
    AppPrefs appPrefs;
    Intent intent;
    EditText editFirstName, editLastName;
    String firstName, lastName;
    private ProgressBar progressBar;
    private ImageButton imgButton;
    String iemail = "";
    private int mStatusCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        appPrefs = new AppPrefs(getApplicationContext());
        editFirstName = (EditText) findViewById(R.id.first_name);
        editLastName = (EditText) findViewById(R.id.last_name);
        imgButton = (ImageButton) findViewById(R.id.edit_profile);
        progressBar = (ProgressBar) findViewById(R.id.edit_process);
        Intent intent = getIntent();
        String ifirst_name = intent.getStringExtra("first_name");
        String ilast_name = intent.getStringExtra("last_name");
        iemail = intent.getStringExtra("email");
        editFirstName.setText(ifirst_name);
        editLastName.setText(ilast_name);

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String first_name = editFirstName.getText().toString();
                String last_name = editLastName.getText().toString();
                int err = 0;
                if(first_name.length() == 0){
                    err = 1;
                    Toast.makeText(getApplicationContext(), "Please enter nombre.", Toast.LENGTH_LONG).show();
                }
                if(last_name.length() == 0){
                    err = 1;
                    Toast.makeText(getApplicationContext(), "Please enter apellidos.", Toast.LENGTH_LONG).show();
                }

                //if all fields are mandatory
                if(err == 0){
                    progressBar.setVisibility(View.VISIBLE);
                    editUsers(first_name, last_name, iemail);
                }
            }
        });
    }

    //first name, last name and email for update
    public void editUsers(final String fname, final String lname, final String uemail) {

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Config.socialLogin,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                           // Log.d("KamalKant", "Testing India");
                            JSONObject json = new JSONObject(response);
                            //Log.d("JSONData", json.toString());
                            String authKey = json.getString("token");//get key from server
                            String firstName = json.getString("first_name");//get key from server
                            String lastName = json.getString("last_name");//get key from server
                            String userEmail = json.getString("email");//get key from server
                           // String profilePic = json.getString("profile_pic");//get key from server
                            if(firstName.length() > 0){

                                appPrefs.setAuthKey(authKey);
                                appPrefs.setFirstName(firstName);
                                appPrefs.setLastName(lastName);
                                appPrefs.setUserEmail(userEmail);
                                //appPrefs.setProfilePic(profilePic);
                                progressBar.setVisibility(View.GONE);
                                Intent itnt = new Intent(Profile.this, MainActivity.class);
                                startActivity(itnt);
                                finish();

                            } else {
                                Toast.makeText(Profile.this, Config.COMMON_ERROR, Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        JSONObject obj = new JSONObject(res);
                        //Log.d("objerror", obj.toString());
                        Toast.makeText(Profile.this, Config.GMAIL_COMMON_ERROR, Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        })
        {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("email", uemail);
                params.put("first_name", fname);
                params.put("last_name", lname);
                params.put("social_type", "gm");
                Log.d("paramsdata", params.toString());
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
