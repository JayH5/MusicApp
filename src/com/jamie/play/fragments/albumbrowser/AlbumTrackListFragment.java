package com.jamie.play.fragments.albumbrowser;

import android.os.Bundle;

import com.jamie.play.R;
import com.jamie.play.adapters.AlbumTrackListAdapter;
import com.jamie.play.adapters.abs.TrackAdapter;
import com.jamie.play.cursormanager.CursorDefinitions;
import com.jamie.play.cursormanager.CursorManager;
import com.jamie.play.fragments.TrackListFragment;

public class AlbumTrackListFragment extends TrackListFragment {

	private static final String EXTRA_ALBUM_ID = "extra_album_id";
	
	private long mAlbumId;
	
	public static AlbumTrackListFragment newInstance(long albumId) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ALBUM_ID, albumId);
		
		AlbumTrackListFragment frag = new AlbumTrackListFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAlbumId = getArguments().getLong(EXTRA_ALBUM_ID);
				
		final TrackAdapter adapter = new AlbumTrackListAdapter(getActivity(), 
				R.layout.list_item_track, null, 0);
		
		setListAdapter(adapter);
		
		final CursorManager cm = new CursorManager(getActivity(), adapter, 
				CursorDefinitions.getAlbumBrowserCursorParams(mAlbumId));
		
		getLoaderManager().initLoader(0, null, cm);
	}
}
