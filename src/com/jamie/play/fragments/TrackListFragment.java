package com.jamie.play.fragments;

import java.util.List;

import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.jamie.play.adapters.TrackAdapter;
import com.jamie.play.service.MusicServiceWrapper;
import com.jamie.play.service.Track;

public class TrackListFragment extends ListFragment {
	
	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (!(adapter instanceof TrackAdapter)) {
			throw new IllegalArgumentException("TrackListFragments must have TrackAdapters!");
		} else {
			super.setListAdapter(adapter);
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
 	   	List<Track> list = ((TrackAdapter) getListAdapter()).getTrackList();
 	   	Log.d("SongList", "Track selected: " + list.get(position).getTitle());
 	   	MusicServiceWrapper.playAll(getActivity(), list, position, false);
    }

}
