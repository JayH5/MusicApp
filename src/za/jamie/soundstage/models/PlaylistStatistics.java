package za.jamie.soundstage.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by jamie on 2014/02/17.
 */
public class PlaylistStatistics {

    public final SortedSet<Album> albums;
    public final SortedSet<MusicItem> artists;
    public final int numTracks;
    public final long duration;

    private PlaylistStatistics(SortedSet<Album> albums, SortedSet<MusicItem> artists,
                               int numTracks, long duration) {
        this.albums = albums;
        this.artists = artists;
        this.numTracks = numTracks;
        this.duration = duration;
    }

    public static class Builder {
        private int numTracks;
        private SortedSet<MusicItem> artists = new TreeSet<MusicItem>();
        private Map<Long, Album> albums = new HashMap<Long, Album>();
        private long duration;

        public Builder() {
        }

        public Builder setNumTracks(int numTracks) {
            this.numTracks = numTracks;
            return this;
        }

        public Builder addArtist(long id, String title) {
            artists.add(new MusicItem(id, title, MusicItem.TYPE_ARTIST));
            return this;
        }

        public Builder addAlbum(long id, String title, String artist) {
            Album album = albums.get(id);
            if (album != null) {
                album.incrementFrequency();
            } else {
                album = new Album(id, title, artist);
                albums.put(id, album);
            }
            return this;
        }

        public Builder addDuration(long duration) {
            this.duration += duration;
            return this;
        }

        public PlaylistStatistics create() {
            SortedSet<Album> sortedAlbums = new TreeSet<Album>(albums.values());
            return new PlaylistStatistics(sortedAlbums, artists, numTracks, duration);
        }
    }

    public static class Album implements Comparable<Album> {
        public final long id;
        public final String title;
        public final String artist;
        private int frequency = 1;

        private Album(long id, String title, String artist) {
            this.id = id;
            this.title = title;
            this.artist = artist;
        }

        private void incrementFrequency() {
            frequency++;
        }

        public int getFrequency() {
            return frequency;
        }

        @Override
        public int compareTo(Album another) {
            return ((Integer) frequency).compareTo(another.frequency);
        }

        @Override
        public boolean equals(Object rhs) {
            if (!(rhs instanceof Album)) {
                return false;
            }

            Album album = (Album) rhs;
            return id == album.id;
        }
    }
}
