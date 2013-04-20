package za.jamie.soundstage.cursormanager;

import za.jamie.soundstage.cursormanager.CursorManager.CursorParams;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;


public final class CursorDefinitions {
	
	private static final Uri TRACKS_BASE_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	private static final String[] TRACKS_PROJECTION = new String[] {
		MediaStore.Audio.Media._ID, 
		MediaStore.Audio.Media.TITLE,
		MediaStore.Audio.Media.ARTIST_ID,
		MediaStore.Audio.Media.ARTIST,
		MediaStore.Audio.Media.ALBUM_ID,
		MediaStore.Audio.Media.ALBUM,
		MediaStore.Audio.Media.DURATION
	};
	
	private static final Uri ARTISTS_BASE_URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;	
	private static final String[] ARTISTS_PROJECTION = new String[] {
		MediaStore.Audio.Artists._ID,
		MediaStore.Audio.Artists.ARTIST
	};
	
	private static final Uri ALBUMS_BASE_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
	private static final String[] ALBUMS_PROJECTION = new String[] {
		MediaStore.Audio.Albums._ID,
		MediaStore.Audio.Albums.ALBUM
	};
	
	private static final Uri PLAYLISTS_BASE_URI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
	private static final String[] PLAYLISTS_PROJECTION = new String[] {
		MediaStore.Audio.Playlists._ID,
		MediaStore.Audio.Playlists.NAME
	};
	
	public static final String SELECTION_IS_MUSIC = MediaStore.Audio.Media.IS_MUSIC + "=1";
	
	/**
	 * 
	 * @return the cursor parameter for the album grid view
	 */
	public static CursorParams getAlbumsCursorParams() { 
		return new CursorParams(ALBUMS_BASE_URI)
	 		.addProjection(ALBUMS_PROJECTION)
	 		.addProjection(MediaStore.Audio.Albums.ARTIST)
	 		.setSortOrder(MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);	 		
	}
	
	/**
	 * 
	 * @param albumId The album ID of the album
	 * @return the cursor parameter for the album detail view
	 */
	public static CursorParams getAlbumBrowserCursorParams(long albumId) { 
		return new CursorParams(TRACKS_BASE_URI)
				.addProjection(TRACKS_PROJECTION)
				.addProjection(MediaStore.Audio.Media.TRACK)
				.addProjection(MediaStore.Audio.Media.ARTIST_KEY)
				.addProjection(MediaStore.Audio.Media.YEAR)
				.setSelection(MediaStore.Audio.Media.ALBUM_ID + " = " + albumId)
				.setSortOrder(MediaStore.Audio.Media.TRACK + " ASC");
	}
	
	public static CursorParams getArtistBrowserCursorParams(long artistId) {
		return new CursorParams(TRACKS_BASE_URI)
				.addProjection(TRACKS_PROJECTION)
				.setSelection(MediaStore.Audio.Media.ARTIST_ID + " = " + artistId)
				.setSortOrder(MediaStore.Audio.Media.TITLE + " ASC");
	}
	
	public static CursorParams getSongsCursorParams() {
		return new CursorParams(TRACKS_BASE_URI)
				.addProjection(TRACKS_PROJECTION)
				.setSelection(SELECTION_IS_MUSIC)
				.setSortOrder(MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
	}
	
	public static CursorParams getArtistsCursorParams() {
		return new CursorParams(ARTISTS_BASE_URI)
				.addProjection(ARTISTS_PROJECTION)
				.addProjection(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
				.addProjection(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
				.setSortOrder(MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
	}
	
	public static CursorParams getPlaylistsCursorParams() {
		return new CursorParams(PLAYLISTS_BASE_URI)
				.addProjection(PLAYLISTS_PROJECTION)
				.setSortOrder(MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
	}
	
	public static CursorParams getPlaylistCursorParams(long id) {
		return new CursorParams(MediaStore.Audio.Playlists.Members.getContentUri("external", id))
				.addProjection(TRACKS_PROJECTION)
				.addProjection(MediaStore.Audio.Playlists.Members.AUDIO_ID)
				.setSortOrder(MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
	}
	
	public static CursorParams getArtistAlbumsCursorParams(long artistId) {
		return new CursorParams(MediaStore.Audio.Artists.Albums.getContentUri("external", artistId))
				.addProjection(ALBUMS_PROJECTION)
				.addProjection(MediaStore.Audio.Albums.ARTIST)
				.addProjection(MediaStore.Audio.Albums.FIRST_YEAR)
				.addProjection(MediaStore.Audio.Albums.LAST_YEAR)
				.addProjection(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
				.addProjection(MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST)
				.setSortOrder(MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
	}
	
	public static CursorParams getArtistSummaryCursorParams(long artistId) {
		return new CursorParams(ContentUris.withAppendedId(ARTISTS_BASE_URI, artistId))
				.addProjection(ARTISTS_PROJECTION)
				.addProjection(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
				.addProjection(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
	}
}
