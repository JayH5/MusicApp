package za.jamie.soundstage.cursormanager;

import za.jamie.soundstage.cursormanager.CursorManager.CursorParams;
import android.provider.MediaStore;


public final class CursorDefinitions {
	
	/**
	 * 
	 * @return the cursor parameter for the album grid view
	 */
	public static CursorParams getAlbumsCursorParams() { 
		return new CursorParams()
	 		.setBaseUri(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)
	 		.setProjection(new String[] {
	 				MediaStore.Audio.Albums._ID,
	 				MediaStore.Audio.Albums.ALBUM,
	 				MediaStore.Audio.Albums.ARTIST
	 			})
	 		.setSortOrder(MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
	}
	
	/**
	 * 
	 * @param albumId The album ID of the album
	 * @return the cursor parameter for the album detail view
	 */
	public static CursorParams getAlbumBrowserCursorParams(long albumId) { 
		return new CursorParams()
			.setBaseUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
			.setProjection(new String[] {
					MediaStore.Audio.Media._ID, 
					MediaStore.Audio.Media.TITLE,
					MediaStore.Audio.Media.ARTIST_ID,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media.ALBUM_ID,
					MediaStore.Audio.Media.ALBUM,
					MediaStore.Audio.Media.DURATION,
					MediaStore.Audio.Media.TRACK,
					MediaStore.Audio.Media.YEAR
				})
			.setSelection(MediaStore.Audio.Media.ALBUM_ID + " = " + albumId)
			.setSortOrder(MediaStore.Audio.Media.TRACK + " ASC");
	}
	
	public static CursorParams getArtistBrowserCursorParams(long artistId) {
		return new CursorParams()
			.setBaseUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
			.setProjection(new String[] {
					MediaStore.Audio.Media._ID, 
					MediaStore.Audio.Media.TITLE,
					MediaStore.Audio.Media.ARTIST_ID,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media.ALBUM_ID,
					MediaStore.Audio.Media.ALBUM,
					MediaStore.Audio.Media.DURATION
				})
			.setSelection(MediaStore.Audio.Media.ARTIST_ID + " = " + artistId)
			.setSortOrder(MediaStore.Audio.Media.TITLE + " ASC");
	}
	
	public static CursorParams getSongsCursorParams() {
		return new CursorParams()
				.setBaseUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
				.setProjection(new String[] {
						MediaStore.Audio.Media._ID, 
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.ARTIST_ID,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.ALBUM_ID,
						MediaStore.Audio.Media.ALBUM,
						MediaStore.Audio.Media.DURATION
					})
				.setSelection(MediaStore.Audio.Media.IS_MUSIC + "=1")
				.setSortOrder(MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
	}
	
	public static CursorParams getArtistsCursorParams() {
		return new CursorParams()
				.setBaseUri(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI)
				.setProjection(new String[] {
						MediaStore.Audio.Artists._ID,
						MediaStore.Audio.Artists.ARTIST,
						MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
						MediaStore.Audio.Artists.NUMBER_OF_TRACKS
					})
				.setSortOrder(MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
	}
	
	public static CursorParams getArtistAlbumsCursorParams(String artist) {
		return new CursorParams()
				.setBaseUri(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)
				.setProjection(new String[] {
						MediaStore.Audio.Albums._ID,
						MediaStore.Audio.Albums.ALBUM,
						MediaStore.Audio.Albums.ARTIST,
						MediaStore.Audio.Albums.NUMBER_OF_SONGS,
						MediaStore.Audio.Albums.FIRST_YEAR,
						MediaStore.Audio.Albums.LAST_YEAR
					})
				.setSelection(MediaStore.Audio.Albums.ARTIST + " = \"" + artist + "\"")
				.setSortOrder(MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
	}
}