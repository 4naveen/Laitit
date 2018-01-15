package com.project.laitit.util;

/**
 * Created by KamalKant on 17-06-2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class AppPrefs {


    private static final String USER_PREFS = "USER_PREFS";
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    private String loginToken = "login_token";

    private String loginSessionTime = "login_session_time";
    private String auth_key = "auth_key";
    private String first_name = "first_name";
    private String last_name = "last_name";
    private String profile_pic = "profile_pic";
    private String user_email = "user_email";
    private String imei_no = "imei_no";
    private String ib_gcm_id = "ib_gcm_id";
    private String app_version = "app_version";
    private String last_login = "last_login";

    public static  String BEACONS="";
    public static  String last_modified="";
    public static  String last_item_time="";


    public AppPrefs(Context context){

        this.appSharedPrefs = context.getSharedPreferences(USER_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();

    }

    //Get Auth Key
    public String getAuthKey() {
        return appSharedPrefs.getString(auth_key, "");
    }

    //Set  Auth Key
    public void setAuthKey(String _auth_key) {
        prefsEditor.putString(auth_key, _auth_key).commit();
    }


    // Get First Name
    public String getFirstName() {
        return appSharedPrefs.getString(first_name, "");
    }

    // Set First Name
    public void setFirstName(String _first_name) {
        prefsEditor.putString(first_name, _first_name).commit();
    }

    // Get Last Name
    public String getLastName() {
        return appSharedPrefs.getString(last_name, "");
    }

    // Set Last Name
    public void setLastName(String _last_name) {
        prefsEditor.putString(last_name, _last_name).commit();
    }


    //Get user email
    public String getUserEmail() {
        return appSharedPrefs.getString(user_email, "");
    }

    //Set user email
    public void setUserEmail( String _user_email) {
        prefsEditor.putString(user_email, _user_email).commit();
    }

    //Get Profile Pic
    public String getProfilePic() {
        return appSharedPrefs.getString(profile_pic, "");
    }

    //Set Profile Pic
    public void setProfilePic( String _profile_pic) {
        prefsEditor.putString(profile_pic, _profile_pic).commit();
    }


    //Get IMEI No
    public String getIMEINo() {
        return appSharedPrefs.getString(imei_no, "");
    }

    //Set IMEI No
    public void setIMEINo( String _imeino) {
        prefsEditor.putString(imei_no, _imeino).commit();
    }

    //Get GCM Registration ID
    public String getGcmID() {

        return appSharedPrefs.getString(ib_gcm_id, "");
    }

    //Set GCM Registration ID
    public void setGcmID( String _no_ib_gcm_id) {
        prefsEditor.putString(ib_gcm_id, _no_ib_gcm_id).commit();
    }

    //Get App Version
    public int getAppVersion() {
        return appSharedPrefs.getInt(app_version, 0);
    }

    //Set App Version
    public void setAppVersion(int _app_version) {
        prefsEditor.putInt(app_version, _app_version).commit();
    }
}

