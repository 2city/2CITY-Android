package com.fragments.activity;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import twitter4j.auth.AccessToken;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.dataparser.DataParser;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.models.DataResponse;
import com.models.Status;
import com.models.User;
import com.twitter.android.Twitt_Sharing;
import com.twitter.android.Twitter_Response;
import com.twocity.MainActivity;
import com.twocity.R;
import com.usersession.UserAccessSession;
import com.usersession.UserSession;
import com.utilities.MGUtilities;

public class LoginActivity extends FragmentActivity implements OnClickListener, Twitter_Response {

	
	private Bundle savedInstanceState;
	private Session.StatusCallback statusCallback;
	private Twitt_Sharing mTwitter;
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		

		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		setContentView(R.layout.fragment_login);
		
		Button btnLogin = (Button) this.findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(this);
		
		Button btnFacebook = (Button) this.findViewById(R.id.btnFacebook);
		btnFacebook.setOnClickListener(this);
		
		Button btnTwitter = (Button) this.findViewById(R.id.btnTwitter);
		btnTwitter.setOnClickListener(this);
		
		this.savedInstanceState = savedInstanceState;
		
		ImageView imgViewMenu = (ImageView) this.findViewById(R.id.imgViewMenu);
		imgViewMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(LoginActivity.this, MainActivity.class/*RegisterActivity.class*/);
				startActivity(i);
				finish();
			}
		});
		
		statusCallback = new SessionStatusCallback();
        mTwitter = new Twitt_Sharing(this, Config.TWITTER_CONSUMER_KEY, Config.TWITTER_CONSUMER_SECRET, true);
	}
	
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
			case R.id.btnLogin:
				login();
				break;
				
			case R.id.btnFacebook:

				Session session = Session.getActiveSession();
		        if (session == null) { // not logged in
		        	loginToFacebook(savedInstanceState);
		        }
		        else if(!session.isOpened() && !session.isClosed()){
		        	loginToFacebook(savedInstanceState);
		        }
		        else {
		        	getUsername(session);
		        }
				
				break;
				
			case R.id.btnTwitter:
				
//				loginToTwitter();
				shareWithTwitter();
				
				break;
		}
	}
	private void shareWithTwitter() {
//		String share=Constants.SHARE_LINK.replace("%s", getPackageName());
		try {
			mTwitter = new Twitt_Sharing(this, Config.TWITTER_CONSUMER_KEY,
					Config.TWITTER_CONSUMER_SECRET, true);
//			if (share != null && share.length() > 0) {
				mTwitter.shareToTwitter("", "");// Share as status
//			} 
//			else {
//				mTwitter.shareToTwitter(share, imageurl);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//	    if (requestCode == ProxySocialActivity.FACEBOOK_REQUEST) {
//	    	
//	        if(resultCode == RESULT_OK){
//	            boolean isLogged = data.getBooleanExtra(ProxySocialActivity.IS_LOGGED, false);
//	        }
//	    }
//	    
//	    if (requestCode == ProxySocialActivity.TWITTER_REQUEST) {
//	    	
//	        if(resultCode == RESULT_OK){
//	            boolean isLogged = data.getBooleanExtra(ProxySocialActivity.IS_LOGGED, false);
//	        }
//	    }
//	}//onActivityResult
	
	
	public void login() {
		
		if(!MGUtilities.hasConnection(LoginActivity.this)) {
		
			MGUtilities.showAlertView(
					LoginActivity.this, 
					R.string.network_error, 
					R.string.no_network_connection);
			return;
		}
		
        MGAsyncTask task = new MGAsyncTask(LoginActivity.this);
        task.setMGAsyncTaskListener(new OnMGAsyncTaskListener() {
			
        	DataResponse response;
        	
			@Override
			public void onAsyncTaskProgressUpdate(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAsyncTaskPreExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAsyncTaskPostExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				updateLogin(response);
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				response = syncData();
			}
		});
        task.execute();
        
	}
	
	public DataResponse syncData() {
		
		EditText txtUsername = (EditText) findViewById(R.id.txtUsername);
		EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", txtUsername.getText().toString()));
		params.add(new BasicNameValuePair("password", txtPassword.getText().toString() ));
        
        DataResponse response = DataParser.getJSONFromUrlWithPostRequest(Config.LOGIN_URL, params);
        
        return response;
	}
	
	public void updateLogin(DataResponse response) {
		if(response!=null){
			Status status = response.getStatus();
			
	        if(response != null && status != null) {
	        	
	        	if(status.getStatus_code() == -1 && response.getUser_info() != null ) {
	        		
	        		User user = response.getUser_info();
	        		
	        		UserAccessSession session = UserAccessSession.getInstance(LoginActivity.this);
	        		UserSession userSession = new UserSession();
	        		userSession.setEmail(user.getEmail());
	        		userSession.setFacebook_id(user.getFacebook_id());
	        		userSession.setFull_name(user.getFull_name());
	        		userSession.setLogin_hash(user.getLogin_hash());
	        		userSession.setPhoto_url(user.getPhoto_url());
	        		try {
						if(user.getTwitter_id()!=null && user.getTwitter_id().length()>0){
							userSession.setThumb_url(mTwitter.getUserProfile());
							userSession.setPhoto_url(mTwitter.getUserProfile1());
						}else{
							userSession.setThumb_url(user.getThumb_url());
						}
					} catch (Exception e) {
						e.printStackTrace();
						userSession.setThumb_url(user.getThumb_url());
					}	        		
	        		userSession.setTwitter_id(user.getTwitter_id());
	        		userSession.setUser_id(user.getUser_id());
	        		userSession.setUsername(user.getUsername());
	        		session.storeUserSession(userSession);
	        		
//	        		finish();
//	        		Intent intent =  new Intent(LoginActivity.this, ProfileActivity.class);
//	        		startActivity(intent);
//	        		finish();
	        		Intent i = new Intent(LoginActivity.this, MainActivity.class/*RegisterActivity.class*/);
					startActivity(i);
					finish();
	        	}
	        	else {
	        		MGUtilities.showAlertView(LoginActivity.this, R.string.network_error, status.getStatus_text());
	        	}
	        	
	        }
		}		
	}
	
	public void syncFacebookUser(final GraphUser user) {
		
		if(!MGUtilities.hasConnection(LoginActivity.this)) {
			
			MGUtilities.showAlertView(
					LoginActivity.this, 
					R.string.network_error, 
					R.string.no_network_connection);
			return;
		}
		
        MGAsyncTask task = new MGAsyncTask(LoginActivity.this);
        task.setMGAsyncTaskListener(new OnMGAsyncTaskListener() {
			
        	DataResponse response;
        	
			@Override
			public void onAsyncTaskProgressUpdate(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAsyncTaskPreExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAsyncTaskPostExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				updateLogin(response);
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("facebook_id", user.getId() ));
				params.add(new BasicNameValuePair("full_name", String.valueOf(user.getName()) ));
				try {
					String email = String.valueOf(user.asMap().get("email"));
					if(email!=null && email.trim().length()>0){
						params.add(new BasicNameValuePair("email", ""+ email));
					}else{
						params.add(new BasicNameValuePair("email", ""));
					}
				} catch (Exception e) {
					e.printStackTrace();
					params.add(new BasicNameValuePair("email", ""));
				}
				
				response = DataParser.getJSONFromUrlWithPostRequest(Config.REGISTER_URL, params);
//				Log.d("System out","response: "+response);
			}
		});
        task.execute();
	}
	
	public void syncTwitterUser(final twitter4j.User user, final AccessToken accessToken) {
		
		if(!MGUtilities.hasConnection(this)) {
			
			MGUtilities.showAlertView(
					LoginActivity.this, 
					R.string.network_error, 
					R.string.no_network_connection);
			return;
		}
		
        MGAsyncTask task = new MGAsyncTask(LoginActivity.this);
        task.setMGAsyncTaskListener(new OnMGAsyncTaskListener() {
			
        	DataResponse response;
        	
			@Override
			public void onAsyncTaskProgressUpdate(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAsyncTaskPreExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAsyncTaskPostExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				updateLogin(response);
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("twitter_id", String.valueOf(accessToken.getUserId()) ));
				String username= accessToken.getScreenName();
				if(username==null || username.length()==0){
					username = user.getScreenName();
					if(username==null || username.length()==0){
						username = user.getName();
					}
				}
				params.add(new BasicNameValuePair("full_name",  ""+username));
				params.add(new BasicNameValuePair("email", "" ));
				
				response = DataParser.getJSONFromUrlWithPostRequest(Config.REGISTER_URL, params);
			}
		});
        task.execute();
	}
	
	
	// FACEBOOK
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
	}
	
	@Override
    public void onStart()  {
        super.onStart();
        
        if(Session.getActiveSession() != null)
        	Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        
        if(Session.getActiveSession() != null)
        	Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
    
    // ###############################################################################################
   	// FACEBOOK INTEGRATION METHODS
   	// ###############################################################################################
    public void loginToFacebook(Bundle savedInstanceState) {
      	Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

      	Session session = Session.getActiveSession();
      	
      	if (session == null) {
      
      		session = new Session(this);
             Session.setActiveSession(session);
      	}
      	
      	if (!session.isOpened() && !session.isClosed()) {
             session.openForRead(new Session.OpenRequest(this)
                 .setPermissions(Arrays.asList("public_profile", "email"))
                 .setCallback(statusCallback));
        } else {
             Session.openActiveSession(this, true, statusCallback);
             updateView();
        }
    }
      
	private void updateView() {
		Session session = Session.getActiveSession();
			if (session.isOpened()) {
//            URL_PREFIX_FRIENDS + session.getAccessToken();
          	getUsername(session);
			} 
      }
      
	private void getUsername(final Session session) {
       	Request request = Request.newMeRequest(session, 
       	        new Request.GraphUserCallback() {
       		
   			@Override
   			public void onCompleted(GraphUser user, Response response) {
   				// If the response is successful
       	        if (session == Session.getActiveSession()) {
       	            if (user != null) {
       	                // Set the id for the ProfilePictureView
       	                // view that in turn displays the profile picture.
       	            	Log.e("FACEBOOK USERNAME**", user.getName());
       	            	Log.e("FACEBOOK ID**", user.getId());
       	            	Log.e("FACEBOOK EMAIL**", ""+user.asMap().get("email"));
       	            	
       	            	syncFacebookUser(user);
       	            	
       	            }
       	        }
       	        
       	        if (response.getError() != null) {
       	            // Handle errors, will do so later.
       	        	Log.e("ERROR", response.getError().getErrorMessage());
       	        }
   			}

       	});
       	request.executeAsync();
	}
	
	private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            updateView();
        }
    }
	
	
//	private void onClickLogin() {
//        Session session = Session.getActiveSession();
//        if (!session.isOpened() && !session.isClosed()) {
//            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
//        } else  {
//            Session.openActiveSession(this, true, Arrays.asList("user_likes", "user_status"), statusCallback);
//        }
//    }
	
	// ###############################################################################################
   	// TWITTER INTEGRATION METHODS
   	// ###############################################################################################
//   	public void loginToTwitter() {
//   		if (mTwitter.hasAccessToken() == true) {
//   			try {
//   				syncTwitterUser(mTwitter.getAccessToken());
//   			} 
//   			catch (Exception e) {
//   				e.printStackTrace();
//   			}
//   		} 
//   		else {
//   			mTwitter.loginToTwitter();
//   		}
//   	}
//   	
//   	TwitterAppListener twitterAppListener = new TwitterAppListener() {
// 		
// 		@Override
// 		public void onError(String value)  {
// 			// TODO Auto-generated method stub
// 			Log.e("TWITTER ERROR**", value);
// 		}
// 		
// 		@Override
// 		public void onComplete(AccessToken accessToken) {
// 			// TODO Auto-generated method stub
// 			syncTwitterUser(mTwitter.getAccessToken());
// 		}
// 	};

	@Override
	public void getTwitterResponse(int code, String response) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void getTwitterUser(twitter4j.User user, AccessToken accessToken) {
		// TODO Auto-generated method stub
		syncTwitterUser(user, accessToken);		
	}



	
	
}
