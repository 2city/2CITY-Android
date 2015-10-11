package com.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adapters.MGListAdapter;
import com.adapters.MGListAdapter.OnMGListAdapterAdapterListener;
import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.config.UIConfig;
import com.dataparser.DataParser;
import com.db.Queries;
import com.fragments.activity.DetailActivity;
import com.imageview.MGImageView;
import com.models.Category;
import com.models.Data;
import com.models.DataNews;
import com.models.News;
import com.models.Photo;
import com.models.Store;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.slider.MGSlider;
import com.slider.MGSlider.OnMGSliderListener;
import com.slider.MGSliderAdapter;
import com.slider.MGSliderAdapter.OnMGSliderAdapterListener;
import com.twocity.MainActivity;
import com.twocity.R;
import com.utilities.MGUtilities;

public class HomeFragment extends Fragment implements OnItemClickListener,
		OnClickListener {

	private View viewInflate;
	DisplayImageOptions options;
	ArrayList<Store> storeList;
	ArrayList<News> newsList;
	private Queries q;
	
	public HomeFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		viewInflate = inflater.inflate(R.layout.fragment_home, null);

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
				.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();

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

		Handler h = new Handler();
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				getData();
			}
		}, Config.DELAY_SHOW_ANIMATION);

	}

	public void getData() {
		
		MGAsyncTask task = new MGAsyncTask(getActivity());
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
				MainActivity main = (MainActivity) getActivity();
				if(main!=null){
					q = main.getQueries();
					storeList = q.getStoresFeatured();
					newsList = q.getNews();
					
					/*
					 * filter store current location city wise.
					 * If no store found, will show all stores.
					 */
					try {
						List<Address> adds = MainActivity.addresses;
						Log.d("System out","adds: "+adds);
						if(adds!=null && adds.size()>0){
							String cityName = adds.get(0).getLocality();
							if(cityName!=null && cityName.length()>0){
								cityName = cityName.toString().toLowerCase();
								Log.d("System out","my cityName: "+cityName);
								ArrayList<Store> storeListTemp = new ArrayList<Store>();
								for(int i=0;i<storeList.size();i++){
									String nm = storeList.get(i).getPhone_no().toString().trim().toLowerCase();
									Log.d("System out","store cityName: "+nm);
									if(nm!=null && nm.length()>0){
										if(cityName.contains(nm)){
											storeListTemp.add(storeList.get(i));
										}
									}
								}
								if(storeListTemp.size()>0){
									storeList = new ArrayList<Store>();
									for(int i=0;i<storeListTemp.size();i++){
										storeList.add(storeListTemp.get(i));
									}
								}
							}else{
								Log.d("System out","no city name:"+cityName);
							}
						}else{
							Log.d("System out","Address null or size 0");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					try {
						Collections.sort(storeList, new StoreComparator());
					} catch (Exception e) {
						e.printStackTrace();
					}
					createSlider();
					showList();
				}				
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				try {
					
					DataParser parser = new DataParser();
					Data data = parser.getData(Config.DATA_JSON_URL);
					DataNews dataNews = parser.getDataNews(Config.DATA_NEWS_URL);
					
					MainActivity main = (MainActivity) getActivity();
					Queries q = main.getQueries();
					
					if(data == null)
						return;
					
					if(data.getCategories() != null && data.getCategories().size() > 0) {
						
						q.deleteTable("categories");
						for(Category cat : data.getCategories()) {
							q.insertCategory(cat);
						}

						Log.e("HOME FRAGMENT LOG", "Store count =" + data.getCategories().size());
					}
					
					if(data.getPhotos() != null && data.getPhotos().size() > 0) {
						
						q.deleteTable("photos");
						for(Photo photo : data.getPhotos()) {
							q.insertPhoto(photo);
						}
					}
					
					if(data.getStores() != null && data.getStores().size() > 0) {
						
						q.deleteTable("stores");
						for(Store store : data.getStores()) {
							q.insertStore(store);
						}
						
						Log.e("HOME FRAGMENT LOG", "Store count =" + data.getStores().size());
					}
					
					if(dataNews.getNews() != null && dataNews.getNews().size() > 0) {
						
						q.deleteTable("news");
						for(News news : dataNews.getNews()) {
							q.insertNews(news);
						}
						
						Log.e("HOME FRAGMENT LOG", "Store count =" + dataNews.getNews().size());
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		task.execute();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (viewInflate != null) {
			ViewGroup parentViewGroup = (ViewGroup) viewInflate.getParent();
			if (parentViewGroup != null) {

				MGSlider slider = (MGSlider) viewInflate
						.findViewById(R.id.slider);
				slider.pauseSliderAnimation();

				parentViewGroup.removeAllViews();
			}
		}
	}

	final SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
	SimpleDateFormat sdf1 = new SimpleDateFormat("dd-mm-yyyy");

	private void showList() {

		ListView listView = (ListView) viewInflate.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		MGListAdapter adapter = new MGListAdapter(getActivity(),
				storeList.size(), R.layout.home_entry);

		adapter.setOnMGListAdapterAdapterListener(new OnMGListAdapterAdapterListener() {

			@Override
			public void OnMGListAdapterAdapterCreated(MGListAdapter adapter,
					View v, int position, ViewGroup viewGroup) {
				// TODO Auto-generated method stub

				/*
				 * News news = newsList.get(position);
				 * 
				 * MGImageView imgViewPhoto = (MGImageView)
				 * v.findViewById(R.id.imgViewPhoto);
				 * imgViewPhoto.setCornerRadius(0.0f);
				 * imgViewPhoto.setBorderWidth(UIConfig.BORDER_WIDTH);
				 * imgViewPhoto.setBorderColor(getResources().getColor(UIConfig.
				 * THEME_BLACK_COLOR));
				 * 
				 * if(news.getPhoto_url() != null) {
				 * MainActivity.getImageLoader(
				 * ).displayImage(news.getPhoto_url(), imgViewPhoto, options); }
				 * else { MainActivity.getImageLoader().displayImage(null,
				 * imgViewPhoto, options); }
				 * 
				 * imgViewPhoto.setTag(position);
				 * 
				 * Spanned name = Html.fromHtml(news.getNews_title()); Spanned
				 * address = Html.fromHtml(news.getNews_content());
				 * 
				 * TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
				 * tvTitle.setText(name);
				 * 
				 * TextView tvSubtitle = (TextView)
				 * v.findViewById(R.id.tvSubtitle); tvSubtitle.setText(address);
				 * 
				 * 
				 * String date =
				 * DateTimeHelper.getStringDateFromTimeStamp(news.getCreated_at
				 * (), "MM/dd/yyyy" ); TextView tvDate = (TextView)
				 * v.findViewById(R.id.tvDate); tvDate.setText(date);
				 */
				final Store store = storeList.get(position);

				Photo p = q.getPhotoByStoreId(store.getStore_id());

				MGImageView imgViewPhoto = (MGImageView) v
						.findViewById(R.id.imgViewPhoto);
				imgViewPhoto.setCornerRadius(0.0f);
				imgViewPhoto.setBorderWidth(UIConfig.BORDER_WIDTH);
				imgViewPhoto.setBorderColor(getResources().getColor(
						UIConfig.THEME_BLACK_COLOR));
				// imgViewPhoto.setOnClickListener(new OnClickListener() {
				//
				// @Override
				// public void onClick(View arg0) {
				// // TODO Auto-generated method stub
				//
				//
				// // DetailFragment fragment = new DetailFragment();
				// // Bundle b = new Bundle();
				// // b.putSerializable("store", store);
				// // fragment.setArguments(b);
				// // main.switchContent(fragment, true);
				//
				// Intent i = new Intent(getActivity(), DetailActivity.class);
				// i.putExtra("store", store);
				// getActivity().startActivity(i);
				// }
				// });

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

				// float rating = 0;
				//
				// if(store.getRating_total() > 0 && store.getRating_count() >
				// 0)
				// rating = store.getRating_total() / store.getRating_count();
				//
				// String strRating = String.format("%.2f %s %d %s",
				// rating,
				// getActivity().getResources().getString(R.string.average_based_on),
				// store.getRating_count(),
				// getActivity().getResources().getString(R.string.rating));
				//
				// RatingBar ratingBar = (RatingBar)
				// v.findViewById(R.id.ratingBar);
				// ratingBar.setRating(rating);
				//
				// TextView tvRatingBarInfo = (TextView)
				// v.findViewById(R.id.tvRatingBarInfo);
				//
				//
				// if(rating > 0)
				// tvRatingBarInfo.setText(strRating);
				// else
				// tvRatingBarInfo.setText(
				// getActivity().getResources().getString(R.string.no_rating));
				//
				//
				// Favorite fave = q.getFavoriteByStoreId(store.getStore_id());
				//
				// ImageView imgViewFeatured = (ImageView)
				// v.findViewById(R.id.imgViewFeatured);
				// imgViewFeatured.setVisibility(View.VISIBLE);
				//
				// ImageView imgViewStarred = (ImageView)
				// v.findViewById(R.id.imgViewStarred);
				// imgViewStarred.setVisibility(View.VISIBLE);
				//
				// if(fave == null)
				// imgViewStarred.setVisibility(View.INVISIBLE);
				//
				// if(store.getFeatured() == 0)
				// imgViewFeatured.setVisibility(View.INVISIBLE);
			}
		});
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		listView.invalidate();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int pos,
			long resId) {
		// TODO Auto-generated method stub

		MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
		slider.stopSliderAnimation();

		// News news = newsList.get(pos);
		// Intent i = new Intent(getActivity(), NewsDetailActivity.class);
		// i.putExtra("news", news);
		// getActivity().startActivity(i);

		Store store = storeList.get(pos);
		Intent i = new Intent(getActivity(), DetailActivity.class);
		i.putExtra("store", store);
		getActivity().startActivity(i);

		// NewsDetailFragment fragment = new NewsDetailFragment();
		// Bundle b = new Bundle();
		// b.putSerializable("news", news);
		// fragment.setArguments(b);
		//
		// MainActivity main = (MainActivity)this.getActivity();
		// main.switchContent(fragment, true);
		//
		// TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
		// tvTitle.setTextColor(getResources().getColor(UIConfig.THEME_COLOR));
		//
		// ImageView imgArrow = (ImageView) v.findViewById(R.id.imgArrow);
		// imgArrow.setImageResource(UIConfig.LIST_ARROW_SELECTED);
		//
		// Category cat = categoryList.get(pos);
		//
		// Bundle b = new Bundle();
		// b.putInt("categoryId", cat.category_id);
		//
		// RestaurantFragment frag = new RestaurantFragment();
		// frag.setArguments(b);
		//
		// MainActivity main = (MainActivity)getActivity();
		//
		// main.changeFragment(
		// Constants.FRAGMENT_TAB_1_RESTAURANT,
		// R.id.frameMainContainer,
		// frag,
		// true);
	}

	// Create Slider
	private void createSlider() {

		final MainActivity main = (MainActivity) getActivity();
		final Queries q = main.getQueries();

		MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
		slider.setMaxSliderThumb(storeList.size());
		MGSliderAdapter adapter = new MGSliderAdapter(R.layout.slider_entry,
				storeList.size(), storeList.size());

		adapter.setOnMGSliderAdapterListener(new OnMGSliderAdapterListener() {

			@Override
			public void onOnMGSliderAdapterCreated(MGSliderAdapter adapter,
					View v, int position) {
				// TODO Auto-generated method stub

				final Store entry = storeList.get(position);
				Photo p = q.getPhotoByStoreId(entry.getStore_id());

				ImageView imageViewSlider = (ImageView) v
						.findViewById(R.id.imageViewSlider);

				if (p != null) {
					MainActivity.getImageLoader().displayImage(
							p.getPhoto_url(), imageViewSlider, options);
				} else {
					imageViewSlider
							.setImageResource(UIConfig.SLIDER_PLACEHOLDER);
				}

				imageViewSlider.setTag(position);
				imageViewSlider.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						// DetailFragment fragment = new DetailFragment();
						// Bundle b = new Bundle();
						// b.putSerializable("store", entry);
						// fragment.setArguments(b);
						//
						// main.switchContent(fragment, true);

						Intent i = new Intent(getActivity(),
								DetailActivity.class);
						i.putExtra("store", entry);
						getActivity().startActivity(i);
					}
				});

				Spanned name = Html.fromHtml(entry.getStore_name());
				Spanned address = Html.fromHtml(entry.getStore_address());

				TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
				tvTitle.setText(name);

				TextView tvSubtitle = (TextView) v
						.findViewById(R.id.tvSubtitle);
				tvSubtitle.setText(address);
			}
		});

		slider.setOnMGSliderListener(new OnMGSliderListener() {

			@Override
			public void onItemThumbSelected(MGSlider slider,
					ImageView[] buttonPoint, ImageView imgView, int pos) {
			}

			@Override
			public void onItemThumbCreated(MGSlider slider, ImageView imgView,
					int pos) {
			}

			@Override
			public void onItemPageScrolled(MGSlider slider,
					ImageView[] buttonPoint, int pos) {
			}

			@Override
			public void onItemMGSliderToView(MGSlider slider, int pos) {
			}

			@Override
			public void onItemMGSliderViewClick(AdapterView<?> adapterView,
					View v, int pos, long resid) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAllItemThumbCreated(MGSlider slider,
					LinearLayout linearLayout) {
			}

		});

		slider.setOffscreenPageLimit(storeList.size() - 1);
		slider.setAdapter(adapter);
		slider.setActivity(this.getActivity());
		slider.setSliderAnimation(5000);
		slider.resumeSliderAnimation();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
		slider.stopSliderAnimation();

		switch (v.getId()) {

		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
		slider.resumeSliderAnimation();
		Log.e("RESUME STATE", "RESUME STATE");
		MGUtilities.screenTracking("Home", getActivity());
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
		slider.pauseSliderAnimation();
		Log.e("PAUSE STATE", "PAUSE STATE");
	}

	class StoreComparator implements Comparator<Store> {

		@Override
		public int compare(Store lhs, Store rhs) {
			// TODO Auto-generated method stub
			String l = lhs.getDate_for_ordering();
			String r = rhs.getDate_for_ordering();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			try {
				Date datel=sdf.parse(l);
				Date dater=sdf.parse(r);
//				return datel.compareTo(dater);
				if (datel.before(dater)) {
		            return -1;
		        } else if (datel.after(dater)) {
		            return 1;
		        } else {
		            return 0;
		        }  
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}

	}
//	private DrawerLayout mDrawerLayout;
//    private ActionBarDrawerToggle mDrawerToggle;
//	private void initSlidingDrawer(){
//		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
//		// ActionBarDrawerToggle ties together the the proper interactions
//        // between the sliding drawer and the action bar app icon
//        mDrawerToggle = new ActionBarDrawerToggle(
//                this,                  /* host Activity */
//                mDrawerLayout,         /* DrawerLayout object */
//                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
//                R.string.drawer_open,  /* "open drawer" description for accessibility */
//                R.string.drawer_close  /* "close drawer" description for accessibility */
//                ) {
//            public void onDrawerClosed(View view) {
//                getActionBar().setTitle("Title");
//                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//            }
//
//            public void onDrawerOpened(View drawerView) {
//                getActionBar().setTitle("mDrawerTitle");
//                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//            }
//        };
//        mDrawerLayout.setDrawerListener(mDrawerToggle);
//	}
//	
//
//	
//	
//	@Override
//    public boolean onCreateOptionsMenu(Menu menu) {
////        MenuInflater inflater = getMenuInflater();
////        inflater.inflate(R.menu.main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    /* Called whenever we call invalidateOptionsMenu() */
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
////        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
//        return super.onPrepareOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//         // The action bar home/up action should open or close the drawer.
//         // ActionBarDrawerToggle will take care of this.
//        if (mDrawerToggle.onOptionsItemSelected( (android.view.MenuItem) item)) {
//            return true;
//        }
//        // Handle action buttons
//        switch(item.getItemId()) {
//        /*case R.id.action_websearch:
//            // create intent to perform web search for this planet
//            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
//            intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
//            // catch event that there's no activity to handle intent
//            if (intent.resolveActivity(getPackageManager()) != null) {
//                startActivity(intent);
//            } else {
//                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
//            }
//            return true;*/
//        default:
//            return super.onOptionsItemSelected(item);
//        }
//    }
//
//    /* The click listner for ListView in the navigation drawer */
//    private class DrawerItemClickListener implements ListView.OnItemClickListener {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            selectItem(position);
//        }
//    }
//
//    private void selectItem(int position) {/*
//        // update the main content by replacing fragments
//        Fragment fragment = new PlanetFragment();
//        Bundle args = new Bundle();
//        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//        fragment.setArguments(args);
//
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
//
//        // update selected item and title, then close the drawer
//        mDrawerList.setItemChecked(position, true);
//        setTitle(mPlanetTitles[position]);
//        mDrawerLayout.closeDrawer(mDrawerList);
//    */}
//
//    @Override
//    public void setTitle(CharSequence title) {
////        mTitle = title;
////        getActionBar().setTitle(mTitle);
//    }
//
//    /**
//     * When using the ActionBarDrawerToggle, you must call it during
//     * onPostCreate() and onConfigurationChanged()...
//     */
//
//    @Override
//	public void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        // Sync the toggle state after onRestoreInstanceState has occurred.
//        mDrawerToggle.syncState();
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        // Pass any configuration change to the drawer toggls
//        mDrawerToggle.onConfigurationChanged(newConfig);
//    }


}
