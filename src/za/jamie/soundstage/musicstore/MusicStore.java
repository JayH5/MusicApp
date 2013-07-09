package za.jamie.soundstage.musicstore;

import za.jamie.soundstage.musicstore.CursorManager.CursorRequest;
import android.net.Uri;
import android.provider.MediaStore;


public final class MusicStore {
	
	public static final class Albums {
		public static final String _ID = MediaStore.Audio.Albums._ID;
		public static final Uri URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
		public static final String[] PROJECTION = new String[] {
			MediaStore.Audio.Albums._ID,
			MediaStore.Audio.Albums.ALBUM,
			MediaStore.Audio.Albums.ARTIST
		};
		public static final String SORT_ORDER = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
		
		public static final CursorRequest CURSOR = new CursorRequest(URI)
				.addProjection(PROJECTION)
				.setSortOrder(SORT_ORDER);
		
		public static CursorRequest getArtistAlbums(long artistId) {
			return new CursorRequest(MediaStore.Audio.Artists.Albums.getContentUri("external", artistId))
					.addProjection(PROJECTION)
					.addProjection(MediaStore.Audio.Albums.FIRST_YEAR,
						MediaStore.Audio.Albums.LAST_YEAR,
						MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST)
					.setSortOrder(SORT_ORDER);
		}
	}
	
	public static final class Artists {
		public static final String _ID = MediaStore.Audio.Artists._ID;
		public static final Uri URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
		public static final String[] PROJECTION = new String[] {
			MediaStore.Audio.Artists._ID,
			MediaStore.Audio.Artists.ARTIST,
			MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
			MediaStore.Audio.Artists.NUMBER_OF_TRACKS
		};
		public static final String SORT_ORDER = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;
		
		public static final CursorRequest CURSOR = new CursorRequest(URI)
				.addProjection(PROJECTION)
				.setSortOrder(SORT_ORDER);
	}
	
	public static final class Playlists {
		public static final String _ID = MediaStore.Audio.Playlists._ID;
		public static final Uri URI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
		public static final String[] PROJECTION = new String[] {
			MediaStore.Audio.Playlists._ID,
			MediaStore.Audio.Playlists.NAME
		};
		public static final String SORT_ORDER = MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER;
		public static final CursorRequest CURSOR = new CursorRequest(URI)
				.addProjection(PROJECTION)
				.setSortOrder(SORT_ORDER);
	}
	
	public static class Tracks {
		public static final String _ID = MediaStore.Audio.Media._ID;
		public static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		public static final String[] PROJECTION = new String[] {
			MediaStore.Audio.Media._ID, 
			MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ARTIST_ID,
			MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.DURATION
		};
		public static final String SELECTION = MediaStore.Audio.Media.IS_MUSIC + "=1";
		public static final String SORT_ORDER = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
		
		public static final CursorRequest CURSOR = new CursorRequest(URI)
				.addProjection(PROJECTION)
				.setSelection(SELECTION)
				.setSortOrder(SORT_ORDER);
		
		public static CursorRequest getAlbumTracks(long albumId) {
			return CURSOR.addProjection(MediaStore.Audio.Media.ARTIST_KEY, 
						MediaStore.Audio.Media.TRACK,
						MediaStore.Audio.Media.YEAR)
					.setSelection(MediaStore.Audio.Media.ALBUM_ID + "=" + albumId)
					.setSortOrder(MediaStore.Audio.Media.TRACK);
		}
		
		public static CursorRequest getArtistTracks(long artistId) {
			return CURSOR.setSelection(MediaStore.Audio.Media.ARTIST_ID + "=" + artistId)
					.setSortOrder(MediaStore.Audio.Media.TITLE);
		}
		
		public static CursorRequest getPlaylistTracks(long playlistId) {
			return new CursorRequest(MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId))
					.addProjection(PROJECTION)
					.addProjection(MediaStore.Audio.Playlists.Members.AUDIO_ID)
					.setSortOrder(MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
		}
	}
}
