package com.fragments;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.adapters.MGListAdapter;
import com.adapters.MGListAdapter.OnMGListAdapterAdapterListener;
import com.config.Config;
import com.config.UIConfig;
import com.db.Queries;
import com.fragments.activity.StoreActivity;
import com.models.Category;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.twocity.MainActivity;
import com.twocity.R;
import com.utilities.MGUtilities;

public class CategoryFragment extends Fragment implements OnItemClickListener{
	
	private View viewInflate;
	private ArrayList<Category> categoryList;
	DisplayImageOptions options;
	public CategoryFragment() { 
		
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewInflate = inflater.inflate(R.layout.fragment_category, null);
		
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
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(UIConfig.SLIDER_PLACEHOLDER)
		.showImageForEmptyUri(UIConfig.SLIDER_PLACEHOLDER)
		.showImageOnFail(UIConfig.SLIDER_PLACEHOLDER)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		MainActivity main = (MainActivity) this.getActivity();
		final Queries q = main.getQueries();
		
		
		ImageView imgViewMenu = (ImageView) viewInflate.findViewById(R.id.imgViewMenu);
		imgViewMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity main = (MainActivity) getActivity();
				main.showSideMenu();
			}
		});
		
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				categoryList = q.getCategories(); 
				showList();
			}
		}, Config.DELAY_SHOW_ANIMATION);
	}
	
	private void showList() {
		
		ListView listView = (ListView) viewInflate.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		MGListAdapter adapter = new MGListAdapter(
				getActivity(), categoryList.size(), R.layout.category_entry);
		
		adapter.setOnMGListAdapterAdapterListener(new OnMGListAdapterAdapterListener() {
			
			@Override
			public void OnMGListAdapterAdapterCreated(MGListAdapter adapter, View v,
					int position, ViewGroup viewGroup) {
				// TODO Auto-generated method stub
				
				Category category = categoryList.get(position);
				
				Spanned title = Html.fromHtml(category.getCategory());
				TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
				tvTitle.setText(title);
				tvTitle.setVisibility(View.GONE);
				
				ImageView imageView2 = (ImageView) v.findViewById(R.id.imageView2);
				Log.d("System out","cat:url: "+category.getCategory_icon());
//				if(category.getCategory_icon() != null) {
					MainActivity.getImageLoader().displayImage(category.getCategory_icon(), imageView2, options);
//				}
//				else {
//					MainActivity.getImageLoader().displayImage(null, imageView2, options);
//				}
			}
		});
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int pos, long resId) {
		// TODO Auto-generated method stub
		Category category = categoryList.get(pos);
		
//		StoreFragment storeFragment = new StoreFragment();
//		Bundle b = new Bundle();
//		b.putSerializable("category", category);
//		storeFragment.setArguments(b);
//		
//		MainActivity main = (MainActivity)this.getActivity();
//		main.switchContent(storeFragment, true);
		
		Intent i = new Intent(getActivity(), StoreActivity.class);
		i.putExtra("category", category);
		getActivity().startActivity(i);
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MGUtilities.screenTracking("Category", getActivity());
	}
}
