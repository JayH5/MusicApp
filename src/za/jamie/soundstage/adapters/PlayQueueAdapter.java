package za.jamie.soundstage.adapters;

import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.models.Track;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PlayQueueAdapter extends BaseAdapter {

	private static final int TYPE_PAST = 0;
	private static final int TYPE_PRESENT = 1;
	private static final int TYPE_FUTURE = 2;
	private static final int NUM_TYPES = 3;
	
	private List<Track> mList;
	private LayoutInflater mInflater;
	
	private int mQueuePosition;
	private int mResourcePast;
	private int mResourcePresent;
	private int mResourceFuture;
	
	public PlayQueueAdapter(Context context, int resourcePast, int resourcePresent, 
			int resourceFuture, List<Track> list, int position) {
	
		mList = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mResourcePast = resourcePast;
		mResourcePresent = resourcePresent;
		mResourceFuture = resourceFuture;
		mQueuePosition = position;
	}
	
	public void setQueuePosition(int position) {
		mQueuePosition = position;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if (mList != null) {
			return mList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mList != null) {
			return mList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (mList != null) {
			return mList.get(position).getId();
		}
		return 0;
	}
	
	@Override
	public int getItemViewType(int position) {
		int type;
		if (mQueuePosition >= 0) {
			if (position < mQueuePosition) {
				type = TYPE_PAST;
			} else if (position == mQueuePosition) {
				type = TYPE_PRESENT;
			} else {
				type = TYPE_FUTURE;
			}
		} else {
			type = TYPE_FUTURE;
		}
		return type;
	}
	
	@Override
	public int getViewTypeCount() {
		return NUM_TYPES;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View view;
		if (convertView == null) {
	    	final int type = getItemViewType(position);
	    	int resource;
	    	switch (type) {
	    	case TYPE_PAST:
	    		resource = mResourcePast;
	    		break;
	    	case TYPE_PRESENT:
	    		resource = mResourcePresent;
	    		break;
	    	case TYPE_FUTURE:
	    		resource = mResourceFuture;
	    		break;
	    	default:
	    		resource = mResourceFuture;
	    		break;
	    	}
	        view = mInflater.inflate(resource, parent, false);
	    } else {
	    	view = convertView;
	    }
	    
	    bindView(position, view);
	    return view;
	}
	
	public void setList(List<Track> list) {
		mList = list;
		if (list != null) {
			notifyDataSetChanged();
		} else {
			notifyDataSetInvalidated();
		}
	}
	
	public List<Track> getList() {
		return mList;
	}
	
	public void remove(int position) {
		mList.remove(position);
		notifyDataSetChanged();
	}
	
	public void moveQueueItem(int from, int to) {
		mList.add(to, mList.remove(from));
		notifyDataSetChanged();
	}
	 
	private void bindView(int position, View view) {
		final TextView titleText = (TextView) view.findViewById(R.id.title);
		final TextView subtitleText = (TextView) view.findViewById(R.id.subtitle);
		 
		final Track track = (Track) getItem(position);
		if (track != null) {
			titleText.setText(track.getTitle());
			subtitleText.setText(track.getArtist());
		}		 
	}

}
