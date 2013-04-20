package za.jamie.soundstage.models;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class AlbumStatistics {
	public final String title;
	public final int numTracks;
	public final long duration;
	public final int firstYear;
	public final int lastYear;
	public final List<Artist> artists;
	
	public AlbumStatistics(String title, int numTracks, long duration, 
			int firstYear, int lastYear, List<Artist> artists) {
		
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
		private String title;
		private int numTracks;
		private long duration = 0;
		private int firstYear = Integer.MAX_VALUE;
		private int lastYear = Integer.MIN_VALUE;
		private final SortedSet<Artist> artists = new TreeSet<Artist>();
		
		public Builder() { }
		
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
		
		public Builder addArtist(Artist artist) {
			artists.add(artist);
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
			
			List<Artist> artistList = new ArrayList<Artist>(artists.size());
			for (Artist artist : artists) {
				artistList.add(artist);
			}
			
			return new AlbumStatistics(title, numTracks, duration, 
					firstYear, lastYear, artistList);
		}
	}
}
