package com.stickyheader;

import com.models.Menu;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class MGStickyHeaderAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Menu[] menu;
    private Menu[] menuHeader;
    private LayoutInflater inflater;

    public MGStickyHeaderAdapter(Context context, Menu[] menu, Menu[] menuHeader) {
        inflater = LayoutInflater.from(context);
        this.menu = menu;
        this.menuHeader = menuHeader;
    }

    @Override
    public int getCount() {
        return menu.length;
    }

    @Override
    public Object getItem(int position) {
        return menu[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override 
    public View getView(int position, View convertView, ViewGroup parent) {
    	
//        ViewHolder holder;
//        if (convertView == null) {
//            holder = new ViewHolder();
//            convertView = inflater.inflate(R.layout.test_list_item_layout, parent, false);
//            holder.text = (TextView) convertView.findViewById(R.id.text);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        holder.text.setText(countries[position]);
    	
    	if(mCallback != null)
			mCallback.OnMGStickyListHeadersAdapterViewCreated(
					this, inflater, convertView, position, parent);

        return convertView;
    }

    @Override 
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
//        HeaderViewHolder holder;
//        if (convertView == null) {
//            holder = new HeaderViewHolder();
//            convertView = inflater.inflate(R.layout.header, parent, false);
//            holder.text = (TextView) convertView.findViewById(R.id.text);
//            convertView.setTag(holder);
//        } else {
//            holder = (HeaderViewHolder) convertView.getTag();
//        }
//        //set header text as first char in name
//        String headerText = "" + countries[position].subSequence(0, 1).charAt(0);
//        holder.text.setText(headerText);
    	
    	if(mCallback != null)
			mCallback.OnMGStickyListHeadersAdapterHeaderViewCreated(
					this, inflater, convertView, position, parent);
    	
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
        return position;
    }
    
    
    OnMGStickyListHeadersAdapter mCallback;
	
	public interface OnMGStickyListHeadersAdapter {
		
        public void OnMGStickyListHeadersAdapterViewCreated(MGStickyHeaderAdapter 
        		adapter, LayoutInflater inflater, View v, int position, ViewGroup viewGroup);
        
        public void OnMGStickyListHeadersAdapterHeaderViewCreated(MGStickyHeaderAdapter 
        		adapter, LayoutInflater inflater, View v, int position, ViewGroup viewGroup);
    }
	
	public void setOnMGStickyListHeadersAdapter(OnMGStickyListHeadersAdapter listener) {
		try {
            mCallback = (OnMGStickyListHeadersAdapter) listener;
        } catch (ClassCastException e)  {
            throw new ClassCastException(this.toString() + " must implement OnMGStickyListHeadersAdapter");
        }
	}
}