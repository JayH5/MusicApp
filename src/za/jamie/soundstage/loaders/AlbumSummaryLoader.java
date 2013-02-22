package za.jamie.soundstage.loaders;

import java.util.LinkedList;
import java.util.List;

import za.jamie.soundstage.models.AlbumSummary;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;


public class AlbumSummaryLoader extends WrappedAsyncTaskLoader<AlbumSummary> {

	private final static Uri BASE_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	
	private final static String[] PROJECTION = new String[] {
		MediaStore.Audio.Media.ALBUM_ID,
		MediaStore.Audio.Media.DURATION,
		MediaStore.Audio.Media.YEAR,
		MediaStore.Audio.Media.ARTIST,
		MediaStore.Audio.Media.ARTIST_ID,
		MediaStore.Audio.Media.ARTIST_KEY
	};
	
	private final static String SELECTION = MediaStore.Audio.Media.ALBUM_ID + "=?";
	
	private final static String SORT_ORDER = MediaStore.Audio.Media.ARTIST_KEY + " ASC";
	
	private final String[] mSelectionArgs;
	
	public AlbumSummaryLoader(Context context, long albumId) {
		super(context);
		mSelectionArgs = new String[] { String.valueOf(albumId) };
	}

	@Override
	public AlbumSummary loadInBackground() {
		final Cursor cursor = getContext().getContentResolver().query(
				BASE_URI, 
				PROJECTION, 
				SELECTION, 
				mSelectionArgs, 
				SORT_ORDER);
		
		AlbumSummary summary = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				long duration = 0;
				int firstYear = Integer.MAX_VALUE;
				int lastYear = 0;
				final List<String> artists = new LinkedList<String>();
				final List<Long> artistIds = new LinkedList<Long>();
				
				long artistIdStore = -1;
				do {
					// Add up the duration
					duration += cursor.getLong(1);
					
					// Get the first/last year
					final int year = cursor.getInt(2);
					firstYear = Math.min(year, firstYear);
					lastYear = Math.max(year, lastYear);
								
					final long artistId = cursor.getLong(4);
					if (artistId != artistIdStore) {
						artistIdStore = artistId;
						artistIds.add(artistId);
						artists.add(cursor.getString(3));
					}
								
				} while (cursor.moveToNext());
				
				
				summary = new AlbumSummary(cursor.getCount(), duration, firstYear, 
						lastYear, artists, artistIds);
			}
			cursor.close();
		}
		return summary;
	}
}
