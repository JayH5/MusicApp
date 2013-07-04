package za.jamie.soundstage.adapters.wrappers;

import java.util.List;

import android.content.Context;
import android.widget.ListAdapter;
import za.jamie.soundstage.adapters.interfaces.TrackAdapter;
import za.jamie.soundstage.models.Track;

public class TrackHeaderFooterAdapterWrapper extends HeaderFooterAdapterWrapper
		implements TrackAdapter {

	private TrackAdapter mDelegate;
	
	public TrackHeaderFooterAdapterWrapper(Context context, ListAdapter delegate) {
		super(context, delegate);
		init(delegate);
	}

	public TrackHeaderFooterAdapterWrapper(Context context,
			ListAdapter delegate, int headerLayout) {
		super(context, delegate, headerLayout);
		init(delegate);
	}

	public TrackHeaderFooterAdapterWrapper(Context context,
			ListAdapter delegate, int headerLayout, int footerLayout) {
		super(context, delegate, headerLayout, footerLayout);
		init(delegate);
	}
	
	private void init(ListAdapter delegate) {
		try {
			mDelegate = (TrackAdapter) delegate;
		} catch (ClassCastException e) {
			throw new ClassCastException("Adapter must implement TrackAdapter!");
		}
	}

	@Override
	public List<Track> getTrackList() {
		return mDelegate.getTrackList();
	}

	@Override
	public Track getTrack(int position) {
		return mDelegate.getTrack(position - mNumHeaders);
	}

}
