package za.jamie.soundstage.providers;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.models.Track;

import static android.provider.MediaStore.Audio.Media;
import static android.provider.MediaStore.Audio.Playlists.Members;

/**
 * Created by jamie on 2014/01/31.
 */
public final class MusicItemStore {
    private static final Uri URI = Media.EXTERNAL_CONTENT_URI;
    private static final String[] PROJECTION = {
            Media._ID,
            Media.TITLE,
            Media.ARTIST_ID,
            Media.ARTIST,
            Media.ALBUM_ID,
            Media.ALBUM,
            Media.DURATION
    };

    private static final String ALBUM_SELECTION = Media.ALBUM_ID + "=?";
    private static final String ARTIST_SELECTION = Media.ARTIST_ID + "=?";

    public static List<Track> fetchMusicItem(ContentResolver resolver, MusicItem item) {
        Uri uri = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        PROJECTION[0] = Media._ID;
        switch (item.getType()) {
            case MusicItem.TYPE_TRACK:
                uri = ContentUris.withAppendedId(URI, item.getId());
                break;
            case MusicItem.TYPE_ARTIST:
                uri = URI;
                selection = ARTIST_SELECTION;
                selectionArgs = new String[] { String.valueOf(item.getId()) };
                sortOrder = Media.TITLE_KEY;
                break;
            case MusicItem.TYPE_ALBUM:
                uri = URI;
                selection = ALBUM_SELECTION;
                selectionArgs = new String[] { String.valueOf(item.getId()) };
                sortOrder = Media.TRACK;
                break;
            case MusicItem.TYPE_PLAYLIST:
                uri = MediaStore.Audio.Playlists.Members.getContentUri("external", item.getId());
                PROJECTION[0] = Members.AUDIO_ID;
                sortOrder = Members.PLAY_ORDER;
                break;
        }
        final Cursor cursor = resolver.query(uri, PROJECTION, selection, selectionArgs, sortOrder);

        return buildTrackList(cursor);
    }

    public static List<Track> fetchAllMusic(ContentResolver resolver) {
        final String selection = Media.IS_MUSIC + "=?";
        final String[] selectionArgs = { "1" };
        final String sortOrder = Media.DEFAULT_SORT_ORDER;

        Cursor cursor = resolver.query(URI, PROJECTION, selection, selectionArgs, sortOrder);
        return buildTrackList(cursor);
    }

    private static List<Track> buildTrackList(Cursor cursor) {
        List<Track> trackList = null;
        if (cursor.moveToFirst()) {
            trackList = new ArrayList<Track>();
            do {
                trackList.add(new Track(
                        cursor.getLong(0), // Track id
                        cursor.getString(1), // Title
                        cursor.getLong(2), // Artist id
                        cursor.getString(3), // Artist
                        cursor.getLong(4), // Album id
                        cursor.getString(5))); // Album
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trackList;
    }
}
