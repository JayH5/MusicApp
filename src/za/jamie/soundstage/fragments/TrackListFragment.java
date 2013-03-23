package za.jamie.soundstage.fragments;

import java.util.List;

import za.jamie.soundstage.MusicLibraryWrapper;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import za.jamie.soundstage.models.Track;
import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;


public class TrackListFragment extends ListFragment {
	
	private MusicLibraryWrapper mCallback;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mCallback = (MusicLibraryWrapper) activity;
	}
	
	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (!(adapter instanceof TrackAdapter)) {
			throw new IllegalArgumentException("TrackListFragments must have TrackAdapters!");
		} else {
			super.setListAdapter(adapter);
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, final int position, long id) {
		List<Track> trackList = ((TrackAdapter) getListAdapter()).getTrackList();
		mCallback.open(trackList, position);
    }

}
