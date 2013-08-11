package za.jamie.soundstage.adapters.wrappers;

import java.util.List;

import za.jamie.soundstage.adapters.interfaces.TrackListAdapter;
import za.jamie.soundstage.models.Track;
import android.widget.ListAdapter;

public class TrackHeaderViewListAdapter extends HeaderViewListAdapter
		implements TrackListAdapter {

	private TrackListAdapter mDelegate;
	
	public TrackHeaderViewListAdapter(ListAdapter delegate) {
		super(delegate);
		try {
			mDelegate = (TrackListAdapter) delegate;
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
		return mDelegate.getTrack(position - getHeadersCount());
	}

}
