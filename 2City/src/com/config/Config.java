package com.config;

public class Config {
	
	
//	// Change this on your own consumer key
//	public static final String TWITTER_CONSUMER_KEY = "H38nfnHau0QiP3y5nY2zhL8Ie";
//	
//	// Change this on your own consumer secret
//	public static final String TWITTER_CONSUMER_SECRET = "ZKYBB29KIxcXPLIlVnPj2YyP2o2P1HQx28r3ioRcPIbLk2s9wv";
	
	// Change this on your own consumer key
	public static final String TWITTER_CONSUMER_KEY = "cCwpdyKn5uFHnjdd6EWwpuwXO";
	
	// Change this on your own consumer secret
	public static final String TWITTER_CONSUMER_SECRET = "8YKdmO0AhkMp2ZGzVToYWDZxHgSi48JoDNMHPuiGWPYnzzk3CB";
	
	// Set to true if you want to display test ads in emulator
	public static final boolean TEST_ADS_USING_EMULATOR = true;
	
	// Set to true if you want to display test ads on your testing device
	public static final boolean TEST_ADS_USING_TESTING_DEVICE = true;
	
	// Add testing device hash
	// It is displated upon running the app, please check logcat.
	public static final String TESTING_DEVICE_HASH = "3BE2FA86964E0348BBE40ECFE3FAD546";
	
	// Set to true if you want to display ads in all views.
	public static final boolean WILL_SHOW_ADS = false;
	
	// You AdMob Banner Unit ID
	public static final String BANNER_UNIT_ID = "26c4d22648d9483e";
	
	// Change this url depending on the name of your web hosting.
	public static String BASE_URL = "http://2city.merkabahnk.net/";
	
	// Your email that you wish that users on your app will contact you.
	public static String ABOUT_US_EMAIL = "torressilvasergio@gmail.com";
	
	// Adjust this if you want to display reviews at a
	// certain count and shows the View More Comments
	public static int MAX_REVIEW_COUNT_PER_LISTING = 15;
	
	// Edit this if you wish to increase 
	// character count when adding reviews.
	public static int MAX_CHARS_REVIEWS = 255;
	
	// Map zoom level
	public static int MAP_ZOOM_LEVEL = 14;
	
	// Edit this to increase radius in searching store
	public static int MAX_SEARCH_RADIUS = 200;
	
	// Edit this to increase radius to show stores nearby
	public static int MAX_RADIUS_NEARBY_IN_METERS = 20000;
	
	// Debug state, set this always to true to get always an update of data.
	public final static boolean WILL_DOWNLOAD_DATA = true;
	
	// adjust this depending on the offset of you map info window.
	public final static float MAP_INFO_WINDOW_X_OFFSET = 0.25f;
	
	
	// DO NOT EDIT THIS
	public static String DATA_JSON_URL = BASE_URL + "rest/data.php";
	
	// DO NOT EDIT THIS
	public static String CATEGORY_JSON_URL = BASE_URL + "rest/categories.php";
	
	// DO NOT EDIT THIS
	public static String DATA_NEWS_URL = BASE_URL + "rest/data_news.php";
	
	// DO NOT EDIT THIS
	public static String REGISTER_URL = BASE_URL + "rest/register.php";
	
	// DO NOT EDIT THIS
	public static String USER_PHOTO_UPLOAD_URL = BASE_URL + "rest/file_uploader_user_photo.php";
	
	// DO NOT EDIT THIS
	public static String REVIEWS_URL = BASE_URL + "rest/review_load_more.php";
	
	// DO NOT EDIT THIS
	public static String POST_REVIEW_URL = BASE_URL + "rest/post_review.php";
	
	// DO NOT EDIT THIS
	public static String POST_RATING_URL = BASE_URL + "rest/post_rating.php";
	
	// DO NOT EDIT THIS
	public static String GET_RATING_USER_URL = BASE_URL + "rest/get_rating_user.php";
	
	// DO NOT EDIT THIS
	public static String LOGIN_URL = BASE_URL + "rest/login.php";
	
	// DO NOT EDIT THIS
	public static String UPDATE_USER_PROFILE_URL = BASE_URL + "rest/update_user_profile.php";
	
	// DO NOT EDIT THIS
	public static String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?";
	
	// DO NOT EDIT THIS
	public final static int DELAY_SHOW_ANIMATION = 200;
	
	public final static String GetDevicedtlsURL = BASE_URL+ "RestaurantAPI.asmx/addDeviceToken";
	public final static int GetDevicedtlsURLId = 146;
	
	public static String BUY_TICKET = "http://2city.merkabahnk.net/rest/ticket.php";
	public static String UBER_APP="https://play.google.com/store/apps/details?id=com.ubercab&hl=en";
}
