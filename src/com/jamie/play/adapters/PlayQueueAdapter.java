package com.jamie.play.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jamie.play.R;
import com.jamie.play.service.Track;

public class PlayQueueAdapter extends BaseAdapter {

	private List<Track> mList;
	private int mResource;
	private LayoutInflater mInflater;
	
	public PlayQueueAdapter(Context context, int resource, List<Track> list) {
		mResource = resource;
		mList = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mList.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mResource);
	}
	
	 private View createViewFromResource(int position, View convertView,
	            ViewGroup parent, int resource) {
		 View v;
	     if (convertView == null) {
	         v = mInflater.inflate(resource, parent, false);
	     } else {
	    	 v = convertView;
	     }

	     bindView(position, v);

	     return v;
	 }
	 
	 private void bindView(int position, View view) {
		 final TextView titleText = (TextView) view.findViewById(R.id.title);
		 final TextView subtitleText = (TextView) view.findViewById(R.id.subtitle);
		 
		 final Track track = mList.get(position);
		 
		 titleText.setText(track.getTitle());
		 subtitleText.setText(track.getArtist());
		 
	 }

}
