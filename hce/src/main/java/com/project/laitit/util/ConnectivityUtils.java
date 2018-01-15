package com.project.laitit.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;

/**
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> <br/>
 * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
 */

public class ConnectivityUtils {
	
	private static final String LOG_TAG = "ConnectivityUtils";
	
	/**
	 * @param
	 * @return
	 */

	public static boolean emailValidator(String email) 
	{
	    Pattern pattern;
	    Matcher matcher;
	    final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	    pattern = Pattern.compile(EMAIL_PATTERN);
	    matcher = pattern.matcher(email);
	    return matcher.matches();
	}
	
	public static boolean hasActiveInternetConnection(Context context) {
	    if (isNetworkAvailable(context)) {
	    	return true;
//	       
	    } else {
	        //Log.d(LOG_TAG, "No network available!");
	    }
	    return false;
	}
	
	
	public static boolean isNetworkAvailable(Context context) {
	    ConnectivityManager connectivityManager 
	         = (ConnectivityManager)context. getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	}
	
	
	
	/**
	 * @param pContext
	 * @return
	 */
	public static String getMobileNumber(Context pContext) {
		TelephonyManager telephonyMngr = (TelephonyManager) pContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyMngr.getLine1Number();
	}

	/**
	 * @param pContext
	 * @return
	 */
	public static String getImeiNo(Context pContext) {
		TelephonyManager telephonyManager = (TelephonyManager) pContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
	
	public static String getDeviceId(Context pContext) {
		
		String identifier = null;
		TelephonyManager tm = (TelephonyManager)pContext.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm != null)
		      identifier = tm.getDeviceId();
		if (identifier == null || identifier .length() == 0)
		      identifier = Secure.getString(pContext.getContentResolver(),Secure.ANDROID_ID);

		return identifier;
	}


	/**
	 * @ greeting time of the day
	 * 
	 */
	
	public static String greetingTimeOfDay() {
		String timeFlag = null;
		
		Calendar c = Calendar.getInstance();
		int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

		if(timeOfDay >= 0 && timeOfDay < 12){
			
			timeFlag = "Good Morning";
		       
		    
		}else if(timeOfDay >= 12 && timeOfDay < 16){
			
			timeFlag = "Good Afternoon";
		   
		    
		}else if(timeOfDay >= 16 && timeOfDay < 21){
			
			timeFlag = "Good Evening";
			
		   
		    
		}else if(timeOfDay >= 21 && timeOfDay < 24){
			
			timeFlag = "Good Night";
		    
		}
		
		
		
		return timeFlag;
	}
	
	
	public static Spanned getColoredSpanned(String text, int color) {
	    String input = "<font color='" + color + "'>" + text + "</font>";
	    Spanned spannedStrinf = Html.fromHtml(input);
	    return spannedStrinf;
	}
	
	
	//
	
	 public static Integer differenceInMonths(Date beginningDate, Date endingDate) {
	        if (beginningDate == null || endingDate == null) {
	            return 0;
	        }
	        Calendar cal1 = new GregorianCalendar();
	        cal1.setTime(beginningDate);
	        Calendar cal2 = new GregorianCalendar();
	        cal2.setTime(endingDate);
	        return differenceInMonths(cal1, cal2);
	    }

	    private static Integer differenceInMonths(Calendar beginningDate, Calendar endingDate) {
	        if (beginningDate == null || endingDate == null) {
	            return 0;
	        }
	        int m1 = beginningDate.get(Calendar.YEAR) * 12 + beginningDate.get(Calendar.MONTH);
	        int m2 = endingDate.get(Calendar.YEAR) * 12 + endingDate.get(Calendar.MONTH);
	        return m2 - m1;
	    }
	
	    
	    // hash key
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

		        for (android.content.pm.Signature signature : packageInfo.signatures) {
		            MessageDigest md = MessageDigest.getInstance("SHA");
		            md.update(signature.toByteArray());
		            key = new String(Base64.encode(md.digest(), 0));

		            // String key = new String(Base64.encodeBytes(md.digest()));
		            Log.e("Key Hash=", key);
		        }
		    } catch (PackageManager.NameNotFoundException e1) {
		        Log.e("Name not found", e1.toString());
		    } catch (NoSuchAlgorithmException e) {
		        Log.e("No such an algorithm", e.toString());
		    } catch (Exception e) {
		        Log.e("Exception", e.toString());
		    }

		    return key;
		}
	   
}
