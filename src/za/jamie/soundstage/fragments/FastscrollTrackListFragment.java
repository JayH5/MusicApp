package za.jamie.soundstage.fragments;

import java.util.List;

import za.jamie.soundstage.adapters.abs.BasicTrackAdapter;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.connections.MusicLibraryConnection;
import android.app.Activity;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class FastscrollTrackListFragment extends FastscrollListFragment {

	private MusicLibraryConnection mCallback;
	private BasicTrackAdapter mAdapter;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (MusicLibraryConnection) activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
	}
	
	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (adapter != null && !(adapter instanceof BasicTrackAdapter)) {
			throw new IllegalArgumentException("TrackListFragments must have TrackAdapter!");
		} else {
			super.setListAdapter(adapter);
			mAdapter = (BasicTrackAdapter) adapter;
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, final int position, long id) {
		if (mCallback != null) {
			List<Track> trackList = mAdapter.getTrackList();
			mCallback.open(trackList, position);
		}
    }
	
	public void shuffleAll() {
		if (mCallback != null) {
			List<Track> trackList = mAdapter.getTrackList();
			mCallback.shuffle(trackList);
		}
	}

}
