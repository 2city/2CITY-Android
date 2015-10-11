package com.fragments;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twocity.MainActivity;
import com.twocity.R;

public class SplashFragment extends Fragment {
	
	private View viewInflate;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewInflate = inflater.inflate(R.layout.fragment_splash, null);
		generateHashKey();
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
		
		
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				UserAccessSession session = UserAccessSession
//				.getInstance(getActivity());
//				if (session.getUserSession() == null) {
//					Intent i = new Intent(getActivity(), LoginActivity.class/*RegisterActivity.class*/);
//					getActivity().startActivity(i);
//				}else{
					MainActivity act = (MainActivity)getActivity();
					act.showMainView();
//				}
			}
		}, 2000);
	}
	
	private void generateHashKey() {
		// ========START=====get hash key===========
		try {			
			PackageInfo info = getActivity().getPackageManager().getPackageInfo(
					getActivity().getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String sign = Base64
						.encodeToString(md.digest(), Base64.DEFAULT);
				Log.e("System out", "MY KEY HASH: " + sign);
				// Toast.makeText(getApplicationContext(),sign,
				// Toast.LENGTH_LONG).show();
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// =========END====get hash key===========
	}
//	@Override
//	public void onResume() {
//		// TODO Auto-generated method stub
//		super.onResume();
//		MGUtilities.screenTracking("Splash screen", getActivity());
//	}
}
