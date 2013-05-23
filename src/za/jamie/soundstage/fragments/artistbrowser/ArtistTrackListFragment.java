package za.jamie.soundstage.fragments.artistbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.ArtistTrackListAdapter;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.fragments.TrackListFragment;
import android.app.Activity;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.MediaStore;

public class ArtistTrackListFragment extends TrackListFragment {
	
	private static final String EXTRA_ARTIST_ID = "extra_artist_id";
	
	private long mArtistId;
	
	private TrackAdapter mAdapter;
	
	private ArtistTrackListListener mCallback;
	
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
		
		mAdapter = new ArtistTrackListAdapter(getActivity(), 
				R.layout.list_item_two_line, null, 0);
		
		mAdapter.registerDataSetObserver(mDataSetObserver);
		
		setListAdapter(mAdapter);
		
		final CursorManager cm = new CursorManager(getActivity(), mAdapter, 
				CursorDefinitions.getArtistBrowserCursorParams(mArtistId));
		
		getLoaderManager().initLoader(0, null, cm);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (ArtistTrackListListener) activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
	}
	
	private long calculateDuration() {
		final Cursor cursor = mAdapter.getCursor();
		if (cursor != null && cursor.moveToFirst()) {
			long duration = 0;
			int durationColIdx = cursor.getColumnIndexOrThrow(
					MediaStore.Audio.Media.DURATION);
			
			do {
				duration += cursor.getLong(durationColIdx);
			} while (cursor.moveToNext());
			
			return duration;
		}
		return -1;
	}
	
	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			if (mCallback != null) {
				mCallback.onDurationCalculated(calculateDuration());
			}
		}
	};
	
	public interface ArtistTrackListListener {
		public void onDurationCalculated(long duration);
	}
}
