package za.jamie.soundstage.fragments.albumbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.BasicTrackAdapter;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.models.Artist;
import za.jamie.soundstage.utils.TextUtils;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

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
	
	private static class AlbumTrackListAdapter extends BasicTrackAdapter {

		private int mTrackNumColIdx;
		
		public AlbumTrackListAdapter(Context context, int layout, Cursor c,
				int flags) {
			
			super(context, layout, c, flags);
		}

		@Override
		protected void getColumnIndices(Cursor cursor) {
			super.getColumnIndices(cursor);
			mTrackNumColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView titleText = (TextView) view.findViewById(R.id.title);
			TextView durationText = (TextView) view.findViewById(R.id.subtitle);
			TextView trackNumText = (TextView) view.findViewById(R.id.trackNumber);
			
			titleText.setText(cursor.getString(getTitleColIdx()));
			
			long duration = cursor.getLong(getDurationColIdx());
			durationText.setText(TextUtils.getTrackDurationText(duration));
			
			int trackNum = cursor.getInt(mTrackNumColIdx);
			trackNumText.setText(TextUtils.getTrackNumText(trackNum));
		}
		
	}
}
