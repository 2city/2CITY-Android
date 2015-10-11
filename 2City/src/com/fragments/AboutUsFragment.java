package com.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;

import com.config.Config;
import com.twocity.MainActivity;
import com.twocity.R;
import com.utilities.MGUtilities;

public class AboutUsFragment extends Fragment implements OnClickListener {

	private View viewInflate;
	private final String TEL_PREFIX = "tel:";
	private final String MAIL_PREFIX = "mailto:";

	public AboutUsFragment() {

	}

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		viewInflate = inflater.inflate(R.layout.fragment_about_us, null);

		return viewInflate;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);

		// ImageView imgViewMenu = (ImageView)
		// viewInflate.findViewById(R.id.imgViewMenu);
		// imgViewMenu.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// MainActivity main = (MainActivity) getActivity();
		// main.showSideMenu();
		// }
		// });
		//
		// Button btnContactUs = (Button)
		// viewInflate.findViewById(R.id.btnContactUs);
		// btnContactUs.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// email();
		// }
		// });
		WebView textView2 = (WebView) viewInflate.findViewById(R.id.textView2);
		textView2.getSettings().setLoadWithOverviewMode(true);
		textView2.getSettings().setUseWideViewPort(false);
//		textView2.getSettings().setBuiltInZoomControls(true);
		textView2.getSettings().setJavaScriptEnabled(true);

		
		textView2.setBackgroundColor(0x00000000);
		textView2.setWebViewClient(new CustomWebClient());
		textView2.loadUrl("file:///android_asset/aboutus.htm");

		ImageView imgViewMenu = (ImageView) viewInflate
				.findViewById(R.id.imgViewMenu);
		imgViewMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity main = (MainActivity) getActivity();
				main.showSideMenu();
			}
		});

		Button btnContactUs = (Button) viewInflate
				.findViewById(R.id.btnContactUs);
		btnContactUs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				email();
			}
		});
		Button btnCallUs = (Button) viewInflate.findViewById(R.id.btnCallUs);
		btnCallUs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:3205422852"));
				startActivity(callIntent);
			}
		});

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	private void email() {

		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { Config.ABOUT_US_EMAIL });

		emailIntent.putExtra(Intent.EXTRA_SUBJECT, MGUtilities
				.getStringFromResource(getActivity(),
						R.string.email_subject_company));

		emailIntent.putExtra(Intent.EXTRA_TEXT, MGUtilities
				.getStringFromResource(getActivity(),
						R.string.email_body_company));
		emailIntent.setType("message/rfc822");

		getActivity().startActivity(
				Intent.createChooser(emailIntent, MGUtilities
						.getStringFromResource(getActivity(),
								R.string.choose_email_client)));
	}

	class CustomWebClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			// return super.shouldOverrideUrlLoading(view, url);
			if (url.startsWith(TEL_PREFIX)) {
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse(url));
				startActivity(intent);
				return true;
			} else if (url.startsWith(MAIL_PREFIX)) {
				email();
				return true;
			}
			return false;
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MGUtilities.screenTracking("About us", getActivity());
	}
}
