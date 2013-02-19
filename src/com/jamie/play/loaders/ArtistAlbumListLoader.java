package com.jamie.play.loaders;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jamie.play.models.ArtistAlbum;

public class ArtistAlbumListLoader extends WrappedAsyncTaskLoader<List<ArtistAlbum>> {

	private static final Uri BASE_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	
	private static final String[] PROJECTION = new String[] {
		MediaStore.Audio.Media.ARTIST_ID,
		MediaStore.Audio.Media.ALBUM,
		MediaStore.Audio.Media.ALBUM_ID,
		MediaStore.Audio.Media.YEAR
	};
	
	private static final String SELECTION = MediaStore.Audio.Media.ARTIST_ID + "=?";
	
	private final String[] mSelectionArgs;
	
	private static final String SORT_ORDER = MediaStore.Audio.Media.ALBUM_KEY + " ASC";
	
	public ArtistAlbumListLoader(Context context, long artistId) {
		super(context);
		mSelectionArgs = new String[] { String.valueOf(artistId) };
	}

	@Override
	public List<ArtistAlbum> loadInBackground() {
		final Cursor cursor = getContext().getContentResolver().query(
				BASE_URI, 
				PROJECTION, 
				SELECTION, 
				mSelectionArgs, 
				SORT_ORDER);
		
		final List<ArtistAlbum> artistAlbums = new LinkedList<ArtistAlbum>();
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int firstYear = Integer.MAX_VALUE;
				int lastYear = 0;
				int numTracks = 0;
				
				long albumIdStored = cursor.getLong(2);
				String albumStored = cursor.getString(1);
				do {
					final long albumId = cursor.getLong(2);
					if (albumId != albumIdStored) {
						// Save the album
						artistAlbums.add(new ArtistAlbum(
								albumStored,
								albumIdStored,
								numTracks,
								firstYear,
								lastYear));
						
						// New stored id and name
						albumIdStored = albumId;
						albumStored = cursor.getString(1);
						
						// Reset track count
						numTracks = 0;
						
						// Reset year measures
						firstYear = Integer.MAX_VALUE;
						lastYear = 0;						
					}
					
					numTracks++;
					
					// Get the first/last year
					final int year = cursor.getInt(3);
					firstYear = Math.min(year, firstYear);
					lastYear = Math.max(year, lastYear);
					
					
				} while (cursor.moveToNext());
				
				// Add the last album
				artistAlbums.add(new ArtistAlbum(
						albumStored,
						albumIdStored,
						numTracks,
						firstYear,
						lastYear));
			}
			cursor.close();
		}
		
		return artistAlbums;
	}

}
