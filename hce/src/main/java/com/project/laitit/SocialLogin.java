package com.project.laitit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.facebook.FacebookSdk;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.project.laitit.util.AppPrefs;
import com.project.laitit.util.Config;
import com.project.laitit.util.ConnectivityUtils;


public class SocialLogin extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    Context context;
    AppPrefs appPrefs;
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private ProgressBar progressBar;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private int mStatusCode;
    ImageView btn_fb_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getString(com.google.android.gms.R.string.common_signin_button_text_long);//customized gmail login button
        setContentView(R.layout.activity_social_login);
        //initialize class
        context = getApplicationContext();
        appPrefs = new AppPrefs(context);


       // FacebookSdk.sdkInitialize(context);//facebook login
        callbackManager = CallbackManager.Factory.create();
        progressBar = (ProgressBar) findViewById(R.id.loginLoader);


        // Button listeners
        findViewById(R.id.button_gmail_sign_in).setOnClickListener(this);
        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]
        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]

        /*************************************************** Facebook Login ***********************************************/
        //Facebook Intializaiton

        btn_fb_login = (ImageView)findViewById(R.id.facebookView);
        //facebook login button
        try {
            loginButton = (LoginButton) findViewById(R.id.facebookLogin);
            loginButton.setReadPermissions("public_profile", "email");
            //final Context context = this;
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // App code
                    //Log.d("FBLogin", "on success reuslt");
                    //Log.d("LoginSuccess", "onSuccess: " + loginResult.getAccessToken());
                    AccessToken accessToken = loginResult.getAccessToken();

                    GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            // Application code
                            try {

                                JSONObject picture_object = object.getJSONObject("picture");
                                JSONObject data_object = picture_object.getJSONObject("data");
                                String user_pics = data_object.getString("url");
                                String facebook_id = object.getString("id");
                                String first_name = object.getString("first_name");
                                String last_name = object.getString("last_name");
                                String fb_email = object.getString("email");
                                String fb_url = object.getString("email");
                                String gender = object.getString("gender");
                                Log.d("ID: ", facebook_id + " First Name: "+ first_name + " Last Name: "+last_name +" Email: "+ fb_email + " Profile Url: " + user_pics);

                                if (ConnectivityUtils.hasActiveInternetConnection(SocialLogin.this)) {
                                    //call registerUsers Function for saving data on following parameter if prameter is not available then send empty.
                                    //social_id, first_name, last_name, gender, email, profile_pic,  profile_url, social_type
                                    registerUsers(facebook_id, first_name, last_name ,gender, fb_email, user_pics, fb_url ,"fb");//call function for saving data
                                } else {
                                    Toast.makeText(getApplicationContext(), Config.NO_INTERNET, Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception ex) {
                                ex.getStackTrace();
                            }
                        }
                    });

                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,first_name,last_name,link,gender,picture,email");
                    //parameters.putString("fields", "id,name,first_name,last_name,age_range,link," +
                    //      "gender,locale,picture,timezone,updated_time,verified," +
                    //    "email");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                    // App code
                    Log.d("FBLogin", "on cancle method.");
                }

                @Override
                public void onError(FacebookException exception) {
                    // App code
                    Log.d("FBLogin", "show error");
                    //Log.d("Login attempt failed", "onError: " + exception.getMessage());
                    if (exception.getMessage().contains("CONNECTION_FAILURE")) {
                        Toast.makeText(getApplicationContext(), Config.NO_INTERNET, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            //Log.d("GmailLoginA", "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            //showProgressDialog();
            //Log.d("GmailLoginB", "Got cached sign-in");
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    //hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.e("TAG", "onActivityResult");
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        String []names=new String[2];
        //Log.d("ALoginD", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            String google_id = null;
            String display_name = null;
            String user_email = null;
            String profile_url = null;

            if (acct != null) {
                google_id = acct.getId();
                display_name = acct.getDisplayName();
                 names=display_name.split(" ");
                 user_email = acct.getEmail();
                 profile_url = String.valueOf(acct.getPhotoUrl());
            }
            Log.d("ID: ", google_id + " Name: "+ display_name + " Email: "+ user_email + " Profile Url: " + profile_url);
            //call registerUsers Function for saving data on following parameter if prameter is not available then send empty.
            //social_id, first_name, last_name, gender, email, profile_pic,  profile_url, social_type
            registerUsers(google_id,names[0],names[1] ,"", user_email, profile_url, "" ,"gm");//call function for saving data

        } else {
            //Log.d("ALoginD", "data not retrive from gmail account");
            Toast.makeText(SocialLogin.this, Config.GMAIL_COMMON_ERROR, Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        }
    }
    // [END handleSignInResult]



    // [START signIn]
    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        //Log.d("ALoginD", "onConnectionFailed:" + connectionResult);
        Toast.makeText(SocialLogin.this, Config.GMAIL_COMMON_ERROR, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_gmail_sign_in:
                signIn();
                break;

            //case R.id.facebookView:
               // facebookLogin.performClick();
              //  break;
        }
    }

    //call api for saving data

    // registerUsers(google_id, display_name, user_email, profile_url, "gmail_login");//call function for saving data
    //social_id, first_name, last_name, gender, email, profile_pic,  profile_url, social_type
    public void registerUsers(final String social_id, final String first_name, final String last_name, final String gender, final String email, final String profile_pic, final String profile_url, final String social_type) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, Config.socialLogin,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            String authKey = json.getString("token");//get key from server
                            String firstName = json.getString("first_name");//get key from server
                            String lastName = json.getString("last_name");//get key from server
                            String userEmail = json.getString("email");//get key from server
                            String freshUser = json.getString("created");//get key from server
                            System.out.println("email----"+last_name);
                            //String profilePic = json.getString("profile_pic");//get key from server
                            if(firstName.length() > 0){
                                //Toast.makeText(LoginActivity.this, tocken_key, Toast.LENGTH_LONG).show();
                                //set value in session
                                appPrefs.setAuthKey(authKey);
                                appPrefs.setFirstName(firstName);
                                appPrefs.setLastName(lastName);
                                appPrefs.setUserEmail(userEmail);
                                //appPrefs.setProfilePic(profilePic);
                                progressBar.setVisibility(View.GONE);
                                if(freshUser.equals("true")) {
                                    Intent itnt = new Intent(SocialLogin.this, Profile.class);
                                    itnt.putExtra("first_name", firstName);
                                    itnt.putExtra("last_name", lastName);
                                    itnt.putExtra("email", userEmail);
                                    startActivity(itnt);
                                    finish();
                                }else{
                                    Intent itnt = new Intent(SocialLogin.this, MainActivity.class);
                                    startActivity(itnt);
                                    finish();
                                }
                            } else {
                                Toast.makeText(SocialLogin.this, Config.GMAIL_COMMON_ERROR, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(SocialLogin.this, Config.GMAIL_COMMON_ERROR, Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                        // returned data is not JSONObject?
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
                if(social_type.equals("fb")){
                    params.put("fb_id", social_id);
                    params.put("fb_profile_pic", profile_pic);
                    params.put("fb_profile", profile_url);
                }else{
                    params.put("google_id", social_id);
                    params.put("google_profile", profile_url);
                    params.put("google_profile_pic",profile_pic);
                }
                params.put("username", email);
                params.put("email", email);
                params.put("first_name", first_name);
                params.put("last_name", last_name);
                params.put("gender", gender);
                params.put("social_type", social_type);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    //click on facebook link button
    public void fbBtnClick(View v) {
        switch (v.getId()) {
            case R.id.facebookView:
                loginButton.performClick();
                break;
        }
    }
}
