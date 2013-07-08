package za.jamie.soundstage.musicstore;

import za.jamie.soundstage.musicstore.CursorManager.CursorRequest;
import android.net.Uri;
import android.provider.MediaStore;


public final class MusicStore {
	
	public static class Albums {
		public static final String _ID = MediaStore.Audio.Albums._ID;
		public static final Uri URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
		public static final String[] PROJECTION = new String[] {
			MediaStore.Audio.Albums._ID,
			MediaStore.Audio.Albums.ALBUM,
			MediaStore.Audio.Albums.ARTIST
		};
		public static final CursorRequest CURSOR = new CursorRequest(URI)
				.addProjection(PROJECTION)
				.setSortOrder(MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
	}
	
	public static class Artists {
		public static final String _ID = MediaStore.Audio.Artists._ID;
		public static final Uri URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
		public static final String[] PROJECTION = new String[] {
			MediaStore.Audio.Artists._ID,
			MediaStore.Audio.Artists.ARTIST,
			MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
			MediaStore.Audio.Artists.NUMBER_OF_TRACKS
		};
		public static final CursorRequest CURSOR = new CursorRequest(URI)
				.addProjection(PROJECTION)
				.setSortOrder(MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
		
		public static class Albums {
			public static final String _ID = MediaStore.Audio.Artists.Albums.ALBUM_ID;
			public static String[] PROJECTION = new String[] {
				MediaStore.Audio.Artists.Albums.ALBUM_ID,
				MediaStore.Audio.Artists.Albums.ALBUM,
				MediaStore.Audio.Artists.Albums.FIRST_YEAR,
				MediaStore.Audio.Artists.Albums.LAST_YEAR,
				MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS_FOR_ARTIST
			};
			public static final String SORT_ORDER = MediaStore.Audio.Artists.Albums.ALBUM_KEY;
			public static CursorRequest getItem(long id) {
				return new CursorRequest(MediaStore.Audio.Artists.Albums.getContentUri("external", id))
						.addProjection(PROJECTION)
						.setSortOrder(SORT_ORDER);
			}
		}
	}
	
	public static class Playlists {
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
		
		public static class Members {
			public static final String _ID = MediaStore.Audio.Playlists.Members._ID;
			public static String[] PROJECTION = new String[] {
				MediaStore.Audio.Playlists.Members.AUDIO_ID,
				MediaStore.Audio.Playlists.Members.TITLE,
				MediaStore.Audio.Playlists.Members.ARTIST,
				MediaStore.Audio.Playlists.Members.ARTIST_ID,
				MediaStore.Audio.Playlists.Members.ALBUM,
				MediaStore.Audio.Playlists.Members.ALBUM_ID,
				MediaStore.Audio.Playlists.Members.DURATION
			};
			public static final String SORT_ORDER = MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER;
			public static CursorRequest getItem(long id) {
				return new CursorRequest(MediaStore.Audio.Playlists.Members.getContentUri("external", id))
						.setProjection(PROJECTION)
						.setSortOrder(SORT_ORDER);
			}
		}
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
				.setProjection(PROJECTION)
				.setSelection(SELECTION)
				.setSortOrder(SORT_ORDER);
		
		public static CursorRequest getAlbumTracks(long albumId) {
			return CURSOR.addProjection(MediaStore.Audio.Media.ARTIST_KEY)
					.setSelection(MediaStore.Audio.Media.ALBUM_ID + "=" + albumId)
					.setSortOrder(MediaStore.Audio.Media.TRACK);
		}
		
		public static CursorRequest getArtistTracks(long artistId) {
			return CURSOR.setSelection(MediaStore.Audio.Media.ARTIST_ID + "=" + artistId)
					.setSortOrder(MediaStore.Audio.Media.TITLE);
		}
	}
}
