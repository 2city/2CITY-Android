package com.fragments;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.adapters.MGListAdapter;
import com.adapters.MGListAdapter.OnMGListAdapterAdapterListener;
import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.config.UIConfig;
import com.dataparser.DataParser;
import com.fragments.activity.BuyTicketDetailActivity;
import com.fragments.activity.LoginActivity;
import com.fragments.activity.ProfileActivity;
import com.imageview.TouchImageView;
import com.models.Menu;
import com.models.Menu.HeaderType;
import com.twocity.MainActivity;
import com.twocity.R;
import com.usersession.UserAccessSession;
import com.usersession.UserSession;
import com.utilities.MGUtilities;

public class MenuFragment extends Fragment implements OnItemClickListener {

	private View viewInflate;
	private Menu[] MENUS;
	String buyticketres;
	public MenuFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		viewInflate = inflater.inflate(R.layout.fragment_menu, null);

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
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.menubgimg);
		TouchImageView imgmenuslider = (TouchImageView) viewInflate.findViewById(R.id.imgmenuslider);
		imgmenuslider.setImageBitmap(bitmap);
		
		final LinearLayout imgViewMenu = (LinearLayout) viewInflate
				.findViewById(R.id.imgViewMenu);

		imgViewMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final MainActivity main = (MainActivity) getActivity();

				final Animation rotate = AnimationUtils.loadAnimation(
						MenuFragment.this.getActivity(), R.anim.rotate);
				rotate.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animation arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation arg0) {
						// TODO Auto-generated method stub
						main.showContent();
					}
				});

				imgViewMenu.setAnimation(rotate);
				imgViewMenu.startAnimation(rotate);

			}
		});

		updateMenuList();
	}

	private void showList() {

		ListView listView = (ListView) viewInflate.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
//		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setDivider(null);

		MGListAdapter adapter = new MGListAdapter(getActivity(), MENUS.length,
				R.layout.menu_entry);

		adapter.setOnMGListAdapterAdapterListener(new OnMGListAdapterAdapterListener() {

			@Override
			public void OnMGListAdapterAdapterCreated(MGListAdapter adapter,
					View v, int position, ViewGroup viewGroup) {
				// TODO Auto-generated method stub

				FrameLayout frameCategory = (FrameLayout) v
						.findViewById(R.id.frameCategory);
				FrameLayout frameHeader = (FrameLayout) v
						.findViewById(R.id.frameHeader);

				frameCategory.setVisibility(View.GONE);
				frameHeader.setVisibility(View.GONE);

				Menu menu = MENUS[position];

				if (menu.getHeaderType() == HeaderType.HeaderType_CATEGORY) {
					frameCategory.setVisibility(View.VISIBLE);
					Spanned title = Html.fromHtml(getActivity().getResources()
							.getString(menu.getMenuResTitle()));
					TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
					tvTitle.setText(title);

					ImageView imgViewIcon = (ImageView) v
							.findViewById(R.id.imgViewIcon);
					imgViewIcon.setImageResource(menu.getMenuResIconSelected());
				} else {
					frameHeader.setVisibility(View.VISIBLE);

					Spanned title = Html.fromHtml(getActivity().getResources()
							.getString(menu.getMenuResTitle()));
					TextView tvTitleHeader = (TextView) v
							.findViewById(R.id.tvTitleHeader);
					tvTitleHeader.setText(title);
				}
			}
		});
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int pos,
			long resId) {
		// TODO Auto-generated method stub
		MainActivity main = (MainActivity) this.getActivity();

		main.clearBackStack();
		Intent i;
		switch (pos) {
		case 1:
			getButTicketUrl(Config.BUY_TICKET);
//			main.switchContent(new HomeFragment(), false);
//			i = new Intent(getActivity(), BuyTicketDetailActivity.class);			
//			i.putExtra("buyticketurl", Config.BUY_TICKET);
//			getActivity().startActivity(i);
			break;
		case 0:
			main.switchContent(new HomeFragment(), false);
			break;

		case 3:
			main.switchContent(new CategoryFragment(), false);
			break;

		case 2:
			main.switchContent(new NewsFragment(), false);
			break;

		case 4:
			UserAccessSession session = UserAccessSession
					.getInstance(getActivity());
			if (session.getUserSession() == null) {
				i = new Intent(getActivity(), LoginActivity.class/*RegisterActivity.class*/);
				getActivity().startActivity(i);
			} else {
				i = new Intent(getActivity(), ProfileActivity.class);
				getActivity().startActivity(i);
//				getActivity().finish();
			}
			break;
			
		case 5:
			main.switchContent(new AboutUsFragment(), false);
			break;

		case 6:
			main.switchContent(new TermsConditionFragment(), false);
			break;

		}

		main.getSlidingMenu().showContent();
	}

	public void updateMenuList() {

		UserAccessSession accessSession = UserAccessSession
				.getInstance(getActivity());
		UserSession userSession = accessSession.getUserSession();

		if (userSession == null)
			MENUS = UIConfig.MENUS_NOT_LOGGED;
		else
			MENUS = UIConfig.MENUS_LOGGED;

		showList();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MGUtilities.screenTracking("Menus", getActivity());
	}
	
	private void getButTicketUrl(final String url){
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
				try {
					JSONObject jsonObject = new JSONObject(buyticketres);
					String buyticketurl = jsonObject.optJSONArray("ticket").optJSONObject(0).optString("ticket_url");
					if(!buyticketurl.startsWith("http")){
						buyticketurl="http://"+buyticketurl;
					}
					Intent i = new Intent(getActivity(), BuyTicketDetailActivity.class);			
					i.putExtra("buyticketurl", buyticketurl);
					getActivity().startActivity(i);	
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				try {
					DataParser dataParser = new DataParser();
					InputStream inputStream = dataParser.retrieveStream(url);
					BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
					
					StringBuffer sb = new StringBuffer("");
					String line = "";
	
					String NL = System.getProperty("line.separator");
					while ((line = in.readLine()) != null) {
						sb.append(line + NL);
					}
					in.close();
					buyticketres = sb.toString();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		task.execute();
	}

}
