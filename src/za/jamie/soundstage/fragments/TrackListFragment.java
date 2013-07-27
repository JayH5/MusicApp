package za.jamie.soundstage.fragments;

import java.util.List;

import za.jamie.soundstage.adapters.interfaces.TrackAdapter;
import za.jamie.soundstage.models.Track;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class TrackListFragment extends MusicListFragment {
	
	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (adapter != null && !(adapter instanceof TrackAdapter)) {
			throw new IllegalArgumentException("TrackListFragments must have TrackAdapter!");
		}
		super.setListAdapter(adapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		List<Track> trackList = ((TrackAdapter) getListAdapter()).getTrackList();
		getMusicConnection().open(trackList, position);
		showPlayer();
    }
	
	public void shuffleAll() {
		List<Track> trackList = ((TrackAdapter) getListAdapter()).getTrackList();
		getMusicConnection().shuffle(trackList);
		showPlayer();
	}

}
