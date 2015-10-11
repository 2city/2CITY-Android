package com.fragments;

import java.util.ArrayList;
import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.db.Queries;
import com.fragments.activity.SearchResultActivity;
import com.models.Category;
import com.models.Store;
import com.twocity.MainActivity;
import com.twocity.R;
import com.utilities.MGUtilities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SearchFragment extends Fragment implements OnClickListener{
	
	private View viewInflate;
	private EditText txtKeywords;
	private SeekBar seekbarRadius;
	private Spinner spinnerCategories;
	private ToggleButton toggleButtonNearby;
	
	public SearchFragment() { 
		
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewInflate = inflater.inflate(R.layout.fragment_search, null);
		
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
		
		MainActivity main = (MainActivity) this.getActivity();
		Queries q = main.getQueries();
		
		ImageView imgViewMenu = (ImageView) viewInflate.findViewById(R.id.imgViewMenu);
		imgViewMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity main = (MainActivity) getActivity();
				main.showSideMenu();
			}
		});
		
		Button btnSearch = (Button) viewInflate.findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(this);
		
		
		txtKeywords = (EditText) viewInflate.findViewById(R.id.txtKeywords);
		toggleButtonNearby = (ToggleButton) viewInflate.findViewById(R.id.toggleButtonNearby);
		toggleButtonNearby.setOnClickListener(this);
		
		
		final TextView tvRadiusText = (TextView) viewInflate.findViewById(R.id.tvRadiusText);
		
		seekbarRadius = (SeekBar) viewInflate.findViewById(R.id.seekbarRadius);
		seekbarRadius.setMax(Config.MAX_SEARCH_RADIUS);
		seekbarRadius.setProgress(Config.MAX_SEARCH_RADIUS / 3);
		seekbarRadius.setEnabled(false);
		seekbarRadius.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				
				String strSeekVal = String.format("%s: %d %s", 
						MGUtilities.getStringFromResource(getActivity(), R.string.radius), 
						progress,
						MGUtilities.getStringFromResource(getActivity(), R.string.km));
				
				tvRadiusText.setText(strSeekVal);
			}
		});
	
		ArrayList<String> categories = q.getCategoryNames();
		
		String allCategories = this.getActivity().getResources().getString(R.string.all_categories);
		categories.add(0, allCategories);
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item, categories);
         
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinnerCategories = (Spinner) viewInflate.findViewById(R.id.spinnerCategories);
		spinnerCategories.setAdapter(dataAdapter);
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch(v.getId()) {
		
			case R.id.toggleButtonNearby:
				
				
				if(toggleButtonNearby.isChecked()) {
					MainActivity mainActivity = (MainActivity) this.getActivity();
					
					if(mainActivity.location == null)
						mainActivity.getLocation();
					
					seekbarRadius.setEnabled(true);
				}
				else {
					seekbarRadius.setEnabled(false);
				}
				
				
				
				break;
				
			case R.id.btnSearch:
				asyncSearch();
				break;
				
		}
	}
	
	
	private void asyncSearch() {
		
		MGAsyncTask task = new MGAsyncTask(getActivity());
		task.setMGAsyncTaskListener(new OnMGAsyncTaskListener() {
			
			ArrayList<Store> arrayFilter;
			
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
				
//				Bundle b = new Bundle();
//				b.putSerializable("searchResults", arrayFilter);
//				
//				SearchResultFragment fragment = new SearchResultFragment();
//				fragment.setArguments(b);
//				
//				MainActivity main = (MainActivity)getActivity();
//				main.switchContent(fragment, true);
				
				Intent i = new Intent(getActivity(), SearchResultActivity.class);
				i.putExtra("searchResults", arrayFilter);
				getActivity().startActivity(i);
				
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				arrayFilter = search();
			}
		});
		task.execute();
	}
	
	private ArrayList<Store> search() {

		MainActivity main = (MainActivity) this.getActivity();
		Queries q = main.getQueries();
		
		String strKeywords = txtKeywords.getText().toString().trim();
	    
	    int radius = seekbarRadius.getProgress();
	    String category = spinnerCategories.getSelectedItem().toString();
	    
	    int countParams = strKeywords.length() > 0 ? 1 : 0;
	    countParams += radius > 0 && toggleButtonNearby.isChecked() ? 1 : 0;
	    countParams += category.length() > 0 ? 1 : 0;
	    
	    ArrayList<Store> arrayStores = q.getStores();
	    
	    
	    ArrayList<Store> arrayFilter = new ArrayList<Store>();
	    for(Store store : arrayStores) {
	    	
	    	int qualifyCount = 0;
	        
	        
	        boolean isFoundKeyword = store.getStore_name().toLowerCase().contains(strKeywords) ||
	                               store.getStore_address().toLowerCase().contains(strKeywords);
	        
	        if( strKeywords.length() > 0  && isFoundKeyword)
	            qualifyCount += 1;
	        
	        if( category.length() > 0) {
	            
	            Category storeCat = q.getCategoryByCategory(category);
	            
	            boolean isFoundCat = false;
	            
	            if(storeCat != null && storeCat.getCategory_id() == store.getCategory_id())
	                isFoundCat = true;
	            
	            if(spinnerCategories.getSelectedItemPosition() == 0)
	                isFoundCat = true;
	            
	            if(isFoundCat)
	                qualifyCount += 1;
	        }
	        
	        store.setDistance(-1);
	        if(toggleButtonNearby.isChecked()) {
	        	MainActivity mainActivity = (MainActivity) this.getActivity();
				
				if(mainActivity.location != null) {
					Location locStore = new Location("Store");
					locStore.setLatitude(store.getLat());
					locStore.setLongitude(store.getLon());
					
					double distance = locStore.distanceTo(mainActivity.location) / 1000;
					store.setDistance(distance);
					
					if(distance <= radius)
		                qualifyCount += 1;
				}
				
	        }
	        
//	        store.distance = LOCALIZED(@"NOT_AVAILABLE");
//	        if(_myLocation != nil && radius > 0) {
//	            CLLocationCoordinate2D coord;
//	            coord = CLLocationCoordinate2DMake([store.lat doubleValue], [store.lon doubleValue]);
//	            CLLocation *location = [[CLLocation alloc] initWithLatitude:coord.latitude
//	                                                              longitude:coord.longitude];
//	            
//	            double distance = [_myLocation distanceFromLocation:location] / 1000;
//	            store.distance = [NSString stringWithFormat:@"%f", distance];
//	            
//	            if(distance <= radius)
//	                qualifyCount += 1;
//	        }
//	        
	        if(qualifyCount == countParams)
	        	arrayFilter.add(store);
	    }
	    
	    return arrayFilter;

	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MGUtilities.screenTracking("Search", getActivity());
	}
}
