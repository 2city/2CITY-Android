package com.fragments;

import com.twocity.MainActivity;
import com.twocity.R;
import com.utilities.MGUtilities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ScrollView;


public class TermsConditionFragment extends Fragment {
	
	private View viewInflate;
	
	public TermsConditionFragment() { 
		
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewInflate = inflater.inflate(R.layout.fragment_terms_condition, null);
		
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
		
//		TextView textView2 = (TextView)viewInflate.findViewById(R.id.textView2);
//		textView2.setText(getString(R.string.terms_condition_desc));
//		textView2.setText(Html.fromHtml(getString(R.string.terms_condi_desc)));
//		try {
//			InputStream inputStream = getResources().getAssets().open("termscondi_desc.htm");
//			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
//			StringBuilder buf = new StringBuilder();
//			String str="";
//			while((str=br.readLine())!=null){
//				buf.append(str);
//			}
//			br.close();
//			textView2.setText(Html.fromHtml(buf.toString()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		WebView textView2 = (WebView)viewInflate.findViewById(R.id.textView2);
		textView2.getSettings().setLoadWithOverviewMode(true);
		textView2.getSettings().setUseWideViewPort(false);
//		textView2.getSettings().setBuiltInZoomControls(true);
		textView2.getSettings().setJavaScriptEnabled(true);
		
		textView2.setBackgroundColor(0x00000000);
		textView2.loadUrl("file:///android_asset/termscondi_desc.htm");
		
		
		ImageView imgViewMenu = (ImageView) viewInflate.findViewById(R.id.imgViewMenu);
		imgViewMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity main = (MainActivity) getActivity();
				main.showSideMenu();
			}
		});
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MGUtilities.screenTracking("Terms and condition", getActivity());
	}
	
}
