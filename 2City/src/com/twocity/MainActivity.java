package com.twocity;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.auth.AccessToken;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.base.BaseActivity;
import com.config.Config;
import com.config.Constants;
import com.config.PrefUtils;
import com.db.DbHelper;
import com.db.Queries;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.fragments.HomeFragment;
import com.fragments.MenuFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.location.LocationHelper;
import com.location.LocationHelper.OnLocationListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.utilities.MGUtilities;

public class MainActivity extends BaseActivity  implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{

	private Fragment mContent;
	private static SQLiteDatabase db;
	private static DbHelper dbHelper;
	private static Queries q;
	protected static ImageLoader imageLoader;
	private Session.StatusCallback statusCallback;
//	private static TwitterApp mTwitter;
	public static Location location;
	public static List<Address> addresses;
	
	OnSocialAuthenticationListener mCallback;
	OnActivityResultListener mCallbackActivityResult;
	
	
	MenuFragment fragmentMenu;
	
	
	static MainActivity mainActivity;
	
	private AdView adView;

	
	
	private LocationManager locationManager = null;
	Location currentLocation;
	private SharedPreferences prefs;
	int cityId = 0, cuisineId = 0;
	DisplayMetrics metrics = new DisplayMetrics();
	int deviceHeight = 0;
	Point p;
//	customLoaderDialog cm = new customLoaderDialog(this);
	private LocationClient locationClient;
	private LocationRequest loactionRequest;
	
	
	public static MainActivity getInstance() {
		
		return mainActivity;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		// set the Above View	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_frame);
		
		mainActivity = this;
		prefs = getSharedPreferences(Constants.TWOCITY_PREF, 0);
		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		
		if (mContent == null)
			mContent = new HomeFragment();	
		
		dbHelper = new DbHelper(this);
		q = new Queries(db, dbHelper);
		
		imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getBaseContext()));

        statusCallback = new SessionStatusCallback();
//        mTwitter = new TwitterApp(this, twitterAppListener);
        
        getDebugKey();
//        getLocation();
        GetCurrentLocation();
        
//        UserAccessSession session = UserAccessSession
//		.getInstance(MainActivity.this);
//		if (session.getUserSession() == null) {
//			Intent i = new Intent(MainActivity.this, LoginActivity.class/*RegisterActivity.class*/);
//			startActivity(i);
//			finish();
//		}else{
//			switchContent(new SplashFragment(), false);
//		}
        showMainView();
        FrameLayout frameAds = (FrameLayout) findViewById(R.id.frameAds);
		frameAds.setVisibility(View.GONE);
		
	}
	
	
	
	public void showAds() {
		
		FrameLayout frameAds = (FrameLayout) findViewById(R.id.frameAds);
        if(Config.WILL_SHOW_ADS) {
        	
        	frameAds.setVisibility(View.VISIBLE);
        	
        	// Create an ad.
            adView = new AdView(this);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(Config.BANNER_UNIT_ID);
            
            // Add the AdView to the view hierarchy. The view will have no size
            // until the ad is loaded.            
            frameAds.addView(adView);

            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device.
            Builder builder = new AdRequest.Builder();
            
            if(Config.TEST_ADS_USING_EMULATOR)
            	builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
                
            if(Config.TEST_ADS_USING_TESTING_DEVICE)
            	builder.addTestDevice(Config.TESTING_DEVICE_HASH);
                
            AdRequest adRequest = builder.build();
            // Start loading the ad in the background.
            adView.loadAd(adRequest);
        }
        else {
        	frameAds.setVisibility(View.GONE);
        }
	}
	
	
	public void showMainView() {
		// set the Above View
		
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, new HomeFragment())
		.commit();
		
		fragmentMenu = new MenuFragment();
		
		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, fragmentMenu)
		.commit();
		
		// customize the SlidingMenu
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		getSlidingMenu().setSlidingEnabled(true);
		
		
		showAds();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
//		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
		
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
	}
	
	public void switchContent(Fragment fragment, boolean addToBackStack) {
		
		if(mContent == fragment)
			return;
		
		mContent = fragment;
		
		FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
//        transaction.setCustomAnimations(R.anim.trans_slide_in_left, R.anim.trans_slide_out_right);
        
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		
        
//        transaction.setCustomAnimations(R.animator.slide_up,
//                R.animator.slide_down,
//                R.animator.slide_up,
//                R.animator.slide_down);
                
		transaction.replace(R.id.content_frame, fragment);
		
		
		if(addToBackStack)
			transaction.addToBackStack(null);
		
		transaction.commit();
		
		getSlidingMenu().showContent();
	}
	
	public Queries getQueries() {
		
		return q;
	}
	
	public static ImageLoader getImageLoader() {
		return imageLoader;
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
        
        if (locationClient != null)
			locationClient.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(mCallbackActivityResult != null) {
        	mCallbackActivityResult.onActivityResultCallback(this, requestCode, resultCode, data);
        }
        else {
        	Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        }
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
        }
         
         updateView();
     }
     
     private void updateView() {
         Session session = Session.getActiveSession();
         if (session.isOpened()) {
//           URL_PREFIX_FRIENDS + session.getAccessToken();
         	getUsername(session);
         	
         } 
         else {
//         	onClickLogin();
         }
     }
     
//     private void onClickLogin() {
//         Session session = Session.getActiveSession();
//         if (!session.isOpened() && !session.isClosed()) {
//             session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
//         } else  {
//             Session.openActiveSession(this, true, Arrays.asList("user_likes", "user_status"), statusCallback);
//         }
//     }
     
     public void onClickLogout() {
         Session session = Session.getActiveSession();
         if (session != null && !session.isClosed()) {
             session.closeAndClearTokenInformation();
         }

     }

     private class SessionStatusCallback implements Session.StatusCallback {
         
    	 @Override
         public void call(Session session, SessionState state, Exception exception) {
             updateView();
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
     	            	
     	            	if(mCallback != null) {
     	            		mCallback.socialAuthenticationFacebookCompleted(
     	            				MainActivity.this, 
     	            				user, 
     	            				response);
     	            	}
     	            		
     	            	
//     	            	facebookSession.storeAccessToken(session, user.getName());
//     	            	grantApplication();
     	            	
     	            }
     	        }
     	        
     	        if (response.getError() != null) {
     	            // Handle errors, will do so later.
     	        }
 			}

     	});
     	request.executeAsync();
     }
     
     public void getDebugKey() {
 		try {
 	        PackageInfo info = getPackageManager().getPackageInfo(
 	        		getApplicationContext().getPackageName(), 
 	                PackageManager.GET_SIGNATURES);
 	        for (Signature signature : info.signatures) {
 	            MessageDigest md = MessageDigest.getInstance("SHA");
 	            md.update(signature.toByteArray());
 	            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
 	            }
 	        
 	    } catch (NameNotFoundException e) {
 	    	e.printStackTrace();
 	    } catch (NoSuchAlgorithmException e) {
 	    	e.printStackTrace();
 	    }
 	}
     
     
//    // ###############################################################################################
// 	// TWITTER INTEGRATION METHODS
// 	// ###############################################################################################
// 	public void loginToTwitter() {
// 		if (mTwitter.hasAccessToken() == true) {
// 			try {
//// 				grantApplication();
// 			} 
// 			catch (Exception e) {
// 				e.printStackTrace();
// 			}
// 		} 
// 		else {
// 			mTwitter.loginToTwitter();
// 		}
// 	}
// 	
// 	
// 	public TwitterApp getTwitterApp() {
// 		return mTwitter;
// 	}
// 	
// 	TwitterAppListener twitterAppListener = new TwitterAppListener() {
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
//// 			grantApplication();
// 		}
// 	};
 	
 	public boolean isLoggedInToFacebook() {
 	    Session session = Session.getActiveSession();
 	    return (session != null && session.isOpened());
 	}
 	
 	
 	
 	
 	public void getLocation() {
 		
 		LocationHelper helper = new LocationHelper();
		helper.setOnLocationListener(new OnLocationListener() {
			
			@Override
			public void onLocationUpdated(LocationHelper helper, Location loc, int tag) {
				// TODO Auto-generated method stub
				location = loc;
				Log.e("LOCATION FOUND", "LAT = " + loc.getLatitude() +", LON = " + loc.getLongitude());
				
				try {
					addresses = LocationHelper.getAddress(getApplicationContext(), location);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		helper.getLocation(this);
 	}
 	
 	
 	public void clearBackStack() {

	    FragmentManager fm = this.getSupportFragmentManager();
	    for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {    
	        fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); 
	    }
	}
 	
 	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if( this.getSupportFragmentManager().getBackStackEntryCount() != 0 ){
                this.getSupportFragmentManager().popBackStack();
                return true;
            }
            // If there are no fragments on stack perform the original back button event
        }

        return super.onKeyDown(keyCode, event);
    }
	
	public void popBackStack() {
		if( this.getSupportFragmentManager().getBackStackEntryCount() != 0 ){
            this.getSupportFragmentManager().popBackStack();
        }
	}
 	
 	
 	
 	
 	
 	
 	
 	// LISTENERS
 	public interface OnSocialAuthenticationListener {
        public void socialAuthenticationFacebookCompleted(
        		MainActivity mainActivity, GraphUser user, Response response);
    }
	
	public void setOnSocialAuthenticationListener(OnSocialAuthenticationListener listener) {
		try {
            mCallback = (OnSocialAuthenticationListener) listener;
        } catch (ClassCastException e)  {
            throw new ClassCastException(this.toString() + " must implement OnSocialAuthenticationListener");
        }
	}
	
	
	public interface OnActivityResultListener {
        public void onActivityResultCallback(
        		MainActivity mainActivity, int requestCode, int resultCode, Intent data);
    }
	
	public void setOnActivityResultListener(OnActivityResultListener listener) {
		try {
			mCallbackActivityResult = (OnActivityResultListener) listener;
        } catch (ClassCastException e)  {
            throw new ClassCastException(this.toString() + " must implement OnActivityResultListener");
        }
	}
	
	
	public void showSideMenu() {
		
		fragmentMenu.updateMenuList();
		this.showMenu();
		
	}
	@Override
	protected void onResume() {
		super.onResume();
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				MGUtilities.GCMRegisterLocal(MainActivity.this);
			}
		}, 1000);
	}
	
	public void GetCurrentLocation() {
		try {
			// Getting Google Play availability status
			int status = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(this);
			if (status != ConnectionResult.SUCCESS) { // Google Play Services
														// are not available
				int requestCode = 9000;
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status,
						this, requestCode);
				dialog.show();
			} else {
				locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				if (locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
						&& locationManager
								.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					locationClient = new LocationClient(
							getApplicationContext(), this, this);
					locationClient.connect();
				} else {
					// DisplayCity();
					PrefUtils.clearCurrentLocation(prefs);
//					LocationPopup();
					return;
				}
			}
			//
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//	public void LocationPopup() {
//		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//				android.view.ViewGroup.LayoutParams.FILL_PARENT,
//				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
//
//		cm.hide();
//		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View view = inflater.inflate(R.layout.facebookmessagelayout, null);
//		view.setLayoutParams(lp);
//
//		TextView txtMessage = (TextView) view.findViewById(R.id.txtMessageFB);
//		txtMessage.setGravity(Gravity.CENTER_HORIZONTAL);
//		txtMessage.setLayoutParams(lp);
//
//		txtMessage.setText(Html.fromHtml(Constants.disable_location_service)
//				.toString());
//
//		Button btnCancel = (Button) view.findViewById(R.id.btnCancelFB);
//		btnCancel.setText("Ok");
//		Button btnUpdate = (Button) view.findViewById(R.id.btnUpdateFB);
//		btnUpdate.setText("Settings");
//		
//		btnCancel.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		btnUpdate.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		cm.showAlert(view);
//	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
				arg0.getErrorCode(), this, 9000);
		dialog.show();

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Location mCurrentLocation = locationClient.getLastLocation();
		if (mCurrentLocation != null) {
			try {
				JSONObject locdtls = new JSONObject();
				locdtls.put(Constants.TWOCITY_LOCATIONCITY, "");
				locdtls.put(Constants.TWOCITY_LOCATIONLAT,
						String.valueOf(mCurrentLocation.getLatitude()));
				locdtls.put(Constants.TWOCITY_LOCATIONLNG,
						String.valueOf(mCurrentLocation.getLongitude()));
				locdtls.put(Constants.TWOCITY_LOCATIONAREA, "");
				PrefUtils.saveCurrentLocation(prefs, locdtls);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Make current location request
		loactionRequest = LocationRequest.create();
		loactionRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 20 seconds
		loactionRequest.setInterval(1800000);
		locationClient
				.requestLocationUpdates(loactionRequest, locationListener);
	}

	@Override
	public void onDisconnected() {
	}
	LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			getAddress(location);
		}
	};
	void getAddress(Location location) {
//		List<Address> addresses = null;
		try {
			Geocoder gcd = new Geocoder(this, Locale.getDefault());
			if (gcd != null) {
				if (location != null && location.getLatitude() > 0
						&& location.getLongitude() > 0) {
					if (gcd.getFromLocation(location.getLatitude(),
							location.getLongitude(), 100) != null) {
						addresses = gcd.getFromLocation(location.getLatitude(),
								location.getLongitude(), 100);
					}
				}
			}
		} catch (IOException ex) {
			 ex.printStackTrace();
		} finally {
			try {
				// DisplayCity();
				if (addresses != null && addresses.size() > 0) {
					Address address = addresses.get(0);
					JSONObject locdtls = new JSONObject();
					locdtls.put(Constants.TWOCITY_LOCATIONCITY,
							address.getLocality());
					locdtls.put(Constants.TWOCITY_LOCATIONLAT,
							String.valueOf(location.getLatitude()));
					locdtls.put(Constants.TWOCITY_LOCATIONLNG,
							String.valueOf(location.getLongitude()));
					locdtls.put(Constants.TWOCITY_LOCATIONAREA,
							address.getSubLocality());
					PrefUtils.saveCurrentLocation(prefs, locdtls);
				} else {
					JSONObject locdtls = new JSONObject();
					locdtls.put(Constants.TWOCITY_LOCATIONCITY, "");
					locdtls.put(Constants.TWOCITY_LOCATIONLAT,
							String.valueOf(location.getLatitude()));
					locdtls.put(Constants.TWOCITY_LOCATIONLNG,
							String.valueOf(location.getLongitude()));
					locdtls.put(Constants.TWOCITY_LOCATIONAREA, "");
					PrefUtils.saveCurrentLocation(prefs, locdtls);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
//	@Override
//	public void onStop() {
//		if (locationClient != null)
//			locationClient.disconnect();
//		super.onStop();
//	}
	
	
}
