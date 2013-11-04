package za.jamie.soundstage.fragments.playlistbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.PlaylistTrackAdapter;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import android.os.Bundle;

public class PlaylistTrackListFragment extends TrackListFragment {

	//private static final String TAG = "TrackListFragment";
	private static final String EXTRA_PLAYLIST_ID = "extra_playlist_id";
	
	private PlaylistTrackAdapter mAdapter;
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
		
		mAdapter = new PlaylistTrackAdapter(getActivity(), 
				R.layout.list_item_two_line, null, 0);
		
		setListAdapter(mAdapter);
		
		mPlaylistId = getArguments().getLong(EXTRA_PLAYLIST_ID);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final CursorManager cm = new CursorManager(getActivity(), mAdapter, 
				MusicStore.Tracks.getPlaylistTracks(mPlaylistId));
		
		getLoaderManager().initLoader(0, null, cm);
	}
}
