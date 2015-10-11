package com.fragments.activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.config.UIConfig;
import com.dataparser.DataParser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imageview.RoundedImageView;
import com.models.DataResponse;
import com.models.Status;
import com.models.User;
import com.twocity.R;
import com.usersession.UserAccessSession;
import com.usersession.UserSession;
import com.utilities.MGUtilities;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class RegisterActivity extends FragmentActivity implements OnClickListener {

	String fullName = null;
	String email = null;
	String userName = null;
	String password = null;
	
	private int REGISTER_IMAGE_PICKER_SELECT_THUMB = 997;
	private int REGISTER_IMAGE_PICKER_SELECT_COVER = 998;
	private String pathImgThumb = null;
	private String pathImgCover = null;
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		

		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		setContentView(R.layout.fragment_register);
		
		ImageView imgViewCover = (ImageView) this.findViewById(R.id.imgViewCover);
		imgViewCover.setOnClickListener(this);
		
		final RoundedImageView imgViewThumb = (RoundedImageView) findViewById(R.id.imgViewThumb);
		imgViewThumb.getViewTreeObserver().addOnPreDrawListener( 
				
				new ViewTreeObserver.OnPreDrawListener() {
					
					public boolean onPreDraw() {
						
//						int height = imgViewThumb.getMeasuredHeight();
						float width = imgViewThumb.getMeasuredWidth();
			         
						imgViewThumb.setCornerRadius( width / 2 );
						
						return true;
					}
			    });
		
		imgViewThumb.setBorderWidth(UIConfig.BORDER_WIDTH);
		imgViewThumb.setBorderColor(getResources().getColor(UIConfig.THEME_BLACK_COLOR));
		imgViewThumb.setOnClickListener(this);
		
		EditText txtFullName = (EditText) this.findViewById(R.id.txtFullName);
		txtFullName.setOnClickListener(this);
		
		EditText txtEmail = (EditText) this.findViewById(R.id.txtEmail);
		txtEmail.setOnClickListener(this);
		
		EditText txtUsername = (EditText) this.findViewById(R.id.txtUsername);
		txtUsername.setOnClickListener(this);
		
		EditText txtPassword = (EditText) this.findViewById(R.id.txtPassword);
		txtPassword.setOnClickListener(this);
		
		Button btnRegister = (Button) this.findViewById(R.id.btnRegister);
		btnRegister.setOnClickListener(this);
		
		ImageView imgViewMenu = (ImageView) this.findViewById(R.id.imgViewMenu);
		imgViewMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) {
			
			case R.id.imgViewCover:	
				getPicture(REGISTER_IMAGE_PICKER_SELECT_COVER);
				break;
				
			case R.id.imgViewThumb:	
				getPicture(REGISTER_IMAGE_PICKER_SELECT_THUMB);
				break;
				
			case R.id.btnRegister:	
				register();
				break;
				
			default:
				break;
		}
	}
	
	private void getPicture(int selector) {
		Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
         
        startActivityForResult(i, selector);
	}
	
	

	public void registerUser() {
		
		if(!MGUtilities.hasConnection(RegisterActivity.this)) {
			
			MGUtilities.showAlertView(
					RegisterActivity.this, 
					R.string.network_error, 
					R.string.no_network_connection);
			return;
		}
		
        MGAsyncTask task = new MGAsyncTask(this);
        task.setMGAsyncTaskListener(new OnMGAsyncTaskListener() {
			
        	DataResponse response;
        	DataResponse photoResponse;
        	
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
				
				updateRegistration(response, photoResponse);
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("full_name", fullName ) );
				params.add(new BasicNameValuePair("email", email ));
				params.add(new BasicNameValuePair("password", password ));
				params.add(new BasicNameValuePair("username", userName ));
				
				response = DataParser.getJSONFromUrlWithPostRequest(Config.REGISTER_URL, params);
				
				if(response != null) {
					
					User user = response.getUser_info();
					
					if(pathImgThumb != null || pathImgCover != null) {
						photoResponse = uploadPhoto(
								Config.USER_PHOTO_UPLOAD_URL, 
								String.valueOf(user.getUser_id()), 
								user.getLogin_hash());
					}
				}
			}
		});
        task.execute();
	}
	
	public void updateRegistration(DataResponse response, DataResponse photoResponse) {
		Status status = response.getStatus();
		
        if(response != null && status != null) {
        	
        	if(status.getStatus_code() == -1 && response.getUser_info() != null ) {
        		
        		User user = response.getUser_info();
        		
        		UserAccessSession session = UserAccessSession.getInstance(this);
        		UserSession userSession = new UserSession();
        		userSession.setEmail(user.getEmail());
        		userSession.setFacebook_id(user.getFacebook_id());
        		userSession.setFull_name(user.getFull_name());
        		userSession.setLogin_hash(user.getLogin_hash());
        		userSession.setPhoto_url(user.getPhoto_url());
        		userSession.setThumb_url(user.getThumb_url());
        		userSession.setTwitter_id(user.getTwitter_id());
        		userSession.setUser_id(user.getUser_id());
        		userSession.setUsername(user.getUsername());
        		
        		if(photoResponse != null && photoResponse.getPhoto_user_info() != null) {
        			User userPhoto = photoResponse.getPhoto_user_info();
        			userSession.setPhoto_url(userPhoto.getPhoto_url());
            		userSession.setThumb_url(userPhoto.getThumb_url());
        		}
        		
        		session.storeUserSession(userSession);
        		finish();
        	}
        	else {
        		MGUtilities.showAlertView(this, R.string.network_error, status.getStatus_text());
        	}
        	
        }
	}
	
	public void register() {
		
		EditText txtFullName = (EditText) this.findViewById(R.id.txtFullName);
		EditText txtEmail = (EditText) this.findViewById(R.id.txtEmail);
		EditText txtUsername = (EditText) this.findViewById(R.id.txtUsername);
		EditText txtPassword = (EditText) this.findViewById(R.id.txtPassword);
		
		fullName = txtFullName.getText().toString();
		email = txtEmail.getText().toString();
		userName = txtUsername.getText().toString();
		password = txtPassword.getText().toString();
		
		
		if(!MGUtilities.hasConnection(RegisterActivity.this)) {
			
			MGUtilities.showAlertView(
					RegisterActivity.this, 
					R.string.network_error, 
					R.string.no_network_connection);
			return;
		}
		
		if(fullName.length() == 0 || 
				email.length() == 0 || 
				userName.length() == 0 || 
				password.length() == 0) {
			
			MGUtilities.showAlertView(
					RegisterActivity.this, 
					R.string.field_error, 
					R.string.some_fields_are_missing);
			return;
		}
		
		registerUser();
		
	}

	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.e("requestCode", ""+requestCode);
		Log.e("resultCode", ""+resultCode);
		
		if ( requestCode == REGISTER_IMAGE_PICKER_SELECT_THUMB && 
				resultCode == Activity.RESULT_OK ) {
		
			Bitmap bmpThumb = getBitmapFromCameraData(data, this);
			pathImgThumb = getPathFromCameraData(data, this);
			
			ImageView imgViewThumb = (ImageView) this.findViewById(R.id.imgViewThumb);
			imgViewThumb.setImageBitmap(bmpThumb);
		}
		
		if ( requestCode == REGISTER_IMAGE_PICKER_SELECT_COVER && 
				resultCode == Activity.RESULT_OK ) {
			
			Bitmap bmpCover = getBitmapFromCameraData(data, this); 
			pathImgCover = getPathFromCameraData(data, this);
			
			ImageView imgViewCover = (ImageView) this.findViewById(R.id.imgViewCover);
			imgViewCover.setImageBitmap(bmpCover);
		}
    }

	private DataResponse uploadPhoto(String url, String userId, String loginHash) {
		
		try {
			
			HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            
            
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();        

            /* example for setting a HttpMultipartMode */
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            /* example for adding an image part */
            
            builder.addTextBody("user_id", userId);
            builder.addTextBody("login_hash", loginHash);
            
            
            if(pathImgCover != null) {
            	FileBody fileBody = new FileBody( new File(pathImgCover) ); //image should be a String
                builder.addPart("photo_file", fileBody);
            }
            
            if(pathImgThumb != null) {
            	FileBody fileBody = new FileBody( new File(pathImgThumb) ); //image should be a String
                builder.addPart("thumb_file", fileBody);
            }
            
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            
            HttpResponse httpResponse = httpClient.execute(httpPost);
            
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            
            if (statusCode != HttpStatus.SC_OK) { 
            	
                Log.w("Status Code", "Error " + statusCode + " for URL " + url); 
                return null;
             }
            
            HttpEntity getResponseEntity = httpResponse.getEntity();
            
            InputStream source = getResponseEntity.getContent();
            ObjectMapper mapper = new ObjectMapper();
            
            DataResponse data = new DataResponse();
    		
    		try  {

    			data = mapper.readValue(source, DataResponse.class);
    			return data;
    		} 
    		catch (JsonParseException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} 
    		catch (JsonMappingException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} 
    		catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
        }
		catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } 
		catch (ClientProtocolException e) {
            e.printStackTrace();
        } 
		catch (IOException e) {
            e.printStackTrace();
        }

		return null;
	}
	
	
	
	public static Bitmap getBitmapFromCameraData(Intent data, Context context) { 
		
		String picturePath = getPathFromCameraData(data, context); 
		
		return BitmapFactory.decodeFile(picturePath); 
		
	}
	
	
	public static String getPathFromCameraData(Intent data, Context context) { 
		Uri selectedImage = data.getData(); 
		
		String[] filePathColumn = { 
				MediaStore.Images.Media.DATA 
				}; 
		
		Cursor cursor = context.getContentResolver().query(
				selectedImage,filePathColumn, null, null, null); 
		
		cursor.moveToFirst(); 
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]); 
		String picturePath = cursor.getString(columnIndex); 
		cursor.close(); 
		
		return picturePath;
		
	}
	
}
