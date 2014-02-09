package za.jamie.soundstage.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.ResourceArrayAdapter;
import za.jamie.soundstage.models.Track;

public class PlayQueueAdapter extends ResourceArrayAdapter<Track> {

	private int mQueuePosition = -1;
	
	private final LayoutInflater mInflater;
	private int mSelectedLayout;
	
	public PlayQueueAdapter(Context context, int layout, int selectedLayout, 
			List<Track> objects) {
		super(context, layout, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mSelectedLayout = selectedLayout;
	}
	
	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	public void bindView(View view, Context context, Track object) {
		final TextView titleText = (TextView) view.findViewById(R.id.title);
		final TextView subtitleText = (TextView) view.findViewById(R.id.subtitle);
		
		titleText.setText(object.getTitle());
		subtitleText.setText(object.getArtist());
	}
	
	public void setQueuePosition(int position) {
		mQueuePosition = position;
        notifyDataSetChanged();
	}
	
	public int getQueuePosition() {
		return mQueuePosition;
	}
	
	@Override
	public View newView(Context context, int type, ViewGroup parent) {
		if (type == 1) {
			return mInflater.inflate(mSelectedLayout, parent, false);
		} else {
			return super.newView(context, type, parent);
		}
	}
	
	@Override
	public int getItemViewType(int position) {
		return position == mQueuePosition ? 1 : 0;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public void move(int from, int to) {
		if (from == mQueuePosition) {
			mQueuePosition = to;
		} else if (from < mQueuePosition && to >= mQueuePosition) {
			mQueuePosition--;
		} else if (from > mQueuePosition && to <= mQueuePosition) {
			mQueuePosition++;
		}
		super.move(from, to);
	}

}
