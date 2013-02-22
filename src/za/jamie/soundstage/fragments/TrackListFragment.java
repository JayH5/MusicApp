package za.jamie.soundstage.fragments;

import java.util.List;

import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.MusicServiceWrapper;

import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;


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
