package za.jamie.soundstage.musicstore;

import android.net.Uri;
import android.provider.MediaStore;


public final class MusicStore {
	
	public static final class Albums {
		public static final Uri URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
		public static final String[] PROJECTION = new String[] {
			MediaStore.Audio.Albums._ID,
			MediaStore.Audio.Albums.ALBUM,
			MediaStore.Audio.Albums.ARTIST,
			MediaStore.Audio.Albums.ALBUM_ART
		};
		public static final String SORT_ORDER = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
		
		public static final CursorRequest REQUEST = new CursorRequest(URI,
				PROJECTION, null, null, SORT_ORDER);
		
		public static CursorRequest getArtistAlbums(long artistId) {
			final String[] projection = new String[] {
					MediaStore.Audio.Albums._ID,
					MediaStore.Audio.Albums.ALBUM,
					MediaStore.Audio.Albums.ARTIST,
					MediaStore.Audio.Albums.FIRST_YEAR,
					MediaStore.Audio.Albums.LAST_YEAR,
					MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST,
					MediaStore.Audio.Albums.ALBUM_ART
			};
			
			return new CursorRequest(MediaStore.Audio.Artists.Albums.getContentUri("external", artistId),
					projection, null, null, SORT_ORDER);
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
		
		public static final CursorRequest REQUEST = new CursorRequest(URI, PROJECTION, 
				null, null, SORT_ORDER);
	}
	
	public static final class Playlists {
		public static final String _ID = MediaStore.Audio.Playlists._ID;
		public static final Uri URI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
		public static final String[] PROJECTION = new String[] {
			MediaStore.Audio.Playlists._ID,
			MediaStore.Audio.Playlists.NAME
		};
		public static final String SORT_ORDER = MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER;
		
		public static final CursorRequest REQUEST = new CursorRequest(URI, PROJECTION,
				null, null, SORT_ORDER);
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
		public static final String SELECTION = MediaStore.Audio.Media.IS_MUSIC + "=(?)";
		public static final String[] SELECTION_ARGS = new String[] { "1" };
		public static final String SORT_ORDER = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
		
		public static final CursorRequest REQUEST = new CursorRequest(URI, PROJECTION,
				SELECTION, SELECTION_ARGS, SORT_ORDER);
		
		public static CursorRequest getAlbumTracks(long albumId) {
			final String[] projection = new String[] {
					MediaStore.Audio.Media._ID, 
					MediaStore.Audio.Media.TITLE,
					MediaStore.Audio.Media.ARTIST_ID,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media.ALBUM_ID,
					MediaStore.Audio.Media.ALBUM,
					MediaStore.Audio.Media.DURATION,
					MediaStore.Audio.Media.TRACK,
					MediaStore.Audio.Media.YEAR,
					MediaStore.Audio.Media.ARTIST_KEY
			};
			final String selection = MediaStore.Audio.Media.ALBUM_ID + "=(?)";
			final String[] selectionArgs = new String[] { String.valueOf(albumId) };
			final String sortOrder = MediaStore.Audio.Media.TRACK;
			
			return new CursorRequest(URI, projection, selection, selectionArgs, sortOrder);
		}
		
		public static CursorRequest getArtistTracks(long artistId) {
			final String selection = MediaStore.Audio.Media.ARTIST_ID + "=(?)";
			final String[] selectionArgs = new String[] { String.valueOf(artistId) };
			final String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
			
			return new CursorRequest(URI, PROJECTION, selection, selectionArgs, sortOrder);
		}
		
		public static CursorRequest getPlaylistTracks(long playlistId) {
			final String[] projection = new String[] {
					MediaStore.Audio.Media._ID, 
					MediaStore.Audio.Media.TITLE,
					MediaStore.Audio.Media.ARTIST_ID,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media.ALBUM_ID,
					MediaStore.Audio.Media.ALBUM,
					MediaStore.Audio.Media.DURATION,
					MediaStore.Audio.Playlists.Members.AUDIO_ID
			};
			final String sortOrder = MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER;
			
			return new CursorRequest(MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
					projection, null, null, sortOrder);
		}
	}
}
