package za.jamie.soundstage.fragments.albumbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.AlbumTrackListAdapter;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.models.Artist;
import android.app.Activity;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.MediaStore;

public class AlbumTrackListFragment extends TrackListFragment {

	private static final String EXTRA_ALBUM_ID = "extra_album_id";
	
	private long mAlbumId;
	
	private AlbumTrackListAdapter mAdapter;
	
	private AlbumStatisticsCallback mCallback;
	
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
		
		mAdapter.registerDataSetObserver(mDataSetObserver);
		
		setListAdapter(mAdapter);
		
		final CursorManager cm = new CursorManager(getActivity(), mAdapter, 
				CursorDefinitions.getAlbumBrowserCursorParams(mAlbumId));
		
		getLoaderManager().initLoader(0, null, cm);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (AlbumStatisticsCallback) activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
	}
	
	public AlbumStatistics getAlbumStatistics() {
		final Cursor cursor = mAdapter.getCursor();
		if (cursor != null && cursor.moveToFirst()) {			
			// Get all the column indexes we need
			int artistKeyColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_KEY);
			int yearColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR);
			int artistIdColIdx = mAdapter.getArtistIdColIdx();
			int artistColIdx = mAdapter.getArtistColIdx();
			int durationColIdx = mAdapter.getDurationColIdx();
			
			// Create a stats builder, add album title and number of tracks
			AlbumStatistics.Builder builder = new AlbumStatistics.Builder()
					.setTitle(cursor.getString(mAdapter.getAlbumColIdx()))
					.setNumTracks(cursor.getCount());
			
			// Iterate through the cursor collecting artists, duration and year
			do {
				Artist artist = new Artist(
						cursor.getString(artistKeyColIdx), // Key
						cursor.getLong(artistIdColIdx),  // Id
						cursor.getString(artistColIdx)); // Title
				
				builder.addArtist(artist)
						.addDuration(cursor.getLong(durationColIdx))
						.addYear(cursor.getInt(yearColIdx));
				
			} while (cursor.moveToNext());
			
			return builder.create();
		}
		return null;
	}
	
	public interface AlbumStatisticsCallback {
		public void deliverAlbumStatistics(AlbumStatistics stats);
	}
	
	private DataSetObserver mDataSetObserver = new DataSetObserver() {		
		@Override
		public void onChanged() {
			if (mCallback != null) {
				mCallback.deliverAlbumStatistics(getAlbumStatistics());
			}
		}
	};
}
