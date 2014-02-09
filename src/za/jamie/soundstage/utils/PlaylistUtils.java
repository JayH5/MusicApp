package za.jamie.soundstage.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.List;

import za.jamie.soundstage.models.Track;

import static android.provider.MediaStore.Audio.Playlists;

/**
 * Created by jamie on 2014/01/30.
 */
public final class PlaylistUtils {

    public static String findExistingPlaylist(ContentResolver resolver, String name) {
        Cursor cursor = findPlaylistsByName(resolver, name);
        String existingName = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                existingName = cursor.getString(0);
            }
            cursor.close();
        }
        return existingName;
    }

    public static Cursor findPlaylistsByName(ContentResolver resolver, String name) {
        return resolver.query(
                Playlists.EXTERNAL_CONTENT_URI,         // Uri
                new String[] { Playlists.NAME },        // Projection
                Playlists.NAME + "= ? COLLATE NOCASE",  // Selection
                new String[] { name },                  // SelectionArgs
                null);                                  // Sort Order
    }

    public static long createPlaylist(ContentResolver resolver, String name) {
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Audio.Playlists.NAME, name);
        Uri uri = resolver.insert(Playlists.EXTERNAL_CONTENT_URI, values);
        return uri != null ? ContentUris.parseId(uri) : 0;
    }

    public static Uri savePlaylistTracks(ContentResolver resolver, long playlistId,
                                          List<Track> tracks) {
        Uri uri = Playlists.Members.getContentUri("external", playlistId);
        int len = tracks.size();
        ContentValues[] values = new ContentValues[len];
        for (int i = 0; i < len; i++) {
            Track track = tracks.get(i);
            values[i] = new ContentValues(2);
            values[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, track.getId());
            values[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, i + 1);
        }
        resolver.bulkInsert(uri, values);

        return uri;
    }
}
