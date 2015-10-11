package com.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;

import com.config.Config;
import com.config.Constants;
import com.config.PrefUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gcm.GCMRegistrar;
import com.twocity.R;

public class MGUtilities {
	static SharedPreferences prefs;
	public static boolean hasConnection(Context c) {
		
	    ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

	    NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    if (wifiNetwork != null && wifiNetwork.isConnected()) {
	      return true;
	    }

	    NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if (mobileNetwork != null && mobileNetwork.isConnected()) {
	      return true;
	    }

	    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	    if (activeNetwork != null && activeNetwork.isConnected()) {
	      return true;
	    }

	    return false;
	    
	}
	
	public static View getNoResultView(Context c) {
		
		LayoutInflater inf = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inf.inflate(R.layout.empty_list, null);
		
		return v;
	}
	
	public static void showAlertView(Activity act, int resIdTitle, int resIdMessage) {
		
		AlertDialog.Builder alert = new AlertDialog.Builder(act);
	    alert.setTitle(resIdTitle);
	    alert.setMessage(resIdMessage);
	    alert.setPositiveButton(act.getResources().getString(R.string.ok), 
	    		new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
	    });

	    alert.create();
	    alert.show();
	}
	
	
	public static void showAlertView(Activity act, int resIdTitle, String message) {
		
		AlertDialog.Builder alert = new AlertDialog.Builder(act);
	    alert.setTitle(resIdTitle);
	    alert.setMessage(message);
	    alert.setPositiveButton(act.getResources().getString(R.string.ok), 
	    		new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
	    });

	    alert.create();
	    alert.show();
	}
	
	public static String getStringFromResource(Context c, int resid) {
		
		return c.getResources().getString(resid);
	}
	
	public static void displayMessage(Context context, String message) {
		Intent intent = new Intent(
				"example.pushnotificationdemo.DISPLAY_MESSAGE");
		intent.putExtra("message", message);
		context.sendBroadcast(intent);
	}

	public static void GCMRegisterLocal(Context cntx) {
		try {
			prefs = cntx.getSharedPreferences(Constants.TWOCITY_PREF, 0);
			// Make sure the device has the proper dependencies.
			GCMRegistrar.checkDevice(cntx);
			// Make sure the manifest was properly set - comment out this line
			// while developing the app, then uncomment it when it's ready.
			GCMRegistrar.checkManifest(cntx);
			// registerReceiver(mHandleMessageReceiver, new
			// IntentFilter("example.pushnotificationdemo.DISPLAY_MESSAGE"));
			// Get GCM registration id
			String regId = GCMRegistrar.getRegistrationId(cntx);
			// cm.showAlert(regId);
			// Check if regid already presents
			if (regId.equals("")) {
				// ////////GCMRegistrar.unregister(this);
				// Registration is not present, register now with GCM
				GCMRegistrar.register(cntx,
						Constants.PROJECTKEY_GSM);
				regId = GCMRegistrar.getRegistrationId(cntx);

//				// Note: call API to send GCM data with userid
//				JSONObject objParam = new JSONObject();
//				try {
//					int cid = 0;
//					cid = PrefUtils.getUser(prefs);
//
//					objParam.put("customerId", cid);
//					objParam.put("deviceToken", regId);
//					objParam.put("appName", "Take Eat Easy");
//					String versionName = cntx
//							.getPackageManager().getPackageInfo(
//									cntx.getPackageName(), 0).versionName;
//					objParam.put("appVersion", versionName);
//					objParam.put("deviceName", Build.DEVICE);
//					objParam.put("deviceModel", Build.MODEL);
//					objParam.put("deviceType", "2");
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (NameNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				String GetDevicedtlsURL = Config.GetDevicedtlsURL;
//				callService("POST", GetDevicedtlsURL, objParam.toString());
//				// Note: call API to send GCM data with userid
			} else {
				// //////////GCMRegistrar.unregister(this);
				// Device is already registered on GCM
				if (GCMRegistrar.isRegisteredOnServer(cntx)) {
					// Skips registration.
					// Toast.makeText(getApplicationContext(),
					// "Already registered with GCM", Toast.LENGTH_LONG).show();
				} else {
					PrefUtils.SaveGCMRegisterId(prefs, regId);
					// cm.showAlert(regId);
					
//					// Note: call API to send GCM data with userid
//					JSONObject objParam = new JSONObject();
//					try {
//
//						// SharedPreferences prefs =
//						// getSharedPreferences(constants.RESTAPP_PREF, 0);
//						int cid = 0;
//						cid = PrefUtils.getUser(prefs);
//
//						objParam.put("customerId", cid);
//						objParam.put("deviceToken", regId);
//						objParam.put("appName", "Take Eat Easy");
//						String versionName = cntx
//								.getPackageManager()
//								.getPackageInfo(
//										cntx.getPackageName(), 0).versionName;
//						objParam.put("appVersion", versionName);
//						objParam.put("deviceName", Build.DEVICE);
//						objParam.put("deviceModel", Build.MODEL);
//						objParam.put("deviceType", "2");
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (NameNotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					String GetDevicedtlsURL = Config.GetDevicedtlsURL;
//					// HttpHelper httpHelper = new HttpHelper(this, "Loading..",
//					// null,constants.GetDevicedtlsURLId);
//					// httpHelper.execute("POST",
//					// GetDevicedtlsURL,objParam.toString());
//					callService("POST", GetDevicedtlsURL, objParam.toString());
//					// Note: call API to send GCM data with userid
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void GCMRegisterLocal1(Context cntx) {
		prefs = cntx.getSharedPreferences(Constants.TWOCITY_PREF, 0);
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(cntx);
		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(cntx);
		// registerReceiver(mHandleMessageReceiver, new
		// IntentFilter("example.pushnotificationdemo.DISPLAY_MESSAGE"));
		// Get GCM registration id
		String regId = GCMRegistrar.getRegistrationId(cntx);
		// cm.showAlert(regId);
		// Check if regid already presents
		if (regId.equals("")) {
			// ////////GCMRegistrar.unregister(this);
			// Registration is not present, register now with GCM
			GCMRegistrar.register(cntx, Constants.PROJECTKEY_GSM);
			regId = GCMRegistrar.getRegistrationId(cntx);
		} else {
			// //////////GCMRegistrar.unregister(this);
			// Device is already registered on GCM
			if (GCMRegistrar.isRegisteredOnServer(cntx)) {
				// Skips registration.
				// Toast.makeText(getApplicationContext(),
				// "Already registered with GCM", Toast.LENGTH_LONG).show();
			} else {
				PrefUtils.SaveGCMRegisterId(prefs, regId);
				// cm.showAlert(regId);
			}
		}
	}
	
	private static void callService(String... values) {
		try {
			BufferedReader in = null;
			String URL = values[1].toString();
			JSONObject objParam = new JSONObject(values[2].toString());

			if (android.os.Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}

			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
					"android");

			HttpPost request = new HttpPost();

			// request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
			request.setParams(httpParams);
			// StringEntity se = new StringEntity(objParam.toString());
			// se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
			// "application/json"));
			// request.setEntity(se);
			StringEntity se = new StringEntity(objParam.toString());
			se.setContentEncoding("UTF-8");
			se.setContentType("application/json");
			request.setEntity(se);
			// request.setEntity(new
			// ByteArrayEntity(objParam.toString().getBytes("UTF8")));
			// request.setHeader("json", objParam.toString());
			request.setURI(new URI(URL));
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";

			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			// strResult = sb.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void screenTracking(String fragmentScreenName, Activity activity) {
		EasyTracker easyTracker = EasyTracker.getInstance(activity);
		easyTracker.set(Fields.SCREEN_NAME, /*getClass().getSimpleName()*/fragmentScreenName+"");
		easyTracker.send(MapBuilder.createAppView().build());

	}
}
