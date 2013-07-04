package za.jamie.soundstage.adapters.wrappers;

import java.util.List;

import za.jamie.soundstage.adapters.interfaces.TrackAdapter;
import za.jamie.soundstage.models.Track;
import android.content.Context;
import android.widget.ListAdapter;

public class TrackSectionedHFAdapterWrapper extends SectionedHeaderFooterAdapterWrapper
		implements TrackAdapter {

	private TrackAdapter mDelegate;
	
	public TrackSectionedHFAdapterWrapper(Context context, ListAdapter delegate) {
		super(context, delegate);
		init(delegate);
	}

	public TrackSectionedHFAdapterWrapper(Context context,
			ListAdapter delegate, int headerLayout) {
		super(context, delegate, headerLayout);
		init(delegate);
	}

	public TrackSectionedHFAdapterWrapper(Context context,
			ListAdapter delegate, int headerLayout, int footerLayout) {
		super(context, delegate, headerLayout, footerLayout);
		init(delegate);
	}
	
	private void init(ListAdapter adapter) {
		try {
			mDelegate = (TrackAdapter) adapter;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					"Adapter must implement TrackAdapter!");
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
