package za.jamie.soundstage.fragments.artistbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.ArtistTrackListAdapter;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.fragments.TrackListFragment;
import android.os.Bundle;

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
