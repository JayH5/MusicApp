package za.jamie.soundstage.adapters;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.models.Artist;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

public class AlbumTrackListAdapter extends TrackAdapter {

	private int mTrackNumColIdx;
	private StatsCallback mCallback;
	
	private final Context mContext;
	
	public AlbumTrackListAdapter(Context context, int layout, Cursor c,
			int flags) {
		
		super(context, layout, c, flags);
		mContext = context;
	}
	
	@Override
	protected void onCursorLoad(Cursor cursor) {
		super.onCursorLoad(cursor);
		if (mCallback != null) {
			mCallback.onStatisticsCalculated(calculateStats(cursor));
		}
	}

	@Override
	protected void getColumnIndices(Cursor cursor) {
		super.getColumnIndices(cursor);
		mTrackNumColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TextView titleText = (TextView) 
				view.findViewById(R.id.title);
		final TextView durationText = (TextView) 
				view.findViewById(R.id.subtitle);
		final TextView trackNumText = (TextView) 
				view.findViewById(R.id.trackNumber);
		
		// Set the track name
		titleText.setText(cursor.getString(getTitleColIdx()));
		
		final Resources res = mContext.getResources();
		
		// Set the duration
		long duration = cursor.getLong(getDurationColIdx());
		durationText.setText(TextUtils.getTrackDurationText(res, duration));
		
		// Set the track number
		int trackNum = cursor.getInt(mTrackNumColIdx);
		trackNumText.setText(TextUtils.getTrackNumText(res, trackNum));
	}
	
	public void setCallback(StatsCallback callback) {
		mCallback = callback;
	}
	
	/**
	 * Scans through the contents of the cursor and finds the artists, duration
	 * and number of tracks in the album.
	 */
	private AlbumStatistics calculateStats(Cursor cursor) {
		if (cursor.moveToFirst()) {			
			// Get all the column indexes we need
			final int artistKeyColIdx = cursor.getColumnIndexOrThrow(
					MediaStore.Audio.Media.ARTIST_KEY);
			final int yearColIdx = cursor.getColumnIndexOrThrow(
					MediaStore.Audio.Media.YEAR);
			final int artistIdColIdx = getArtistIdColIdx();
			final int artistColIdx = getArtistColIdx();
			final int durationColIdx = getDurationColIdx();
			
			AlbumStatistics.Builder builder = new AlbumStatistics.Builder()
					.setTitle(cursor.getString(getAlbumColIdx()))
					.setNumTracks(cursor.getCount());
			
			// Iterate through the cursor
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
	
	public interface StatsCallback {
		public void onStatisticsCalculated(AlbumStatistics stats);
	}
	
}