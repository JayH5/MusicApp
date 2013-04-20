package za.jamie.soundstage.fragments.playlistbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.PlaylistTrackAdapter;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.fragments.TrackListFragment;
import android.os.Bundle;

public class PlaylistTrackListFragment extends TrackListFragment {

	private static final String EXTRA_PLAYLIST_ID = "extra_playlist_id";
	
	private long mPlaylistId;
	
	public static PlaylistTrackListFragment newInstance(long playlistId) {		
		final Bundle args = new Bundle();
		args.putLong(EXTRA_PLAYLIST_ID, playlistId);
		
		PlaylistTrackListFragment frag = new PlaylistTrackListFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPlaylistId = getArguments().getLong(EXTRA_PLAYLIST_ID);
		
		TrackAdapter adapter = new PlaylistTrackAdapter(getActivity(), 
				R.layout.list_item_two_line, null, 0);
		
		setListAdapter(adapter);
		
		CursorManager cm = new CursorManager(getActivity(), adapter, 
				CursorDefinitions.getPlaylistCursorParams(mPlaylistId));
		
		getLoaderManager().initLoader(0, null, cm);
	}
}
