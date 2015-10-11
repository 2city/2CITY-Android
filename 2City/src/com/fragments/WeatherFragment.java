package com.fragments;

import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.dataparser.DataParser;
import com.models.DataWeather;
import com.models.Weather;
import com.twocity.MainActivity;
import com.twocity.R;
import com.utilities.MGUtilities;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class WeatherFragment extends Fragment {
	
	private View viewInflate;
	private DataWeather dataWeather;
	
	public WeatherFragment() { 
		
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewInflate = inflater.inflate(R.layout.fragment_weather, null);
		
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
		
		if(main.location == null) {
			
			MGUtilities.showAlertView(
					getActivity(), 
					R.string.location_error, 
					R.string.cannot_determine_location);
			return;
		}
		
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
				updateView();
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				MainActivity main = (MainActivity) getActivity();
				Location location = main.location;
				String weatherUrl = String.format("%slat=%f&lon=%f", 
						Config.WEATHER_URL, 
						location.getLatitude(), 
						location.getLongitude());
				
				Log.e("WEATHER URL", weatherUrl);
				
				DataParser parser = new DataParser();
				dataWeather = parser.getDataWeather(weatherUrl);
			
			}
		});
		task.execute();
	}
	
	
	private void updateView() {

		MainActivity main = (MainActivity) getActivity();
		
		TextView tvFarenheit = (TextView) viewInflate.findViewById(R.id.tvFarenheit);
		TextView tvCelsius = (TextView) viewInflate.findViewById(R.id.tvCelsius);
		TextView tvAddress = (TextView) viewInflate.findViewById(R.id.tvAddress);
		TextView tvDescription = (TextView) viewInflate.findViewById(R.id.tvDescription);
		
		tvFarenheit.setText(R.string.weather_placeholder);
		tvCelsius.setText(R.string.weather_placeholder);
		tvAddress.setText(R.string.weather_placeholder);
		tvDescription.setText(R.string.weather_placeholder);
		
		
		if(dataWeather == null)
			return;
		
		if(dataWeather.getMain() != null) {
			
			double kelvin = dataWeather.getMain().getTemp();
			
			double celsius = kelvin - 273.15;
			double fahrenheit = (celsius * 1.8) + 32 ;
			
			String farenheitStr = String.format("%.2f %s", 
					fahrenheit, 
					MGUtilities.getStringFromResource(getActivity(), R.string.fahrenheit));
			
			String celsiusStr = String.format("%.2f %s", 
					celsius, 
					MGUtilities.getStringFromResource(getActivity(), R.string.celsius));
			
			tvFarenheit.setText(farenheitStr);
			tvCelsius.setText(celsiusStr);
		}
		
		
		if(dataWeather.getWeather() != null && dataWeather.getWeather().size() > 0) {
		
			Weather weather = dataWeather.getWeather().get(0);
			tvDescription.setText(weather.getDescription());
		}
		
		if(main.addresses != null && main.addresses.size() > 0) {
			
			Address address = main.addresses.get(0);
			Log.e("", "");
			
			String locality = address.getLocality();
			String countryName = address.getCountryName();
			
			String addressStr = String.format("%s, %s", locality, countryName);
			tvAddress.setText(addressStr);
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MGUtilities.screenTracking("Weather", getActivity());
	}
}
