package za.jamie.soundstage.models;

import java.util.List;

public class AlbumSummary {
	public final int numTracks;
	public final long duration;
	public final int firstYear;
	public final int lastYear;
	public final List<String> artists;
	public final List<Long> artistIds;
	
	public AlbumSummary(int numTracks, long duration, int firstYear, int lastYear, 
			List<String> artists, List<Long> artistIds) {
		this.numTracks = numTracks;
		this.duration = duration;
		this.firstYear = firstYear;
		this.lastYear = lastYear;
		this.artists = artists;
		this.artistIds = artistIds;
	}
}
