package com.config;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class PrefUtils {
	public static String GetGCMRegisterId(SharedPreferences prefs) {
		String strOrder="";
		try {
			strOrder = prefs.getString(Constants.TWOCITY_REGISTERID, "");
		} 
		catch (Exception e) {e.printStackTrace();}
		return strOrder;
	}
	public static void SaveGCMRegisterId(SharedPreferences prefs,String regId) {
		Editor editor = prefs.edit();
		editor.putString(Constants.TWOCITY_REGISTERID,regId);
		editor.commit();
	}
	public static void ClearGCMRegisterId(SharedPreferences prefs) {
		Editor editor = prefs.edit();
		editor.putString(Constants.TWOCITY_REGISTERID, "");
		editor.commit();
	}
	public static String GetNotification(SharedPreferences prefs) {
		String strcity="";
		try {
			strcity = prefs.getString(Constants.TWOCITY_NOTIFICATION, "");
		} 
		catch (Exception e) {e.printStackTrace();}
		return strcity;
	}
	public static void SaveNotification(SharedPreferences prefs,String accesstoken) {
		Editor editor = prefs.edit();
		editor.putString(Constants.TWOCITY_NOTIFICATION,accesstoken);
		editor.commit();
	}
	public static void ClearNotification(SharedPreferences prefs) {
		Editor editor = prefs.edit();
		editor.putString(Constants.TWOCITY_NOTIFICATION, "");
		editor.commit();
	}
	
	public static void saveCurrentLocation(SharedPreferences prefs,JSONObject location) {
		Editor editor = prefs.edit();
		try {
			editor.putString(Constants.TWOCITY_LOCATIONCITY,location.getString(Constants.TWOCITY_LOCATIONCITY));
			editor.putString(Constants.TWOCITY_LOCATIONLAT,location.getString(Constants.TWOCITY_LOCATIONLAT));
			editor.putString(Constants.TWOCITY_LOCATIONLNG,location.getString(Constants.TWOCITY_LOCATIONLNG));
			editor.putString(Constants.TWOCITY_LOCATIONAREA,location.getString(Constants.TWOCITY_LOCATIONAREA));
			editor.commit();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void clearCurrentLocation(SharedPreferences prefs) {
		Editor editor = prefs.edit();
		editor.putString(Constants.TWOCITY_LOCATIONCITY, "");
		editor.putString(Constants.TWOCITY_LOCATIONLAT, "");
		editor.putString(Constants.TWOCITY_LOCATIONLNG, "");
		editor.putString(Constants.TWOCITY_LOCATIONAREA, "");
		editor.commit();
	}
	public static JSONObject getCurrentLocation(SharedPreferences prefs) {
		JSONObject location = null;
		try {
			location = new JSONObject();
			location.put(Constants.TWOCITY_LOCATIONCITY,prefs.getString(Constants.TWOCITY_LOCATIONCITY, ""));
			location.put(Constants.TWOCITY_LOCATIONLAT,prefs.getString(Constants.TWOCITY_LOCATIONLAT, ""));
			location.put(Constants.TWOCITY_LOCATIONLNG,prefs.getString(Constants.TWOCITY_LOCATIONLNG, ""));
			location.put(Constants.TWOCITY_LOCATIONAREA,prefs.getString(Constants.TWOCITY_LOCATIONAREA, ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}
}
