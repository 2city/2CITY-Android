package com.fragments.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.dataparser.DataParser;
import com.models.ResponseReview;
import com.models.Review;
import com.models.Store;
import com.twocity.R;
import com.usersession.UserAccessSession;
import com.usersession.UserSession;
import com.utilities.MGUtilities;

public class NewReviewActivity extends FragmentActivity implements OnClickListener {

	
	private Store store;
	private ResponseReview response;
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		

		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		setContentView(R.layout.fragment_review_new);
		
		store = (Store) this.getIntent().getSerializableExtra("store");
		
		
		ImageView imgViewMenu = (ImageView) findViewById(R.id.imgViewMenu);
		imgViewMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);
				finish();
			}
		});
		
		ImageView imgViewPostReview = (ImageView) findViewById(R.id.imgViewPostReview);
		imgViewPostReview.setOnClickListener(this);
		
		final EditText txtReview = (EditText) findViewById(R.id.txtReview);
		final TextView tvMaxCharCount = (TextView) findViewById(R.id.tvMaxCharCount);
		String charsLeft = String.format("%d %s", 
				Config.MAX_CHARS_REVIEWS, 
				MGUtilities.getStringFromResource(NewReviewActivity.this, R.string.chars_left));
		
		tvMaxCharCount.setText(charsLeft);
		
		InputFilter filter = new InputFilter() { 
        
			@Override
            public CharSequence filter(CharSequence source, int start, int end, 
                            Spanned dest, int dstart, int dend) { 
            	
//	            for (int i = start; i < end; i++) { 
//	            	
//	                if (!Character.isLetterOrDigit(source.charAt(i))) { 
//	                        return ""; 
//	                } 
//	            } 
				
				if(source.length() >= Config.MAX_CHARS_REVIEWS)
					return "";
				
				String charsLeft = String.format("%d %s", 
						Config.MAX_CHARS_REVIEWS - txtReview.getText().toString().length(), 
						MGUtilities.getStringFromResource(NewReviewActivity.this, R.string.chars_left));
				
				tvMaxCharCount.setText(charsLeft);
	            
	            return source; 
            }
		};
		
		txtReview.setFilters(new InputFilter[] { filter } ); 
	}
	
	public void postReview() {
		
		if(!MGUtilities.hasConnection(NewReviewActivity.this)) {
			
			MGUtilities.showAlertView(
					NewReviewActivity.this, 
					R.string.network_error, 
					R.string.no_network_connection);
			
			return;
		}
		
        MGAsyncTask task = new MGAsyncTask(NewReviewActivity.this);
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
				reloadDataToReview();
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				syncReview();
			}
		});
        task.execute();
	}
	
	public void syncReview() {
		
		EditText txtReview = (EditText) findViewById(R.id.txtReview);
		
		UserAccessSession userAccess = UserAccessSession.getInstance(NewReviewActivity.this);
		UserSession userSession = userAccess.getUserSession();
		
		try {
			String reviewString = URLEncoder.encode(txtReview.getText().toString(), "UTF-8");
			
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("store_id", String.valueOf(store.getStore_id()) ));
			params.add(new BasicNameValuePair("review", reviewString ));
			params.add(new BasicNameValuePair("user_id", String.valueOf(userSession.getUser_id()) ));
			params.add(new BasicNameValuePair("login_hash", userSession.getLogin_hash() ));

	        response = DataParser.getJSONFromUrlReview(Config.POST_REVIEW_URL, params);
	        
	        if(response != null) {
	        	
	        	if(response.getReturn_count() < response.getTotal_row_count()) {
	        		
	                if(response.getReviews() != null) {
	                	Review review = new Review();
	                	review.setReview_id(-1);
	                	response.getReviews().add(0, review);
	                }
	            }
	        }
	        
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void reloadDataToReview() {
		
		
		Intent returnIntent = new Intent();
//		returnIntent.putExtra("response",response);
//		returnIntent.putExtra("store",store);
		setResult(RESULT_OK, returnIntent);
		finish();
		
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch(v.getId()) {
			case R.id.imgViewPostReview:
				postReview();
				break;
		}
	}

}
