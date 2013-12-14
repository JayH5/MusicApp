package za.jamie.soundstage.models;

import java.util.SortedMap;
import java.util.TreeMap;

public class AlbumStatistics {
	public final long id;
	public final String title;
	public final int numTracks;
	public final long duration;
	public final int firstYear;
	public final int lastYear;
	public final SortedMap<MusicItem, Integer> artists;
	
	public AlbumStatistics(long id, String title, int numTracks, long duration, 
			int firstYear, int lastYear, SortedMap<MusicItem, Integer> artists) {
		
		this.id = id;
		this.title = title;
		this.numTracks = numTracks;
		this.duration = duration;
		this.firstYear = firstYear;
		this.lastYear = lastYear;
		this.artists = artists;
	}
	
	public boolean isCompilation() {
		return artists.size() > 1;
	}
	
	public static class Builder {
		private final long id;
		private String title;
		private int numTracks;
		private long duration = 0;
		private int firstYear = Integer.MAX_VALUE;
		private int lastYear = Integer.MIN_VALUE;
		private final SortedMap<MusicItem, Integer> artists = new TreeMap<MusicItem, Integer>();
		
		public Builder(long albumId) {
			id = albumId;
		}
		
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}
		
		public Builder setNumTracks(int numTracks) {
			this.numTracks = numTracks;
			return this;
		}
		
		public Builder addYear(int year) {
			firstYear = Math.min(firstYear, year);
			lastYear = Math.max(lastYear, year);
			return this;
		}
		
		public Builder addArtist(long id, String name) {
			final MusicItem artist = new MusicItem(id, name, MusicItem.TYPE_ARTIST);
			Integer count = artists.get(artist);
			if (count == null) {
				count = 0;
			}
			artists.put(artist, count + 1);
			return this;
		}
		
		public Builder addDuration(long duration) {
			this.duration += duration;
			return this;
		}
		
		public AlbumStatistics create() {
			if (firstYear == Integer.MAX_VALUE) {
				firstYear = 0;
				lastYear = 0;
			}
			
			return new AlbumStatistics(id, title, numTracks, duration, 
					firstYear, lastYear, artists);
		}
	}

}
