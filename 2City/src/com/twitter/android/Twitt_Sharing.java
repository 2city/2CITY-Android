package com.twitter.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.twitter.android.Twitter_Handler.TwDialogListener;

public class Twitt_Sharing {

	private final Twitter_Handler mTwitter;
	private final Activity activity;
	private String twitt_msg;
	private File image_path;
	private String Image_url;
	Twitter_Response twitter_Response;
	boolean isJustLogin;
	User user;

	public Twitt_Sharing(Activity act, String consumer_key,
			String consumer_secret, boolean isJustLogin) {
		this.activity = act;
		this.isJustLogin = isJustLogin;
		twitter_Response = (Twitter_Response) activity;
		mTwitter = new Twitter_Handler(activity, consumer_key, consumer_secret);
	}

	public void shareToTwitter(String msg, File Image_url) {
		this.twitt_msg = msg;
		this.image_path = Image_url;
		mTwitter.setListener(mTwLoginDialogListener);
		if (mTwitter.hasAccessToken()) {
			if (isJustLogin) {
				getUserDetail();
			} else {
				// this will post data in asyn background thread
				showTwittDialog();
			}
		} else {
			mTwitter.authorize();
		}

	}

	public void shareToTwitter(String msg, String Image_url) {
		this.twitt_msg = msg;
		this.Image_url = Image_url;
		Log.d("System out","Image_url: "+Image_url);
		mTwitter.setListener(mTwLoginDialogListener);

		if (mTwitter.hasAccessToken()) {
			if (isJustLogin) {
				getUserDetail();
			} else {
				// this will post data in asyn background thread
				showTwittDialog();
			}
		} else {
			mTwitter.authorize();
		}
	}

	private void getUserDetail() {
		
//		ProgressDialog pDialog;
		
		final ProgressDialog pDialog = new ProgressDialog(activity);
		pDialog.setMessage("Loading...");
		pDialog.setCancelable(false);
		pDialog.show();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					user = mTwitter.getTwitter().verifyCredentials();
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							twitter_Response.getTwitterUser(user, mTwitter.getmAccessToken());
//							twitter_Response.getAccessToken(mTwitter.getmAccessToken());
							
							pDialog.hide();
						}
					});
				} catch (TwitterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

	}

	private void showTwittDialog() {
		Log.d("System out","twitt_msg: "+twitt_msg);
		new PostTwittTask().execute(twitt_msg);

	}

	private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {

		@Override
		public void onError(String value) {
			// showToast("Login Failed");
			twitter_Response.getTwitterResponse(Twitter_Constants.TWITTER_FAIL,
					"Login Failed");
			mTwitter.resetAccessToken();
		}

		@Override
		public void onComplete(String value) {
			if (isJustLogin) {
				getUserDetail();
			} else {
				showTwittDialog();
			}
		}
	};

	void showToast(final String msg) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

			}
		});

	}

	class PostTwittTask extends AsyncTask<String, Void, String> {
		ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(activity);
			pDialog.setMessage("Posting Twitt...");
			pDialog.setCancelable(false);
			pDialog.show();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... twitt) {
			try {
				// mTwitter.updateStatus(twitt[0]);
				// File imgFile = new File("/sdcard/bluetooth/Baby.jpg");

				// Share_Pic_Text_Titter(image_path, twitt_msg,
				// mTwitter.twitterObj);
				Share_Pic_Text_Titter(Image_url, twitt_msg, Twitter_Handler.twitterObj);
				return "success";

			} catch (Exception e) {
				if (e.getMessage().toString().contains("duplicate")) {
					return "Posting Failed because of Duplicate message...";
				}
				e.printStackTrace();
				return "Posting Failed!!!";
			}

		}

		@Override
		protected void onPostExecute(String result) {
			pDialog.dismiss();

			if (null != result && result.equals("success")) {
				// showToast("Posted Successfully");
				twitter_Response.getTwitterResponse(
						Twitter_Constants.TWITTER_SUCCESS,
						/*"Posted Successfully"*/"Posted to Twitter");
			} else {
				// showToast(result);
				twitter_Response.getTwitterResponse(
						Twitter_Constants.TWITTER_FAIL, "" + result);
			}

			super.onPostExecute(result);
		}
	}

	public void Share_Pic_Text_Titter(File image_path, String message,
			Twitter twitter) throws Exception {
		try {
			Log.d("System out", "image_path: " + image_path);
			Log.d("System out", "message: " + message);
			StatusUpdate st = new StatusUpdate(message);

			st.setMedia(image_path);
			twitter.updateStatus(st);

			/*
			 * Toast.makeText(activity, "Successfully update on Twitter...!",
			 * Toast.LENGTH_SHORT).show();
			 */
		} catch (TwitterException e) {
			e.printStackTrace();
			Log.d("TAG", "Pic Upload error" + e.getErrorMessage());
			// Toast.makeText(activity,
			// "Ooopss..!!! Failed to update on Twitter.",
			// Toast.LENGTH_SHORT).show();
			throw e;
		}
	}

	public void Share_Pic_Text_Titter(String image_path, String message,
			Twitter twitter) throws Exception {
		try {
			Log.d("System out", "image_path: " + image_path);
			Log.d("System out", "message: " + message);
			if (image_path != null & image_path.length() > 0) {
				message = message.substring(0, Math.min(message.length(), 115));
				StatusUpdate st = new StatusUpdate(message);
				if(image_path!=null && !image_path.equalsIgnoreCase("")){
					if(!image_path.startsWith("http")){
						File file = new File(image_path);
						FileInputStream fileInputStream = new FileInputStream(file);
						st.setMedia("images" + System.currentTimeMillis(), fileInputStream);
					}else{
						URL u = new URL(image_path);
						InputStream is = u.openConnection().getInputStream();
						st.setMedia("images" + System.currentTimeMillis(), is);
					}
				}
				
				twitter.updateStatus(st);
			} else {
				twitter.updateStatus(message);
			}

			/*
			 * Toast.makeText(activity, "Successfully update on Twitter...!",
			 * Toast.LENGTH_SHORT).show();
			 */
		} catch (TwitterException e) {
			e.printStackTrace();
			Log.d("TAG", "Pic Upload error" + e.getErrorMessage());
			// Toast.makeText(activity,
			// "Ooopss..!!! Failed to update on Twitter.",
			// Toast.LENGTH_SHORT).show();
			throw e;
		}
	}

	public void Authorize_UserDetail() {

	}
	
	public String getUserProfile() {
//		String profileUrl = mSharedPreferences.getString(PROFILE_PICTURE, "");
    	String profileUrl = user.getProfileImageURL();
        
		return profileUrl;
	}
	
	public String getUserProfile1() {
//		String profileUrl = mSharedPreferences.getString(PROFILE_PICTURE1, "");
		String profileUrl = user.getProfileImageURLHttps();
        
		return profileUrl;
	}
}
