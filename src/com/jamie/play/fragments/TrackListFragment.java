package com.jamie.play.fragments;

import java.util.List;

import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.jamie.play.activities.MusicActivity;
import com.jamie.play.adapters.abs.TrackAdapter;
import com.jamie.play.models.Track;
import com.jamie.play.service.MusicServiceWrapper;

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
	public void onListItemClick(ListView l, View v, final int position, long id) {
		
		(new AsyncTask<TrackAdapter, Void, List<Track>>() {

			@Override
			protected List<Track> doInBackground(TrackAdapter... params) {
				final TrackAdapter adapter = params[0];
				if (adapter != null) {
					List<Track> trackList = adapter.getTrackList();
					MusicServiceWrapper.playAll(getActivity(), trackList, position, false);
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(List<Track> result) {
				
			}
 	   		
 	   	}).execute(((TrackAdapter) getListAdapter()));
		
		//final List<Track> trackList = ((TrackAdapter) getListAdapter()).getTrackList();
		//MusicServiceWrapper2.playAll(getActivity(), trackList, position, false);
		
		((MusicActivity) getActivity()).getMenuDrawer().openMenu();
    }

}
