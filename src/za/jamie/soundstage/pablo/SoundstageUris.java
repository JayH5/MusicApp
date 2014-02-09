package za.jamie.soundstage.pablo;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.models.Track;

/**
 * Created by jamie on 2014/01/29.
 */
public final class SoundstageUris {
    private static final Uri BASE_URI = Uri.parse("soundstage://");

    public static Uri artistImage(long id, String artist) {
        return BASE_URI.buildUpon()
                .appendPath("artist")
                .appendPath(String.valueOf(id))
                .appendQueryParameter("artist", artist)
                .build();
    }

    public static Uri albumImage(long id, String album, String artist) {
        return BASE_URI.buildUpon()
                .appendPath("album")
                .appendPath(String.valueOf(id))
                .appendQueryParameter("album", album)
                .appendQueryParameter("artist", artist)
                .build();
    }

    public static Uri albumImage(Track track) {
        return albumImage(track.getAlbumId(), track.getAlbum(), track.getArtist());
    }

    public static Uri albumImage(AlbumStatistics stats) {
        return albumImage(stats.id, stats.title, stats.artists.firstKey().getTitle());
    }

}
