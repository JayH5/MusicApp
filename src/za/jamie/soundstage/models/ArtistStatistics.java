package za.jamie.soundstage.models;

import java.util.Set;
import java.util.TreeSet;

public class ArtistStatistics {
	
	private final String mTitle;
	private final int mNumAlbums;
	private final int mNumTracks;
	private final long mDuration;
	
	public ArtistStatistics(String title, int numAlbums, int numTracks, long duration) {
		mTitle = title;
		mNumAlbums = numAlbums;
		mNumTracks = numTracks;
		mDuration = duration;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public int getNumAlbums() {
		return mNumAlbums;
	}
	
	public int getNumTracks() {
		return mNumTracks;
	}
	
	public long getDuration() {
		return mDuration;
	}
	
	public static class Builder {
		private String title;
		private int numTracks;
		private long duration = 0;
		private final Set<Long> albumSet = new TreeSet<Long>();
		
		public Builder() {
			
		}
		
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}
		
		public Builder setNumTracks(int numTracks) {
			this.numTracks = numTracks;
			return this;
		}
		
		public Builder addAlbum(long albumId) {
			albumSet.add(albumId);
			return this;
		}
		
		public Builder addDuration(long duration) {
			this.duration += duration;
			return this;
		}
		
		public ArtistStatistics create() {
			return new ArtistStatistics(title, albumSet.size(), numTracks, duration);
		}
	}
}
