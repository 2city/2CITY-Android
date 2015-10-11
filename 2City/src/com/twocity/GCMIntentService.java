package com.twocity;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.config.Constants;
import com.config.PrefUtils;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
 
    private static final String TAG = "GCMIntentService";    
    public GCMIntentService() {
        super(Constants.PROJECTKEY_GSM);
    }
 
    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
    }
    public void onReceive(Context context, Intent intent) {
    	   String regId = intent.getExtras().getString("registration_id");
    	   if(regId != null && !regId.equals("")) {
    		   Log.i("System Out","onReceive _regId="+ regId);
    	   }
	}
    /**
     * Method called on device un registred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
    }
 
    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
//        String message = intent.getExtras().getString("message");
//        SharedPreferences prefs = getSharedPreferences(Constants.TWOCITY_PREF, 0);
//        PrefUtils.SaveNotification(prefs,message);
//        generateNotification(context, message);
    }
   
    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        generateNotification(context, message);
    }
 
    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }
 
    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }
 
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
         
        String title = context.getString(R.string.app_name);
//	     if(globalfunction.isAppOnForeground(context))
//	     {
//	    	 Intent newIntent = new Intent(context, popupactivity.class);
//	         newIntent.putExtra("alarm_message", message);
//	         newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//	         newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//	         context.startActivity(newIntent);
//	         Log.i("isAppOnForeground","isAppOnForeground");
//	         SharedPreferences prefs = context.getSharedPreferences(constants.RESTAPP_PREF, 0);
//	         PrefUtils.ClearNotification(prefs);
//	     }
//	     else{
	         Intent notificationIntent = new Intent(context, MainActivity.class);
	         // set intent so it does not start a new activity
	         notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	         PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
	         notification.setLatestEventInfo(context, title, message, intent);
	         notification.flags |= Notification.FLAG_AUTO_CANCEL;
	          
	         // Play default notification sound
	         notification.defaults |= Notification.DEFAULT_SOUND;	          
	         // Vibrate if vibrate is enabled
	         notification.defaults |= Notification.DEFAULT_VIBRATE;
	         notificationManager.notify(0, notification);
//	     }
    }
}
