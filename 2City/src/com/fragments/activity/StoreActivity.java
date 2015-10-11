package com.fragments.activity;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.adapters.MGListAdapter;
import com.adapters.MGListAdapter.OnMGListAdapterAdapterListener;
import com.config.UIConfig;
import com.db.DbHelper;
import com.db.Queries;
import com.imageview.MGImageView;
import com.models.Category;
import com.models.Photo;
import com.models.Store;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.twocity.MainActivity;
import com.twocity.R;

public class StoreActivity extends FragmentActivity implements OnItemClickListener{
	
	private ArrayList<Store> arrayData;
	DisplayImageOptions options;
	private Queries q;
	private SQLiteDatabase db;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		

		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		setContentView(R.layout.fragment_store);
		
		DbHelper dbHelper = new DbHelper(this);
		q = new Queries(db, dbHelper);
		
		ImageView imgViewMenu = (ImageView) findViewById(R.id.imgViewMenu);
		imgViewMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(UIConfig.SLIDER_PLACEHOLDER)
		.showImageForEmptyUri(UIConfig.SLIDER_PLACEHOLDER)
		.showImageOnFail(UIConfig.SLIDER_PLACEHOLDER)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		
		Category category = (Category)this.getIntent().getSerializableExtra("category");
		
		arrayData = q.getStoresByCategoryId(category.getCategory_id());
		showList();
	}
	
	private void showList() {
		
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		MGListAdapter adapter = new MGListAdapter(
				StoreActivity.this, arrayData.size(), R.layout.store_entry);
		
		adapter.setOnMGListAdapterAdapterListener(new OnMGListAdapterAdapterListener() {
			
			@Override
			public void OnMGListAdapterAdapterCreated(MGListAdapter adapter, View v,
					int position, ViewGroup viewGroup) {
				// TODO Auto-generated method stub
				
//				final Store store = arrayData.get(position);
//				
//				Photo p = q.getPhotoByStoreId(store.getStore_id());
//				
//				MGImageView imgViewPhoto = (MGImageView) v.findViewById(R.id.imgViewPhoto);
//				imgViewPhoto.setCornerRadius(0.0f);
//				imgViewPhoto.setBorderWidth(UIConfig.BORDER_WIDTH);
//				imgViewPhoto.setBorderColor(getResources().getColor(UIConfig.THEME_BLACK_COLOR));
//				imgViewPhoto.setOnClickListener(new OnClickListener() {
//					
//					@Override
//					public void onClick(View arg0) {
//						// TODO Auto-generated method stub
//						
//						
////						DetailFragment fragment = new DetailFragment();
////						Bundle b = new Bundle();
////						b.putSerializable("store", store);
////						fragment.setArguments(b);
////						main.switchContent(fragment, true);
//						
//						Intent i = new Intent(StoreActivity.this, DetailActivity.class);
//						i.putExtra("store", store);
//						StoreActivity.this.startActivity(i);
//					}
//				});
//				
//				if(p != null) {
//					MainActivity.getImageLoader().displayImage(p.getPhoto_url(), imgViewPhoto, options);
//				}
//				else {
//					imgViewPhoto.setImageResource(UIConfig.SLIDER_PLACEHOLDER);
//				}
//				
//				
//				Spanned name = Html.fromHtml(store.getStore_name());
//				Spanned address = Html.fromHtml(store.getStore_address());
//				
//				TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
//				tvTitle.setText(name);
//				
//				TextView tvSubtitle = (TextView) v.findViewById(R.id.tvSubtitle);
//				tvSubtitle.setText(address);
//				
//				float rating = 0;
//				
//				if(store.getRating_total() > 0 && store.getRating_count() > 0)
//					rating = store.getRating_total() / store.getRating_count();
//				
//				String strRating = String.format("%.2f %s %d %s", 
//						rating, 
//						StoreActivity.this.getResources().getString(R.string.average_based_on),
//						store.getRating_count(),
//						StoreActivity.this.getResources().getString(R.string.rating));
//				
//				RatingBar ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);
//				ratingBar.setRating(rating);
//				
//				TextView tvRatingBarInfo = (TextView) v.findViewById(R.id.tvRatingBarInfo);
//				
//				
//				if(rating > 0)
//					tvRatingBarInfo.setText(strRating);
//				else
//					tvRatingBarInfo.setText(
//							StoreActivity.this.getResources().getString(R.string.no_rating));
//				
//				
//				Favorite fave = q.getFavoriteByStoreId(store.getStore_id());
//				
//				ImageView imgViewFeatured = (ImageView) v.findViewById(R.id.imgViewFeatured);
//				imgViewFeatured.setVisibility(View.VISIBLE);
//				
//				ImageView imgViewStarred = (ImageView) v.findViewById(R.id.imgViewStarred);
//				imgViewStarred.setVisibility(View.VISIBLE);
//				
//				if(fave == null)
//					imgViewStarred.setVisibility(View.INVISIBLE);
//				
//				if(store.getFeatured() == 0)
//					imgViewFeatured.setVisibility(View.INVISIBLE);
				
				final Store store = arrayData.get(position);

				Photo p = q.getPhotoByStoreId(store.getStore_id());

				MGImageView imgViewPhoto = (MGImageView) v
						.findViewById(R.id.imgViewPhoto);
				imgViewPhoto.setCornerRadius(0.0f);
				imgViewPhoto.setBorderWidth(UIConfig.BORDER_WIDTH);
				imgViewPhoto.setBorderColor(getResources().getColor(
						UIConfig.THEME_BLACK_COLOR));

				if (p != null) {
					MainActivity.getImageLoader().displayImage(
							p.getPhoto_url(), imgViewPhoto, options);
				} else {
					imgViewPhoto.setImageResource(UIConfig.SLIDER_PLACEHOLDER);
				}

				Spanned name = Html.fromHtml(store.getStore_name());
				Spanned address = Html.fromHtml(store.getStore_address());

				TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
				tvTitle.setText(name);

				TextView tvSubtitle = (TextView) v
						.findViewById(R.id.tvSubtitle);
				tvSubtitle.setText(address);

				TextView tvDate = (TextView) v.findViewById(R.id.tvDate);
				try {
					 tvDate.setText(store.getSms_no());
//					tvDate.setText(/* store.getSms_no() */sdf1.format(sdf
//							.parse(store.getDate_for_ordering())));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int pos, long resid) {
		// TODO Auto-generated method stub
//		MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
//		slider.stopSliderAnimation();

		// News news = newsList.get(pos);
		// Intent i = new Intent(getActivity(), NewsDetailActivity.class);
		// i.putExtra("news", news);
		// getActivity().startActivity(i);

		Store store = arrayData.get(pos);
		Intent i = new Intent(StoreActivity.this, DetailActivity.class);
		i.putExtra("store", store);
		startActivity(i);
	}

}
