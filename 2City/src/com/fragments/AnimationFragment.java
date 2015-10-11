package com.fragments;

import com.twocity.MainActivity;
import com.twocity.R;
import com.utilities.MGUtilities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class AnimationFragment extends Fragment {
	
	private View viewInflate;
	
	public AnimationFragment() { 
		
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewInflate = inflater.inflate(R.layout.fragment_animation, null);
		
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
		MGUtilities.screenTracking("Animation", getActivity());
	}
	
}
