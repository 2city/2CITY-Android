package com.fragments;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;

import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.config.UIConfig;
import com.dataparser.DataParser;
import com.db.Queries;
import com.directions.GMapV2Direction;
import com.drawingview.DrawingView;
import com.drawingview.DrawingView.OnDrawingViewListener;
import com.fragments.activity.DetailActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.imageview.MGHSquareImageView;
import com.models.Category;
import com.models.Data;
import com.models.Favorite;
import com.models.Photo;
import com.models.Store;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sliding.MGSliding;
import com.twocity.MainActivity;
import com.twocity.R;
import com.utilities.MGUtilities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MapFragment extends Fragment implements 
	OnInfoWindowClickListener, OnMapClickListener, OnClickListener, OnDrawingViewListener{
	
	private View viewInflate;
	private GoogleMap googleMap;
	private Location myLocation;
	private HashMap<String, Store> markers;
	private ArrayList<Marker> markerList;
	
	private DisplayImageOptions options;
	private MGSliding frameSliding;
	
	private DrawingView drawingView;
	private GMapV2Direction gMapV2;
	private ArrayList<Store> storeList;
	private ArrayList<Store> selectedStoreList;
	private Store selectedStore;
	Queries q;
	
	public MapFragment() { 
		
	}
	
	@Override
    public void onDestroyView()  {
        super.onDestroyView();
        
        try {
        	if (googleMap != null) {
    	    	
    	    	FragmentManager fManager = this.getActivity().getSupportFragmentManager();
    	    	
    	    	fManager.beginTransaction()
    	            .remove(fManager.findFragmentById(R.id.googleMap)).commit();
    	    	
    	    	googleMap = null;
    	    }

            if (viewInflate != null) {
                ViewGroup parentViewGroup = (ViewGroup) viewInflate.getParent();
                if (parentViewGroup != null) {
                	
                    parentViewGroup.removeAllViews();
                }
            }
        }
        catch(Exception e) {
        	
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewInflate = inflater.inflate(R.layout.fragment_map2, null);
		
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
		
		
		final MainActivity main = (MainActivity) getActivity();
		q = main.getQueries();
		
		frameSliding = (MGSliding) viewInflate.findViewById(R.id.frameSliding);
		
		Animation animationIn = AnimationUtils.loadAnimation(this.getActivity(),
		         R.anim.slide_up2);
//		int i = android.R.anim.slide_out_right;
		Animation animationOut = AnimationUtils.loadAnimation(this.getActivity(),
		         R.anim.slide_down2);
		
		frameSliding.setInAnimation(animationIn);
		frameSliding.setOutAnimation(animationOut);
		frameSliding.setVisibility(View.GONE);
		
		ImageView imgViewDraw = (ImageView)viewInflate.findViewById(R.id.imgViewDraw);
		imgViewDraw.setOnClickListener(this);
		
		ImageView imgViewRefresh = (ImageView)viewInflate.findViewById(R.id.imgViewRefresh);
		imgViewRefresh.setOnClickListener(this);
		
		ImageView imgViewRoute = (ImageView)viewInflate.findViewById(R.id.imgViewRoute);
		imgViewRoute.setOnClickListener(this);
		
		ImageView imgViewLocation = (ImageView)viewInflate.findViewById(R.id.imgViewLocation);
		imgViewLocation.setOnClickListener(this);
		
		ImageView imgViewNearby = (ImageView)viewInflate.findViewById(R.id.imgViewNearby);
		imgViewNearby.setOnClickListener(this);
		
		ImageView imgViewMenu = (ImageView) viewInflate.findViewById(R.id.imgViewMenu);
		imgViewMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity main = (MainActivity) getActivity();
				main.showMenu();
			}
		});
		
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				getData();
			}
		}, Config.DELAY_SHOW_ANIMATION + 500);
		
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
				if (googleMap != null)
			        setUpMap();

			    if (googleMap == null) {
			        // Try to obtain the map from the SupportMapFragment.
			    	FragmentManager fManager = getActivity().getSupportFragmentManager();
			    	
			    	googleMap = ((SupportMapFragment) fManager.findFragmentById(R.id.googleMap)).getMap();
			        // Check if we were successful in obtaining the map.
			        if (googleMap != null)
			            setUpMap();
			    }
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				parseData();
				
			}
		});
		task.execute();
	}
	
	/***** Sets up the map if it is possible to do so *****/
	public void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (googleMap == null) {
	    	
	    	FragmentManager fManager = this.getActivity().getSupportFragmentManager();
	    	
	        // Try to obtain the map from the SupportMapFragment.
	    	googleMap = ((SupportMapFragment) fManager.findFragmentById(R.id.googleMap)).getMap();
	        
	        // Check if we were successful in obtaining the map.
	        if (googleMap != null)
	            setUpMap();
	    }
	}
	
	public void parseData() {
		
		MainActivity main = (MainActivity) this.getActivity();
		Queries q = main.getQueries();
		
		DataParser parser = new DataParser();
		Data data = parser.getData(Config.DATA_JSON_URL);
		
		if(data != null) {
			
			if(data.getStores() != null && data.getStores().size() > 0) {
				
				q.deleteTable("stores");
				for(Store store : data.getStores()) {
					q.insertStore(store);
				}
			}
			
			if(data.getCategories() != null && data.getCategories().size() > 0) {
				
				q.deleteTable("categories");
				for(Category cat : data.getCategories()) {
					q.insertCategory(cat);
				}
			}
			
			if(data.getPhotos() != null && data.getPhotos().size() > 0) {
				
				q.deleteTable("photos");
				for(Photo photo : data.getPhotos()) {
					q.insertPhoto(photo);
				}
			}
		}
	}
	
	public void setUpMap() {
		
		markers = new HashMap<String, Store>();
		markerList = new ArrayList<Marker>();
			
		try {
			FragmentManager myFragmentManager = this.getActivity().getSupportFragmentManager();
			
			SupportMapFragment mySupportMapFragment = 
					(SupportMapFragment)myFragmentManager.findFragmentById(R.id.googleMap);
			
			googleMap = mySupportMapFragment.getMap();
			googleMap.getUiSettings().setMyLocationButtonEnabled(true);
			googleMap.setMyLocationEnabled(true);
			googleMap.setOnMapClickListener(this);
			googleMap.setOnInfoWindowClickListener(this);
			googleMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
				
				@Override
				public void onMyLocationChange(Location location) {
					// TODO Auto-generated method stub
					myLocation = location;
				}
			});
			
			
			addStoreMarkers();			
			
			gMapV2 = new GMapV2Direction();
			drawingView = (DrawingView) viewInflate.findViewById(R.id.drawingView);
			drawingView.setBrushSize(5);
			drawingView.setPolygonFillColor(getResources().getColor(R.color.theme_black_color_opacity));
			drawingView.setColor(getResources().getColor(R.color.theme_black_color));
			drawingView.setPolylineColor(getResources().getColor(R.color.theme_black_color));
			drawingView.setGoogleMap(googleMap);
			drawingView.setOnDrawingViewListener(this);
			
			showBoundedMap();
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		// TODO Auto-generated method stub
		
		final Store store = markers.get(marker.getId());
		selectedStore = store;
		
		if(myLocation != null) {
			
			Location loc = new Location("marker");
			loc.setLatitude(marker.getPosition().latitude);
			loc.setLongitude(marker.getPosition().longitude);
			
			double meters = myLocation.distanceTo(loc);
			double miles = meters * 0.000621371f;
			
			String str = String.format("%.1f %s", 
					miles, 
					MGUtilities.getStringFromResource(getActivity(), R.string.mi));
			
			TextView tvDistance = (TextView) viewInflate.findViewById(R.id.tvDistance);
			tvDistance.setText(str);
		}
		
		final MainActivity main = (MainActivity) getActivity();
		q = main.getQueries();
		
		frameSliding.setVisibility(View.VISIBLE);
		
		ImageView imgViewThumb = (ImageView) viewInflate.findViewById(R.id.imageViewThumb);
		
		Photo p = q.getPhotoByStoreId(store.getStore_id());
		if(p != null) {
			MainActivity.getImageLoader().displayImage(p.getPhoto_url(), imgViewThumb, options);
			
		}
		else {
			imgViewThumb.setImageResource(UIConfig.SLIDER_PLACEHOLDER);
		}
		
		imgViewThumb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
//				DetailFragment fragment = new DetailFragment();
//				Bundle b = new Bundle();
//				b.putSerializable("store", store);
//				fragment.setArguments(b);
//				main.switchContent(fragment, true);
				
				Intent i = new Intent(getActivity(), DetailActivity.class);
				i.putExtra("store", store);
				getActivity().startActivity(i);
			}
		});
		
		
		
		TextView tvTitle = (TextView) viewInflate.findViewById(R.id.tvTitle);
		TextView tvSubtitle = (TextView) viewInflate.findViewById(R.id.tvSubtitle);
		
		tvTitle.setText(Html.fromHtml(store.getStore_name()));
		tvSubtitle.setText(Html.fromHtml(store.getStore_address()));
		
		ToggleButton toggleButtonFave = (ToggleButton) viewInflate.findViewById(R.id.toggleButtonFave);
		toggleButtonFave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				checkFave(v, store);
			}
		});
		

		Favorite fave = q.getFavoriteByStoreId(store.getStore_id());
		toggleButtonFave.setChecked(true);
		
		if(fave == null)
			toggleButtonFave.setChecked(false);
	}

	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub
		
		frameSliding.setVisibility(View.INVISIBLE);
	}
	
	private void checkFave(View view, Store store) {
		
		MainActivity mainActivity = (MainActivity)this.getActivity();
		Queries q = mainActivity.getQueries();
		
		Favorite fave = q.getFavoriteByStoreId(store.getStore_id());
		
		if(fave != null) {
			q.deleteFavorite(store.getStore_id());
			((ToggleButton) view).setChecked(false);
		}
		else {
			
			fave = new Favorite();
			fave.setStore_id(store.getStore_id());
			q.insertFavorite(fave);
			((ToggleButton) view).setChecked(true);
		}
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch(v.getId()) {
			case R.id.imgViewDraw:
				
				drawingView.enableDrawing(true);
				drawingView.startDrawingPolygon(true);
				
				break;
				
			case R.id.imgViewRefresh:
				addStoreMarkers();
				break;
				
			case R.id.imgViewRoute:
				getDirections();
				break;
				
			case R.id.imgViewLocation:
				getMyLocation();
				break;
				
			case R.id.imgViewNearby:
				getNearby();
				break;
		}
	}


	ArrayList<Marker> markers1;
	
	@Override
	public void onUserDidFinishDrawPolygon(PolygonOptions polygonOptions) {
		// TODO Auto-generated method stub
		
		googleMap.clear();
		googleMap.addPolygon( polygonOptions );
		
		markers1 = getMarkersInsidePoly(polygonOptions, null, markerList);
		
		markers = new HashMap<String, Store>();
		markerList = new ArrayList<Marker>();
		
		selectedStoreList = new ArrayList<Store>();
		
		markerList.clear();
		markers.clear();
		for(Marker mark1 : markers1) {
			
			for(Store entry : storeList) {
				if(mark1.getTitle().toLowerCase().compareTo(entry.getStore_name().toLowerCase()) == 0) {
					
					final MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.title( Html.fromHtml(entry.getStore_name()).toString() );
					
					Spanned storeAddress = Html.fromHtml(entry.getStore_address());
					String address = storeAddress.toString();
					
					if(storeAddress.length() > 50)
						address = storeAddress.toString().substring(0,  50) + "...";
					
					markerOptions.snippet(address);
					
					markerOptions.position(new LatLng(entry.getLat(), entry.getLon()));
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_orange));
					
					Marker mark = googleMap.addMarker(markerOptions);
					mark.setInfoWindowAnchor(Config.MAP_INFO_WINDOW_X_OFFSET, 0);
					
					Category cat = q.getCategoryByCategoryId(entry.getCategory_id());
					
					if(cat != null && cat.getCategory_icon() != null) {
						MGHSquareImageView imgView = new MGHSquareImageView(getActivity());
						imgView.setMarker(mark);
						imgView.setMarkerOptions(markerOptions);
						imgView.setTag(entry);
						MainActivity.getImageLoader().displayImage(cat.getCategory_icon(), imgView, options, new ImageLoadingListener() {
							
							@Override
							public void onLoadingStarted(String imageUri, View view) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void onLoadingFailed(String imageUri, View view,
									FailReason failReason) {
								// TODO Auto-generated method stub
								
								markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_orange));
							}
							
							@Override
							public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
								// TODO Auto-generated method stub
								
								if(loadedImage != null) {
									MGHSquareImageView v = (MGHSquareImageView)view;
									
									Marker m = (Marker)v.getMarker();
									m.remove();
									
									MarkerOptions opt = (MarkerOptions)v.getMarkerOptions();
									opt.icon(BitmapDescriptorFactory.fromBitmap(loadedImage));
									
									Marker mark = googleMap.addMarker(opt);
									Store s = (Store) v.getTag();
									
									if(markers.containsKey(m.getId())) {
										markerList.remove(m);
										markerList.add(mark);
										
										markers.remove(m);
										markers.put(mark.getId(), s);
									}
								}
								
							}
							
							@Override
							public void onLoadingCancelled(String imageUri, View view) {
								// TODO Auto-generated method stub
								
							}
						});
					}
					
					markerList.add(mark);
					markers.put(mark.getId(), entry);
					
					selectedStoreList.add(entry);
					break;
				}
			}
		}
		
		drawingView.enableDrawing(false);
		drawingView.resetPolygon();
		drawingView.startNew();
	}


	@Override
	public void onUserDidFinishDrawPolyline(PolylineOptions polylineOptions) {
		// TODO Auto-generated method stub
		
		
	}
	
	public ArrayList<Marker> getMarkersInsidePoly(PolygonOptions polygonOptions, PolylineOptions polylineOptions,  ArrayList<Marker> markers) {
		
		ArrayList<Marker> markersFound = new ArrayList<Marker>();
		for(Marker mark : markers) {
			
			Boolean isFound = polygonOptions != null ? 
					drawingView.latLongContainsInPolygon(mark.getPosition(), polygonOptions) :
						drawingView.latLongContainsInPolyline(mark.getPosition(), polylineOptions);
			
			if(isFound) {
				markersFound.add(mark);
			}
		}
		
		return markersFound;
	}
	
	
	public void addStoreMarkers() {
		
		if(googleMap != null)
			googleMap.clear();
		
		try {
			
			MainActivity main = (MainActivity) this.getActivity();
			Queries q = main.getQueries();
			storeList = q.getStores();
			
			markerList.clear();
			markers.clear();
			for(Store entry: storeList) {
				
				Log.e("LAT = " + entry.getLat(), "LON = " + entry.getLon());
				if(entry.getLat() == 0 || entry.getLon() == 0)
					continue;
				
				final MarkerOptions markerOptions = new MarkerOptions();
				
				markerOptions.title( Html.fromHtml(entry.getStore_name()).toString() );
				Spanned storeAddress = Html.fromHtml(entry.getStore_address());
				String address = storeAddress.toString();
				
				if(storeAddress.length() > 50)
					address = storeAddress.toString().substring(0,  50) + "...";
				
				markerOptions.snippet(address);
				
				markerOptions.position(new LatLng(entry.getLat(), entry.getLon()));
				markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_orange));
				
				Marker mark = googleMap.addMarker(markerOptions);
				mark.setInfoWindowAnchor(Config.MAP_INFO_WINDOW_X_OFFSET, 0);
				
				Category cat = q.getCategoryByCategoryId(entry.getCategory_id());
				
				if(cat != null && cat.getCategory_icon() != null) {
					
					Log.e("ICON", cat.getCategory_icon());
					
					MGHSquareImageView imgView = new MGHSquareImageView(getActivity());
					imgView.setMarker(mark);
					imgView.setMarkerOptions(markerOptions);
					imgView.setTag(entry);
					MainActivity.getImageLoader().displayImage(cat.getCategory_icon(), imgView, options, new ImageLoadingListener() {
						
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							// TODO Auto-generated method stub
//							markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_orange));
						}
						
						@Override
						public void onLoadingComplete(String imageUri, final View view, final Bitmap loadedImage) {
							// TODO Auto-generated method stub
							
							if(loadedImage != null) {
								MGHSquareImageView v = (MGHSquareImageView)view;
								
								Marker m = (Marker)v.getMarker();
								m.remove();
								
								MarkerOptions opt = (MarkerOptions)v.getMarkerOptions();
								opt.icon(BitmapDescriptorFactory.fromBitmap(loadedImage));
								
								Marker mark = googleMap.addMarker(opt);
								
								Store s = (Store) v.getTag();
								if(markers.containsKey(m.getId())) {
									markerList.remove(m);
									markerList.add(mark);
										
									markers.remove(m);
									markers.put(mark.getId(), s);
								}
							}
							else {
								Log.e("LOADED IMAGE", "IS NULL");
							}
						}
						
						@Override
						public void onLoadingCancelled(String imageUri, View view) {
							// TODO Auto-generated method stub
							
						}
					});
				}
				
				markerList.add(mark);
				markers.put(mark.getId(), entry);
			}
			
			showBoundedMap();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void getDirections() {
		
    	MGAsyncTask asyncTask = new MGAsyncTask(getActivity());
    	asyncTask.setMGAsyncTaskListener(new OnMGAsyncTaskListener() {
			
    		private ArrayList<ArrayList<LatLng>> allDirections;
    		
			@Override
			public void onAsyncTaskProgressUpdate(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAsyncTaskPreExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				allDirections = new ArrayList<ArrayList<LatLng>>();
			}
			
			@Override
			public void onAsyncTaskPostExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				for(ArrayList<LatLng> directions : allDirections) {
					PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.RED);
					
					for(LatLng latLng : directions) {
						rectLine.add(latLng);

					}
					
					googleMap.addPolyline(rectLine);
				}
				
				if(allDirections.size() == 0) {
					Toast.makeText(getActivity(), R.string.cannot_determine_direction, Toast.LENGTH_SHORT).show();
				}
//					btnEmail.setEnabled(true);
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				if(myLocation != null && selectedStore != null) {
					LatLng marker1 = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
					LatLng marker2 = new LatLng(selectedStore.getLat(), selectedStore.getLon());
	    			
	    			Document doc = gMapV2.getDocument1(
	    					marker1, marker2, GMapV2Direction.MODE_DRIVING);
	    			
					ArrayList<LatLng> directionPoint = gMapV2.getDirection(doc);
					
					allDirections.add(directionPoint);
				}
			}
		});
    	

    	asyncTask.startAsyncTask();
    }
	
	private void getMyLocation() {
		
		
		if(myLocation == null) {
			
			MGUtilities.showAlertView(
					getActivity(), 
					R.string.location_error, 
					R.string.cannot_determine_location);
			
			return;
		}

		addStoreMarkers();
		
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(Config.MAP_ZOOM_LEVEL);
    	googleMap.moveCamera(zoom);
    	
		CameraUpdate center = CameraUpdateFactory.newLatLng(
				new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
		
		googleMap.animateCamera(center);
		
	}
	
	private void getNearby() {
		
		
		if(googleMap != null)
			googleMap.clear();
		
		
		if(myLocation == null) {
			
			MGUtilities.showAlertView(
					getActivity(), 
					R.string.route_error, 
					R.string.route_error_details);
			
			return;
		}
		
		try {
			
			MainActivity main = (MainActivity) this.getActivity();
			Queries q = main.getQueries();
			storeList = q.getStores();
			
			
			markerList.clear();
			markers.clear();
			for(Store entry: storeList) {
				
				Location destination = new Location("Origin");
				destination.setLatitude(entry.getLat());
				destination.setLatitude(entry.getLon());
				
				double distance = myLocation.distanceTo(destination);
				
				if(distance <= Config.MAX_RADIUS_NEARBY_IN_METERS) {
					
					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.title( Html.fromHtml(entry.getStore_name()).toString() );
					
					Spanned storeAddress = Html.fromHtml(entry.getStore_address());
					String address = storeAddress.toString();
					
					if(storeAddress.length() > 50)
						address = storeAddress.toString().substring(0,  50) + "...";
					
					markerOptions.snippet(address);
					markerOptions.position(new LatLng(entry.getLat(), entry.getLon()));
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_orange));
					
					Marker mark = googleMap.addMarker(markerOptions);
					mark.setInfoWindowAnchor(Config.MAP_INFO_WINDOW_X_OFFSET, 0);
					
					
					Category cat = q.getCategoryByCategoryId(entry.getCategory_id());
					
					if(cat != null && cat.getCategory_icon() != null) {
						MGHSquareImageView imgView = new MGHSquareImageView(getActivity());
						imgView.setMarker(mark);
						imgView.setMarkerOptions(markerOptions);
						imgView.setTag(entry);
						MainActivity.getImageLoader().displayImage(cat.getCategory_icon(), imgView, options, new ImageLoadingListener() {
							
							@Override
							public void onLoadingStarted(String imageUri, View view) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void onLoadingFailed(String imageUri, View view,
									FailReason failReason) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
								// TODO Auto-generated method stub
								
								if(loadedImage != null) {
									MGHSquareImageView v = (MGHSquareImageView)view;
									
									Marker m = (Marker)v.getMarker();
									m.remove();
									
									MarkerOptions opt = (MarkerOptions)v.getMarkerOptions();
									opt.icon(BitmapDescriptorFactory.fromBitmap(loadedImage));
									
									Marker mark = googleMap.addMarker(opt);
									Store s = (Store) v.getTag();
									
									if(markers.containsKey(m.getId())) {
										markerList.remove(m);
										markerList.add(mark);
										markers.remove(m);
										markers.put(mark.getId(), s);
									}
								}
							}
							
							@Override
							public void onLoadingCancelled(String imageUri, View view) {
								// TODO Auto-generated method stub
								
							}
						});
					}
					
					markerList.add(mark);
					markers.put(mark.getId(), entry);
				}
				
			}
			
			CameraUpdate zoom = CameraUpdateFactory.zoomTo(Config.MAP_ZOOM_LEVEL);
	    	googleMap.moveCamera(zoom);
	    	
			CameraUpdate center = CameraUpdateFactory.newLatLng(
					new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
			
			googleMap.animateCamera(center);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	private void showBoundedMap() {
		if(markerList == null || markerList.size() == 0 )
			return;
		
		LatLngBounds.Builder bld = new LatLngBounds.Builder();
        
	    for (int i = 0; i < markerList.size(); i++) {
	    		Marker marker = markerList.get(i);
	    		
	            bld.include(marker.getPosition());            
	    }
	    
	    LatLngBounds bounds = bld.build();          
	    googleMap.moveCamera(
	    		CameraUpdateFactory.newLatLngBounds(bounds, 
	    		this.getResources().getDisplayMetrics().widthPixels, 
                this.getResources().getDisplayMetrics().heightPixels, 
                70));
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MGUtilities.screenTracking("Map", getActivity());
	}
	
}
