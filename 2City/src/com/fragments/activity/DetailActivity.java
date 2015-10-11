package com.fragments.activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import twitter4j.User;
import twitter4j.auth.AccessToken;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.config.UIConfig;
import com.dataparser.DataParser;
import com.db.DbHelper;
import com.db.Queries;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.imageview.MGImageView;
import com.models.Favorite;
import com.models.Photo;
import com.models.Rating;
import com.models.ResponseRating;
import com.models.ResponseStore;
import com.models.Store;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.twitter.android.Twitt_Sharing;
import com.twitter.android.Twitter_Response;
import com.twocity.MainActivity;
import com.twocity.R;
import com.usersession.UserAccessSession;
import com.usersession.UserSession;
import com.utilities.MGUtilities;

public class DetailActivity extends FragmentActivity implements OnClickListener, Twitter_Response {

	private DisplayImageOptions options;
	private Store store;
	private ArrayList<Photo> photoList;
	private ResponseStore responseStore;
	private ResponseRating responseRating;
	boolean canRate = true;
	private SupportMapFragment mapFragment;
	private GoogleMap googleMap;
	private Queries q;
	private SQLiteDatabase db;
	private Session.StatusCallback statusCallback;
	private Twitt_Sharing mTwitter;
	private boolean isPending = false;
	private Bundle savedInstanceState;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		setContentView(R.layout.fragment_detail);

		DbHelper dbHelper = new DbHelper(this);
		q = new Queries(db, dbHelper);

		store = (Store) this.getIntent().getSerializableExtra("store");

		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(UIConfig.SLIDER_PLACEHOLDER)
				.showImageForEmptyUri(UIConfig.SLIDER_PLACEHOLDER)
				.showImageOnFail(UIConfig.SLIDER_PLACEHOLDER)
				.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();

		ImageView imgViewMenu = (ImageView) findViewById(R.id.imgViewMenu);
		imgViewMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		updateStore();

		UserAccessSession userAccess = UserAccessSession.getInstance(this);
		UserSession userSession = userAccess.getUserSession();
		if (userSession != null)
			checkUserCanRate();

		this.savedInstanceState = savedInstanceState;
		statusCallback = new SessionStatusCallback();
//		mTwitter = new TwitterApp(this, twitterAppListener);
		mTwitter = new Twitt_Sharing(this, Config.TWITTER_CONSUMER_KEY, Config.TWITTER_CONSUMER_SECRET, false);
	}

	public void showRatingDialog() {

		if (!MGUtilities.hasConnection(this)) {
			MGUtilities.showAlertView(this, R.string.network_error,
					R.string.no_network_connection);
			return;
		}

		UserAccessSession userAccess = UserAccessSession.getInstance(this);
		UserSession userSession = userAccess.getUserSession();

		if (userSession == null) {
			MGUtilities.showAlertView(this, R.string.login_error,
					R.string.login_error_rating);
			return;
		}

		if (!canRate) {
			MGUtilities.showAlertView(this, R.string.rating_error,
					R.string.rating_error_finish);
			return;
		}

		if (responseRating != null && responseRating.getStore_rating() != null) {

			Rating rating = responseRating.getStore_rating();
			if (rating.getCan_rate() == -1) {
				MGUtilities.showAlertView(this, R.string.rating_error,
						R.string.rating_error_finish);
				return;
			}
		} else {
			MGUtilities.showAlertView(this, R.string.rating_error,
					R.string.rating_error_something_wrong);

			return;
		}

		LayoutInflater li = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View v = li.inflate(R.layout.rating_dialog, null);

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(this.getResources().getString(R.string.rate_store));
		alert.setView(v);
		alert.setPositiveButton(this.getResources().getString(R.string.rate),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						rateStore(v);
					}
				});

		alert.setNegativeButton(this.getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});
		alert.create();
		alert.show();
	}

	public void rateStore(final View v) {

		if (!MGUtilities.hasConnection(this)) {
			MGUtilities.showAlertView(this, R.string.network_error,
					R.string.no_network_connection);
			return;
		}

		MGAsyncTask task = new MGAsyncTask(this);
		task.setMGAsyncTaskListener(new OnMGAsyncTaskListener() {

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
				updateStore();
			}

			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub

				syncRating(v);
			}
		});
		task.execute();

	}

	public void syncRating(View v) {
		RatingBar ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);
		int rating = (int) ratingBar.getRating();

		UserAccessSession userAccess = UserAccessSession.getInstance(this);
		UserSession userSession = userAccess.getUserSession();

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("rating", String.valueOf(rating)));
		params.add(new BasicNameValuePair("store_id", String.valueOf(store
				.getStore_id())));
		params.add(new BasicNameValuePair("user_id", String.valueOf(userSession
				.getUser_id())));
		params.add(new BasicNameValuePair("login_hash", userSession
				.getLogin_hash()));

		responseStore = DataParser.getJSONFromUrlStore(Config.POST_RATING_URL,
				params);

		if (responseStore != null && responseStore.getStore() != null) {

			q.updateStore(responseStore.getStore());
			store = responseStore.getStore();
			canRate = false;
		}
	}

	public void checkUserCanRate() {

		if (!MGUtilities.hasConnection(this)) {
			return;
		}

		MGAsyncTask task = new MGAsyncTask(this);
		task.setMGAsyncTaskListener(new OnMGAsyncTaskListener() {

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

			}

			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub

				UserAccessSession userAccess = UserAccessSession
						.getInstance(DetailActivity.this);
				UserSession userSession = userAccess.getUserSession();

				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("store_id", String
						.valueOf(store.getStore_id())));
				params.add(new BasicNameValuePair("user_id", String
						.valueOf(userSession.getUser_id())));
				params.add(new BasicNameValuePair("login_hash", userSession
						.getLogin_hash()));

				responseRating = DataParser.getJSONFromUrlRating(
						Config.GET_RATING_USER_URL, params);
			}
		});
		task.execute();
	}

	private void setMap() {

		mapFragment = new SupportMapFragment();
		FragmentTransaction fragmentTransaction = this
				.getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.googleMapContainer, mapFragment);
		fragmentTransaction.commit();

		Handler h = new Handler();
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				delayMapCall();
			}
		}, 300);
	}

	private void delayMapCall() {

		try {

			googleMap = mapFragment.getMap();
			googleMap.getUiSettings().setAllGesturesEnabled(false);
			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
			googleMap.getUiSettings().setScrollGesturesEnabled(false);
			googleMap.getUiSettings().setCompassEnabled(false);
			googleMap.getUiSettings().setZoomControlsEnabled(false);

			MarkerOptions markerOptions = new MarkerOptions();

			markerOptions
					.title(Html.fromHtml(store.getStore_name()).toString());

			Spanned storeAddress = Html.fromHtml(store.getStore_address());
			String address = storeAddress.toString();

			if (storeAddress.length() > 50)
				address = storeAddress.toString().substring(0, 50) + "...";

			markerOptions.snippet(address);

			markerOptions.position(new LatLng(store.getLat(), store.getLon()));
			markerOptions.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.map_pin_orange));

			Marker mark = googleMap.addMarker(markerOptions);
			mark.setInfoWindowAnchor(0.25f, 0);
			mark.showInfoWindow();

			CameraUpdate zoom = CameraUpdateFactory
					.zoomTo(Config.MAP_ZOOM_LEVEL);
			googleMap.moveCamera(zoom);

			CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(
					store.getLat() + 0.0035, store.getLon()));

			googleMap.moveCamera(center);

			Handler h = new Handler();
			h.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					final ImageView imgViewMap = (ImageView) findViewById(R.id.imgViewMap);
					googleMap.snapshot(new SnapshotReadyCallback() {

						@Override
						public void onSnapshotReady(Bitmap snapshot) {
							// TODO Auto-generated method stub
							imgViewMap.setImageBitmap(snapshot);

							FragmentTransaction fragmentTransaction = DetailActivity.this
									.getSupportFragmentManager()
									.beginTransaction();
							fragmentTransaction.remove(mapFragment);
							fragmentTransaction.commit();
						}
					});

				}
			}, 1500);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateStore() {

		Photo p = q.getPhotoByStoreId(store.getStore_id());
		photoList = q.getPhotosByStoreId(store.getStore_id());

//		ImageView imgViewPhoto = (ImageView) findViewById(R.id.imgViewPhoto);
		MGImageView imgViewPhoto = (MGImageView) findViewById(R.id.imgViewPhoto);
		

		if (p != null){
			imgViewPhoto.setTag(p.getPhoto_url());
			MainActivity.getImageLoader().displayImage(p.getPhoto_url(),
					imgViewPhoto, options);
		}

		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		TextView tvSubtitle = (TextView) findViewById(R.id.tvSubtitle);

		setMap();

		TextView tvDetails = (TextView) findViewById(R.id.tvDetails);

		ImageView imgViewComments = (ImageView) findViewById(R.id.imgViewComments);
		imgViewComments.setOnClickListener(this);

		Button imgViewShareFb = (Button) findViewById(R.id.imgViewShareFb);
		imgViewShareFb.setOnClickListener(this);

		Button imgViewShareTwitter = (Button) findViewById(R.id.imgViewShareTwitter);
		imgViewShareTwitter.setOnClickListener(this);

		// // SETTING VALUES
		// float rating = 0;
		//
		// if(store.getRating_total() > 0 && store.getRating_count() > 0)
		// rating = store.getRating_total() / store.getRating_count();
		//
		// String strRating = String.format("%.2f %s %d %s",
		// rating,
		// this.getResources().getString(R.string.average_based_on),
		// store.getRating_count(),
		// this.getResources().getString(R.string.rating));

		tvTitle.setText(Html.fromHtml(store.getStore_name()));
		tvSubtitle.setText(Html.fromHtml(store.getStore_address()));

		Spanned details = Html.fromHtml(store.getStore_desc());
		details = Html.fromHtml(details.toString());

		tvDetails.setText(store.getStore_desc());

		TextView tvDate = (TextView) findViewById(R.id.tvDate);
		TextView tvPrice = (TextView) findViewById(R.id.tvPrice);
		TextView tvInfo = (TextView) findViewById(R.id.tvInfo);
		
		ImageView imgDate = (ImageView) findViewById(R.id.imgDate);
		ImageView imgPrice = (ImageView) findViewById(R.id.imgPrice);
		ImageView imgInfo = (ImageView) findViewById(R.id.imgInfo);

		final SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd-mm-yyyy");

		tvPrice.setText(store.getEmail());
		tvPrice.setTag(store.getBuy_ticket());
		tvInfo.setText(store.getWebsite());
		tvInfo.setTag(store.getBuy_ticket());
		
		imgPrice.setTag(store.getBuy_ticket());
		imgInfo.setTag(store.getBuy_ticket());
		
		try {
			tvDate.setText(store.getSms_no());
//			tvDate.setText(/* store.getSms_no() */sdf1.format(sdf.parse(store
//					.getDate_for_ordering())));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		imgDate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String eventName="", eventDescription="", eventLocation="";
				long eventBeginTime=0, eventEndTime=0;
				boolean isAllDay=false;
				try {
					Date date = sdf.parse(store.getDate_for_ordering());
//					Calendar calendar = Calendar.getInstance();
//					calendar.set(date.getYear(), date.getMonth(), date.getDate(), 0, 0, 0);
					eventBeginTime = date.getTime() ;//calendar.getTimeInMillis();
//					calendar.set(date.getYear(), date.getMonth(), date.getDate(), 23, 59, 59);
					date.setHours(23);date.setMinutes(59);date.setSeconds(59);
					eventEndTime = date.getTime(); //calendar.getTimeInMillis();
					eventName = store.getStore_name();
					eventDescription = store.getStore_desc();
					eventLocation = store.getPhone_no();
					addCalenderEvent(eventName, eventDescription, eventLocation, eventBeginTime, eventEndTime, isAllDay);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		});
		imgPrice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String buy_ticket = v.getTag().toString();
				if(buy_ticket!=null && buy_ticket.length()>0){
					Intent i = new Intent(DetailActivity.this,
							BuyTicketDetailActivity.class);
					if(!buy_ticket.startsWith("http")){
						buy_ticket="http://"+buy_ticket;
					}
					i.putExtra("buyticketurl", buy_ticket);
					startActivity(i);	
				}else{
					Toast.makeText(DetailActivity.this, "No Tickets Available", Toast.LENGTH_SHORT).show();
				}
			}
		});
		imgInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PackageManager pm = DetailActivity.this.getPackageManager();
				try
				{
				   pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
//				   pm.getPackageInfo(Config.UBER_APP, PackageManager.GET_ACTIVITIES);
				   // Do something awesome - the app is installed! Launch App.
				   Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.ubercab");
				   startActivity(LaunchIntent);
				}
				catch (PackageManager.NameNotFoundException e)
				{
				   // No Uber app! Open Mobile Website.
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(Config.UBER_APP));
					startActivity(i);
				}
				
			}
		});
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnRateIt:
			showRatingDialog();
			break;

		case R.id.imgViewGallery:

			if (photoList != null && photoList.size() > 0) {

				// Bundle b = new Bundle();
				// b.putSerializable("photoList", photoList);
				//
				// ImageViewerFragment fragment = new ImageViewerFragment();
				// fragment.setArguments(b);

				// MainActivity mainActivity = (MainActivity)this;
				// mainActivity.switchContent(fragment, true);

				Intent i = new Intent(this, ImageViewerActivity.class);
				i.putExtra("photoList", photoList);
				startActivity(i);
			} else {
				MGUtilities.showAlertView(this, R.string.action_error,
						R.string.no_image_to_display);
			}

			break;

		case R.id.imgViewComments:

			Intent i = new Intent(this, ReviewActivity.class);
			i.putExtra("store", store);
			startActivity(i);

			// Bundle b = new Bundle();
			// b.putSerializable("store", store);
			//
			// ReviewFragment fragment = new ReviewFragment();
			// fragment.setArguments(b);

			// MainActivity mainActivity = (MainActivity)this;
			// mainActivity.switchContent(fragment, true);

			break;

		case R.id.imgViewCall:
			call();
			break;

		case R.id.imgViewEmail:
			email();
			break;

		case R.id.imgViewRoute:
			route();
			break;

		case R.id.imgViewShareFb:

			if (isPending)
				return;

			if (isLoggedInToFacebook()) {
				isPending = true;
				loginToFacebook(savedInstanceState);
			} else {
				shareFB();
			}

			break;

		case R.id.imgViewShareTwitter:

			if (isPending)
				return;

//			loginToTwitter();
//			shareWithTwitter();
			postToTwitter();

			break;

		case R.id.imgViewSMS:
			sms();
			break;

		case R.id.imgViewWebsite:
			website();
			break;

		case R.id.toggleButtonFave:
			checkFave(v);
			break;
		}
	}

	private void checkFave(View view) {

		Favorite fave = q.getFavoriteByStoreId(store.getStore_id());

		if (fave != null) {
			q.deleteFavorite(store.getStore_id());
			((ToggleButton) view).setChecked(false);
		} else {

			fave = new Favorite();
			fave.setStore_id(store.getStore_id());
			q.insertFavorite(fave);
			((ToggleButton) view).setChecked(true);
		}
	}

	private void call() {

		if (store.getPhone_no() == null || store.getPhone_no().length() == 0) {
			MGUtilities.showAlertView(this, R.string.action_error,
					R.string.cannot_proceed);
			return;
		}

		PackageManager pm = this.getBaseContext().getPackageManager();
		boolean canCall = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);

		if (!canCall) {
			MGUtilities.showAlertView(this, R.string.action_error,
					R.string.cannot_proceed);
			return;
		}

		String phoneNo = store.getPhone_no().replaceAll("[^0-9]", "");

		String uri = "tel:" + phoneNo;
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse(uri));
		this.startActivity(intent);
	}

	private void route() {

		if (store.getLat() == 0 || store.getLon() == 0) {
			MGUtilities.showAlertView(this, R.string.action_error,
					R.string.cannot_proceed);
			return;
		}

		// String geo = String.format("geo:%f,%f?q=%f,%f",
		// store.getLat(),
		// store.getLon(),
		// store.getLat(),
		// store.getLon() );

		// String geo =
		// String.format("http://maps.google.com/maps?f=d&daddr=%s,%s&dirflg=d",
		// store.getLat(),
		// store.getLon()) ;

		String geo = String.format(
				"http://maps.google.com/maps?f=d&daddr=%s&dirflg=d",
				store.getStore_address());

		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse(geo));
		// Uri.parse("geo:55.74274,37.56577?q=55.74274,37.56577 (name)"));
		intent.setComponent(new ComponentName("com.google.android.apps.maps",
				"com.google.android.maps.MapsActivity"));
		this.startActivity(intent);
	}

	private void email() {

		if (store.getEmail() == null || store.getEmail().length() == 0) {
			MGUtilities.showAlertView(this, R.string.action_error,
					R.string.cannot_proceed);
			return;
		}

		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { store.getEmail() });

		emailIntent
				.putExtra(Intent.EXTRA_SUBJECT, MGUtilities
						.getStringFromResource(this, R.string.email_subject));

		emailIntent.putExtra(Intent.EXTRA_TEXT,
				MGUtilities.getStringFromResource(this, R.string.email_body));
		emailIntent.setType("message/rfc822");

		this.startActivity(Intent.createChooser(emailIntent, MGUtilities
				.getStringFromResource(this, R.string.choose_email_client)));
	}

	private void sms() {

		if (store.getSms_no() == null || store.getSms_no().length() == 0) {
			MGUtilities.showAlertView(this, R.string.action_error,
					R.string.handset_not_supported);
			return;
		}

		PackageManager pm = this.getBaseContext().getPackageManager();
		boolean canSMS = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);

		if (!canSMS) {
			MGUtilities.showAlertView(this, R.string.action_error,
					R.string.handset_not_supported);
			return;
		}

		String smsNo = store.getSms_no().replaceAll("[^0-9]", "");

		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setType("vnd.android-dir/mms-sms");
		smsIntent.putExtra("address", smsNo);

		smsIntent.putExtra("sms_body",
				MGUtilities.getStringFromResource(this, R.string.sms_body));

		this.startActivity(smsIntent);
	}

	private void website() {

		if (store.getWebsite() == null || store.getWebsite().length() == 0) {
			MGUtilities.showAlertView(this, R.string.action_error,
					R.string.cannot_proceed);
			return;
		}

		String strUrl = store.getWebsite();
		if (!strUrl.contains("http")) {
			strUrl = "http://" + strUrl;
		}

		Intent webIntent = new Intent(Intent.ACTION_VIEW);
		webIntent.setData(Uri.parse(strUrl));
		this.startActivity(Intent.createChooser(webIntent, MGUtilities
				.getStringFromResource(this, R.string.choose_browser)));
	}
	WebDialog feedDialog;
	private void shareFB() {

		if (isLoggedInToFacebook()) {

			Photo p = photoList != null && photoList.size() > 0 ? photoList
					.get(0) : null;

//			if (FacebookDialog.canPresentShareDialog(this,
//					FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
//				// Publish the post using the Share Dialog
//				FacebookDialog shareDialog = null;
//
//				if (p != null) {
//					shareDialog = new FacebookDialog.ShareDialogBuilder(this)
//
//					/*.setLink(store.getWebsite())*/.setPicture(p.getThumb_url()).setDescription("Que buen plan! -Via 2City app")
//							.build();
//				} else {
//
//					shareDialog = new FacebookDialog.ShareDialogBuilder(this)
//					.setDescription("Que buen plan! -Via 2City app")
//					/*.setLink(store.getWebsite())*/.build();
//					
//				}
//
//				shareDialog.present();
//			} else 
			{
				// Fallback. For example, publish the post using the Feed Dialog
				Bundle params = new Bundle();
//				params.putString("link", store.getWebsite());
				params.putString("description", "Que buen plan! -Via 2City app");
				
				if (p != null){
					params.putString("picture", p.getThumb_url());
					params.putString("name", store.getStore_name()+"");
				}

				feedDialog = (new WebDialog.FeedDialogBuilder(this,
						Session.getActiveSession(), params))
						.setOnCompleteListener(new OnCompleteListener() {

							@Override
							public void onComplete(Bundle values,
									FacebookException error) {
								// TODO Auto-generated method stub

								if (error == null) {
									// When the story is posted, echo the
									// success
									// and the post Id.
									String postId = values
											.getString("post_id");
									if(postId== null ||postId.length()==0){
										postId= values
										.getString("id");	
									}									
									if (postId != null ) {
										// publish successful
										feedDialog.dismiss();
									} else {
										// User clicked the Cancel button
										MGUtilities.showAlertView(
												DetailActivity.this,
												R.string.publish_error,
												R.string.publish_cancelled);
										feedDialog.dismiss();
									}
								} else if (error instanceof FacebookOperationCanceledException) {
									// User clicked the "x" button

									MGUtilities.showAlertView(
											DetailActivity.this,
											R.string.publish_error,
											R.string.publish_cancelled);
									feedDialog.dismiss();
								} else {
									MGUtilities
											.showAlertView(
													DetailActivity.this,
													R.string.network_error,
													R.string.problems_encountered_facebook);
									feedDialog.dismiss();
								}
							}

						}).build();
				feedDialog.show();
			}
		} else {
			loginToFacebook(savedInstanceState);
		}
	}

	private void postToTwitter() {

		isPending = false;

		LayoutInflater inflate = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final View view = inflate.inflate(R.layout.twitter_dialog, null);

		// create dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setView(view);
		builder.setTitle("Twitter Status");
		builder.setCancelable(false);

		final EditText txtStatus = (EditText) view.findViewById(R.id.txtStatus);
		txtStatus.setText("");

		// set dialog button
		builder.setPositiveButton("Tweet!",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						String tweet = txtStatus.getText().toString().trim();

//						InputStream is = getImage();
//
//						if (is == null)
//							mTwitter.updateStatus(tweet);
//						else
//							mTwitter.updateStatusWithLogo(is, tweet);
						
						shareWithTwitter(tweet);

					}
				}).setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		// show dialog
		AlertDialog alert = builder.create();
		alert.show();
	}

	public InputStream getImage() {

		Photo p = q.getPhotoByStoreId(store.getStore_id());

		ImageView imgViewPhoto = (ImageView) findViewById(R.id.imgViewPhoto);

		if (p == null)
			return null;

		BitmapDrawable drawable = (BitmapDrawable) imgViewPhoto.getDrawable();
		Bitmap bitmap = drawable.getBitmap();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0 /* ignored for PNG */, bos);
		byte[] bitmapdata = bos.toByteArray();
		ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

		return bs;
	}

	// FACEBOOK
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (Session.getActiveSession() != null)
			Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();

		if (Session.getActiveSession() != null)
			Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
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
			session.openForRead(new Session.OpenRequest(this).setPermissions(
					Arrays.asList("public_profile", "email")).setCallback(
					statusCallback));
		} else {
			Session.openActiveSession(this, true, statusCallback);
			updateView();
		}
	}

	private void updateView() {
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			// URL_PREFIX_FRIENDS + session.getAccessToken();
			isPending = false;
			shareFB();
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			updateView();
		}
	}

//	// ###############################################################################################
//	// TWITTER INTEGRATION METHODS
//	// ###############################################################################################
//	public void loginToTwitter() {
//		if (mTwitter.hasAccessToken() == true) {
//			try {
//				postToTwitter();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} else {
//
//			isPending = true;
//			mTwitter.loginToTwitter();
//		}
//	}
//
//	TwitterAppListener twitterAppListener = new TwitterAppListener() {
//
//		@Override
//		public void onError(String value) {
//			// TODO Auto-generated method stub
//			Log.e("TWITTER ERROR**", value);
//		}
//
//		@Override
//		public void onComplete(AccessToken accessToken) {
//			// TODO Auto-generated method stub
//			DetailActivity.this.runOnUiThread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					postToTwitter();
//				}
//			});
//		}
//	};

	public boolean isLoggedInToFacebook() {
		Session session = Session.getActiveSession();
		return (session != null && session.isOpened());
	}
	
	private void addCalenderEvent(String eventName, String eventDescription, String eventLocation, long eventBeginTime, long eventEndTime, boolean isAllDay ){
		try {
			if (Build.VERSION.SDK_INT >= 14) {
		        Intent intent = new Intent(Intent.ACTION_INSERT)
		        .setData(Events.CONTENT_URI)
		        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventBeginTime/*beginTime.getTimeInMillis()*/)
		        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eventEndTime/*endTime.getTimeInMillis()*/)
		        .putExtra(Events.TITLE, ""+eventName)
		        .putExtra(Events.ALL_DAY, ""+isAllDay)
		        .putExtra(Events.DESCRIPTION, ""+eventDescription)
		        .putExtra(Events.EVENT_LOCATION, ""+eventLocation);
		        /*.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);*/
		        /*.putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com");*/
		         startActivity(intent);
		}

		    else {
		        Calendar cal = Calendar.getInstance();              
		        Intent intent = new Intent(Intent.ACTION_EDIT);
		        intent.setType("vnd.android.cursor.item/event");
		        intent.putExtra("beginTime", eventBeginTime/*cal.getTimeInMillis()*/);
		        intent.putExtra("allDay", isAllDay);
//		        intent.putExtra("rrule", "FREQ=YEARLY");
		        intent.putExtra("endTime", eventEndTime/*cal.getTimeInMillis()+60*60*1000*/);
		        intent.putExtra("title", ""+eventName);
		        intent.putExtra("description", ""+eventDescription);
		        intent.putExtra("eventLocation", ""+eventLocation);		        
		        startActivity(intent);
		        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This api works for platform 2.1 and above Those who uses less then 2.1 instead of content://com.android.calendar/events use content://calendar/events
	 * I added event.put("eventTimezone", "UTC/GMT +2:00") and eliminated event.put("visibility", 3) and event.put("transparency", 0) and it works well
	 * @param curActivity
	 * @param title
	 * @param addInfo
	 * @param place
	 * @param status
	 * @param startDate
	 * @param needReminder
	 * @param needMailService
	 * @return
	 */
	private long pushAppointmentsToCalender(Activity curActivity, String title, String addInfo, String place, int status, long startDate, boolean needReminder, boolean needMailService) {
	    /***************** Event: note(without alert) *******************/

	    String eventUriString = "content://com.android.calendar/events";
	    ContentValues eventValues = new ContentValues();

	    eventValues.put("calendar_id", 1); // id, We need to choose from
	                                        // our mobile for primary
	                                        // its 1
	    eventValues.put("title", title);
	    eventValues.put("description", addInfo);
	    eventValues.put("eventLocation", place);

	    long endDate = startDate + 1000 * 60 * 60; // For next 1hr

	    eventValues.put("dtstart", startDate);
	    eventValues.put("dtend", endDate);

	    // values.put("allDay", 1); //If it is bithday alarm or such
	    // kind (which should remind me for whole day) 0 for false, 1
	    // for true
	    eventValues.put("eventStatus", status); // This information is
	    // sufficient for most
	    // entries tentative (0),
	    // confirmed (1) or canceled
	    // (2):

	   /*Comment below visibility and transparency  column to avoid java.lang.IllegalArgumentException column visibility is invalid error */

	    /*eventValues.put("visibility", 3); // visibility to default (0),
	                                        // confidential (1), private
	                                        // (2), or public (3):
	    eventValues.put("transparency", 0); // You can control whether
	                                        // an event consumes time
	                                        // opaque (0) or transparent
	                                        // (1).
	      */
	    eventValues.put("hasAlarm", 1); // 0 for false, 1 for true

	    Uri eventUri = curActivity.getApplicationContext().getContentResolver().insert(Uri.parse(eventUriString), eventValues);
	    long eventID = Long.parseLong(eventUri.getLastPathSegment());

	    if (needReminder) {
	        /***************** Event: Reminder(with alert) Adding reminder to event *******************/

	        String reminderUriString = "content://com.android.calendar/reminders";

	        ContentValues reminderValues = new ContentValues();

	        reminderValues.put("event_id", eventID);
	        reminderValues.put("minutes", 5); // Default value of the
	                                            // system. Minutes is a
	                                            // integer
	        reminderValues.put("method", 1); // Alert Methods: Default(0),
	                                            // Alert(1), Email(2),
	                                            // SMS(3)

	        Uri reminderUri = curActivity.getApplicationContext().getContentResolver().insert(Uri.parse(reminderUriString), reminderValues);
	    }

	    /***************** Event: Meeting(without alert) Adding Attendies to the meeting *******************/

	    if (needMailService) {
	        String attendeuesesUriString = "content://com.android.calendar/attendees";

	        /********
	         * To add multiple attendees need to insert ContentValues multiple
	         * times
	         ***********/
	        ContentValues attendeesValues = new ContentValues();

	        attendeesValues.put("event_id", eventID);
	        attendeesValues.put("attendeeName", "xxxxx"); // Attendees name
	        attendeesValues.put("attendeeEmail", "yyyy@gmail.com");// Attendee
	                                                                            // E
	                                                                            // mail
	                                                                            // id
	        attendeesValues.put("attendeeRelationship", 0); // Relationship_Attendee(1),
	                                                        // Relationship_None(0),
	                                                        // Organizer(2),
	                                                        // Performer(3),
	                                                        // Speaker(4)
	        attendeesValues.put("attendeeType", 0); // None(0), Optional(1),
	                                                // Required(2), Resource(3)
	        attendeesValues.put("attendeeStatus", 0); // NOne(0), Accepted(1),
	                                                    // Decline(2),
	                                                    // Invited(3),
	                                                    // Tentative(4)

	        Uri attendeuesesUri = curActivity.getApplicationContext().getContentResolver().insert(Uri.parse(attendeuesesUriString), attendeesValues);
	    }

	    return eventID;

	}
	
	private void addEvent(){
		long calID = 3;
		long startMillis = 0; 
		long endMillis = 0;     
		Calendar beginTime = Calendar.getInstance();
		beginTime.set(2012, 9, 14, 7, 30);
		startMillis = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.set(2012, 9, 14, 8, 45);
		endMillis = endTime.getTimeInMillis();
		

		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		values.put(Events.DTSTART, startMillis);
		values.put(Events.DTEND, endMillis);
		values.put(Events.TITLE, "Jazzercise");
		values.put(Events.DESCRIPTION, "Group workout");
		values.put(Events.CALENDAR_ID, calID);
		values.put(Events.EVENT_TIMEZONE, "America/Los_Angeles");
		Uri uri = cr.insert(Events.CONTENT_URI, values);

		// get the event ID that is the last element in the Uri
		long eventID = Long.parseLong(uri.getLastPathSegment());
		// 
		// ... do something with event ID
		//
		//
	}
	
	private void shareWithTwitter(String tweet) {
		MGImageView imgViewPhoto = (MGImageView) findViewById(R.id.imgViewPhoto);
		String imgurl = imgViewPhoto.getTag().toString();
		if(imgurl!=null && imgurl.length()>0){
//			mTwitter.shareToTwitter(tweet, imgurl);
			try {
				mTwitter = new Twitt_Sharing(this, Config.TWITTER_CONSUMER_KEY,
						Config.TWITTER_CONSUMER_SECRET, false);
				tweet +="\nQue buen plan! -Via 2City app.";
//				if (share != null && share.length() > 0) {
					mTwitter.shareToTwitter(""+tweet, ""+imgurl);// Share as status
//				} 
//				else {
//					mTwitter.shareToTwitter(share, imageurl);
//				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		String share=Constants.SHARE_LINK.replace("%s", getPackageName());
		
	}

	@Override
	public void getTwitterResponse(int code, String response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getTwitterUser(User user, AccessToken accessToken) {
		// TODO Auto-generated method stub
		
	}
	
}
