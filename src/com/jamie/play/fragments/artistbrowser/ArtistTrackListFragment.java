package com.jamie.play.fragments.artistbrowser;

import android.os.Bundle;

import com.jamie.play.R;
import com.jamie.play.adapters.ArtistTrackListAdapter;
import com.jamie.play.adapters.abs.TrackAdapter;
import com.jamie.play.cursormanager.CursorDefinitions;
import com.jamie.play.cursormanager.CursorManager;
import com.jamie.play.fragments.TrackListFragment;

public class ArtistTrackListFragment extends TrackListFragment {
	
	private static final String EXTRA_ARTIST_ID = "extra_artist_id";
	
	private long mArtistId;
	
	public static ArtistTrackListFragment newInstance(long artistId) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ARTIST_ID, artistId);
		
		final ArtistTrackListFragment frag = new ArtistTrackListFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mArtistId = getArguments().getLong(EXTRA_ARTIST_ID);
		
		final TrackAdapter adapter = new ArtistTrackListAdapter(getActivity(), 
				R.layout.list_item_two_line, null, 0);
		
		setListAdapter(adapter);
		
		final CursorManager cm = new CursorManager(getActivity(), adapter, 
				CursorDefinitions.getArtistBrowserCursorParams(mArtistId));
		
		getLoaderManager().initLoader(0, null, cm);
	}
}
