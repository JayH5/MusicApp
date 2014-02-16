package za.jamie.soundstage.fragments;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.List;

import za.jamie.soundstage.adapters.interfaces.TrackListAdapter;
import za.jamie.soundstage.models.Track;

public class TrackListFragment extends MusicListFragment {
	
	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (adapter != null && !(adapter instanceof TrackListAdapter)) {
			throw new IllegalArgumentException("TrackListFragments must have TrackAdapter!");
		}
		super.setListAdapter(adapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		List<Track> trackList = ((TrackListAdapter) getListAdapter()).getTrackList();
		getMusicConnection().open(trackList, position);
		showPlayer();
    }
	
	public void shuffleAll() {
		List<Track> trackList = ((TrackListAdapter) getListAdapter()).getTrackList();
		getMusicConnection().shuffle(trackList);
		showPlayer();
	}

}
