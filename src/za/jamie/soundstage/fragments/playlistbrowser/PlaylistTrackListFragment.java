package za.jamie.soundstage.fragments.playlistbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.PlaylistTrackAdapter;
import za.jamie.soundstage.adapters.abs.BasicTrackAdapter;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import za.jamie.soundstage.musicstore.CursorManager.CursorRequest;
import android.os.Bundle;

public class PlaylistTrackListFragment extends TrackListFragment {

	private static final String EXTRA_PLAYLIST_ID = "extra_playlist_id";
	
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
		
		BasicTrackAdapter adapter = new PlaylistTrackAdapter(getActivity(), 
				R.layout.list_item_two_line, null, 0);
		
		setListAdapter(adapter);
		
		long playlistId = getArguments().getLong(EXTRA_PLAYLIST_ID);
		
		CursorRequest cr = MusicStore.Tracks.getPlaylistTracks(playlistId);
		CursorManager cm = new CursorManager(getActivity(), adapter, cr);
		
		getLoaderManager().initLoader(0, null, cm);
	}
}
