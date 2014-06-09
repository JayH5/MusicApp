package za.jamie.soundstage.providers;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;

import static android.provider.MediaStore.Audio.Albums;
import static android.provider.MediaStore.Audio.Artists;
import static android.provider.MediaStore.Audio.Media;
import static android.provider.MediaStore.Audio.Playlists;

public final class MusicLoaders {

    public static Loader<Cursor> albums(Context context) {
        final String[] projection = {
                Albums._ID,
                Albums.ALBUM,
                Albums.ARTIST,
                Albums.ALBUM_ART
        };
        return new CursorLoader(context, Albums.EXTERNAL_CONTENT_URI, projection, null, null,
                Albums.DEFAULT_SORT_ORDER);
    }

    public static Loader<Cursor> artists(Context context) {
        final String[] projection = {
                Artists._ID,
                Artists.ARTIST,
                Artists.NUMBER_OF_ALBUMS,
                Artists.NUMBER_OF_TRACKS
        };
        return new CursorLoader(context, Artists.EXTERNAL_CONTENT_URI, projection, null, null,
                Artists.DEFAULT_SORT_ORDER);
    }

    public static Loader<Cursor> artistAlbums(Context context, long artistId) {
        final String[] projection = {
                Albums._ID,
                Albums.ALBUM,
                Albums.ARTIST,
                Albums.FIRST_YEAR,
                Albums.LAST_YEAR,
                Albums.NUMBER_OF_SONGS_FOR_ARTIST
        };

        return new CursorLoader(context, Artists.Albums.getContentUri("external", artistId),
                projection, null, null, Albums.DEFAULT_SORT_ORDER);
    }

    public static Loader<Cursor> playlists(Context context) {
        final String[] projection = {
                Playlists._ID,
                Playlists.NAME
        };

        return new CursorLoader(context, Playlists.EXTERNAL_CONTENT_URI, projection, null, null,
                Playlists.DEFAULT_SORT_ORDER + " COLLATE NOCASE");
    }

    public static Loader<Cursor> songs(Context context) {
        final String[] projection = {
                Media._ID,
                Media.TITLE,
                Media.ARTIST_ID,
                Media.ARTIST,
                Media.ALBUM_ID,
                Media.ALBUM,
                Media.DURATION
        };
        final String selection = Media.IS_MUSIC + "=1";

        return new CursorLoader(context, Media.EXTERNAL_CONTENT_URI, projection, selection, null,
                Media.DEFAULT_SORT_ORDER);
    }

    public static Loader<Cursor> artistSongs(Context context, long artistId) {
        final String[] projection = {
                Media._ID,
                Media.TITLE,
                Media.ARTIST_ID,
                Media.ARTIST,
                Media.ALBUM_ID,
                Media.ALBUM,
                Media.DURATION
        };
        final String selection = Media.ARTIST_ID + "=" + artistId;

        return new CursorLoader(context, Media.EXTERNAL_CONTENT_URI, projection, selection, null,
                Media.DEFAULT_SORT_ORDER);
    }

    public static Loader<Cursor> albumSongs(Context context, long albumId) {
        final String[] projection = {
                Media._ID,
                Media.TITLE,
                Media.ARTIST_ID,
                Media.ARTIST,
                Media.ALBUM_ID,
                Media.ALBUM,
                Media.DURATION,
                Media.YEAR,
                Media.TRACK
        };
        final String selection = Media.ALBUM_ID + "=" + albumId;

        return new CursorLoader(context, Media.EXTERNAL_CONTENT_URI, projection, selection, null,
                Media.TRACK);
    }

    public static Loader<Cursor> playlistSongs(Context context, long playlistId) {
        final String[] projection = {
                Media._ID,
                Media.TITLE,
                Media.ARTIST_ID,
                Media.ARTIST,
                Media.ALBUM_ID,
                Media.ALBUM,
                Media.DURATION,
                Playlists.Members.AUDIO_ID
        };

        return new CursorLoader(context, Playlists.Members.getContentUri("external", playlistId),
                projection, null, null, Playlists.Members.PLAY_ORDER);
    }

}
