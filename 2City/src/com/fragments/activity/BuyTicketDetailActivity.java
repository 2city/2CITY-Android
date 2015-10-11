package com.fragments.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.twocity.R;
import com.utilities.MGUtilities;

public class BuyTicketDetailActivity extends FragmentActivity implements OnClickListener {

//	private News news;
	private WebView mWebview;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		

		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		setContentView(R.layout.fragment_buy_ticket_detail);
		
		
//		news = (News)this.getIntent().getSerializableExtra("news");
		mWebview = (WebView) findViewById(R.id.webView);
		
		
		ImageView imgWebBack = (ImageView) findViewById(R.id.imgWebBack);
		imgWebBack.setOnClickListener(this);
		
		ImageView imgWebForward = (ImageView) findViewById(R.id.imgWebForward);
		imgWebForward.setOnClickListener(this);
		
		ImageView imgWebRefresh = (ImageView) findViewById(R.id.imgWebRefresh);
		imgWebRefresh.setOnClickListener(this);
		
		ImageView imgViewMenu = (ImageView) findViewById(R.id.imgViewMenu);
		imgViewMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				loadWebView();
			}
		}, 100);
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		
		case R.id.imgWebBack:
			if(mWebview.canGoBack())
				mWebview.goBack();
			break;
			
		case R.id.imgWebForward:
			if(mWebview.canGoForward())
				mWebview.goForward();
			break;
			
		case R.id.imgWebRefresh:
			loadWebView();
			break;
		}
	}
	
	private void loadWebView() {

		if(!MGUtilities.hasConnection(this)) {
			
			MGUtilities.showAlertView(
					this, 
					R.string.network_error, 
					R.string.no_network_connection);
			return;
		}
		
		
		
		
//		String strUrl = news.getNews_url();
//		if(!news.getNews_url().contains("http")) {
//			strUrl = "http://" + news.getNews_url();
//		}
		String strUrl = getIntent().getStringExtra("buyticketurl");//Config.BUY_TICKET;
		Log.d("System out","strUrl: "+strUrl);
		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		
		mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(BuyTicketDetailActivity.this, description, Toast.LENGTH_SHORT).show();
            }

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
			}
            
            
        });

        
        mWebview.loadUrl(strUrl);
	}
	

}
