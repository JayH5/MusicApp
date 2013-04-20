package za.jamie.soundstage.fragments.albumbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.AlbumTrackListAdapter;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.fragments.TrackListFragment;
import android.app.Activity;
import android.os.Bundle;

public class AlbumTrackListFragment extends TrackListFragment {

	private static final String EXTRA_ALBUM_ID = "extra_album_id";
	
	private long mAlbumId;
	
	private AlbumTrackListAdapter mAdapter;
	
	private AlbumTrackListAdapter.StatsCallback mCallback;
	
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
				
		mAdapter = new AlbumTrackListAdapter(getActivity(), 
				R.layout.list_item_track, null, 0);
		
		mAdapter.setCallback(mCallback);
		
		setListAdapter(mAdapter);
		
		final CursorManager cm = new CursorManager(getActivity(), mAdapter, 
				CursorDefinitions.getAlbumBrowserCursorParams(mAlbumId));
		
		getLoaderManager().initLoader(0, null, cm);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (AlbumTrackListAdapter.StatsCallback) activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
		mAdapter.setCallback(mCallback);
	}
}
