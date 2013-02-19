package com.jamie.play.models;

public class ArtistSummary {
	
	public final int numAlbums;
	public final int numTracks;
	public final long duration;
	
	public ArtistSummary(int numAlbums, int numTracks, long duration) {
		this.numAlbums = numAlbums;
		this.numTracks = numTracks;
		this.duration = duration;
	}
}
