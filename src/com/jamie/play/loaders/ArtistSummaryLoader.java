package com.jamie.play.loaders;

import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jamie.play.models.ArtistSummary;

public class ArtistSummaryLoader extends WrappedAsyncTaskLoader<ArtistSummary> {
	
	private final static Uri BASE_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	
	private final static String[] PROJECTION = new String[] {
		MediaStore.Audio.Media.ARTIST_ID,
		MediaStore.Audio.Media.DURATION,
		MediaStore.Audio.Media.ALBUM_ID
	};
	
	private final static String SELECTION = MediaStore.Audio.Media.ARTIST_ID + "=?";
	
	private final String[] mSelectionArgs;
	
	public ArtistSummaryLoader(Context context, long artistId) {
		super(context);
		mSelectionArgs = new String[] { String.valueOf(artistId) };
	}

	@Override
	public ArtistSummary loadInBackground() {
		Cursor cursor = getContext().getContentResolver().query(
				BASE_URI, 
				PROJECTION, 
				SELECTION, 
				mSelectionArgs, 
				null);
		
		ArtistSummary summary = null;
		if (cursor != null) {			
			if (cursor.moveToFirst()) {
				long duration = 0;
				final Set<Long> albumSet = new TreeSet<Long>();
				
				do {
					// Add up the duration
					duration += cursor.getLong(1);
					
					// Add album to set
					albumSet.add(cursor.getLong(2));								
				} while (cursor.moveToNext());
				
				
				summary = new ArtistSummary(cursor.getCount(), albumSet.size(), duration);
			}
			cursor.close();
			cursor = null;
		}		
		return summary;
	}

}
