package za.jamie.soundstage.pablo;

import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.models.Track;
import android.net.Uri;

public final class LastfmUris {

	private static final String LAST_FM_API_KEY = "5221fc4823ffde4ec61f8aef509d247c";
	private static final String BASE_PATH = "http://ws.audioscrobbler.com/2.0/";
	private static final String ARTIST_AUTOCORRECT = "1";
	private static final String ALBUM_AUTOCORRECT = "1";
	
	public static Uri getArtistInfoUri(String artist) {
		Uri.Builder builder = new Uri.Builder();
		builder.encodedPath(BASE_PATH)
			.appendQueryParameter("method", "artist.getinfo")
			.appendQueryParameter("artist", artist)
			.appendQueryParameter("autocorrect", ARTIST_AUTOCORRECT)
			.appendQueryParameter("api_key", LAST_FM_API_KEY);
		
		return builder.build();
	}
	
	public static Uri getAlbumInfoUri(String album, String artist, long id) {
		Uri.Builder builder = new Uri.Builder();
		builder.encodedPath(BASE_PATH)
			.appendQueryParameter("method", "album.getinfo")
			.appendQueryParameter("album", album)
			.appendQueryParameter("artist", artist)
			.appendQueryParameter("autocorrect", ALBUM_AUTOCORRECT)
			.appendQueryParameter("api_key", LAST_FM_API_KEY)
			.appendQueryParameter("_id", String.valueOf(id));
		
		return builder.build();
	}
	
	public static Uri getAlbumInfoUri(Track track) {
		return getAlbumInfoUri(track.getAlbum(), track.getArtist(), track.getAlbumId());
	}
	
	public static Uri getAlbumInfoUri(AlbumStatistics stats) {
		return getAlbumInfoUri(stats.title, stats.artists.firstKey().getName(), stats.id);
	}

}
