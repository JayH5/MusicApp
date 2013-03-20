package za.jamie.soundstage.loaders;

import java.util.LinkedList;
import java.util.List;

import za.jamie.soundstage.models.Artist;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class ArtistsLoader extends WrappedAsyncTaskLoader<List<Artist>> {

	private static final Uri BASE_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	
	private static final String[] PROJECTION = new String[] {
		MediaStore.Audio.Media.ARTIST,
		MediaStore.Audio.Media.ARTIST_ID,
		MediaStore.Audio.Media.ALBUM_ID,
		MediaStore.Audio.Media.ARTIST_KEY
	};
	
	private static final String SELECTION = MediaStore.Audio.Media.IS_MUSIC + "=?";
	
	private static final String[] SELECTION_ARGS = new String[] { "1" };
	
	private static final String SORT_ORDER = MediaStore.Audio.Media.ARTIST_KEY + " ASC, "
			+ MediaStore.Audio.Media.ALBUM_ID + " ASC";
	
	
	
	public ArtistsLoader(Context context) {
		super(context);
	}

	@Override
	public List<Artist> loadInBackground() {
		Cursor cursor = getContext().getContentResolver().query(
				BASE_URI, 
				PROJECTION, 
				SELECTION, 
				SELECTION_ARGS, 
				SORT_ORDER);
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				List<Artist> artistList = new LinkedList<Artist>();				
				
				String artist = cursor.getString(0);
				long artistId = cursor.getLong(1);
				long albumId = cursor.getLong(2);
				int numTracks = 1;
				int numAlbums = 1;
				
				while (cursor.moveToNext()) {
					long newArtistId = cursor.getLong(1);
					// If onto next artist
					if (newArtistId != artistId) {
						// Save this artist
						artistList.add(new Artist(artist, artistId, numTracks, numAlbums));
						// Reset counters
						numTracks = 1;
						numAlbums = 1;
						// Get new artistId and albumId and artist name
						artistId = newArtistId;
						albumId = cursor.getLong(2);
						artist = cursor.getString(0);
					} else {
						long newAlbumId = cursor.getLong(2);
						if (albumId != newAlbumId) {
							numAlbums++;
							albumId = newAlbumId;
						}
						numTracks++;
					}
				}
				// Add the last artist
				artistList.add(new Artist(artist, artistId, numTracks, numAlbums));
				
				cursor.close();
				cursor = null;
				
				return artistList;
			}
		}
		
		return null;
	}

}
