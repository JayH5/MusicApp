package com.jamie.play.models;

public class ArtistAlbum implements IdProvider {

	public final String album;
	public final long albumId;
	public final int numTracks;
	public final int firstYear;
	public final int lastYear;
	
	public ArtistAlbum (String album, long albumId, int numTracks, int firstYear, 
			int lastYear) {
		
		this.album = album;
		this.albumId = albumId;
		this.numTracks = numTracks;
		this.firstYear = firstYear;
		this.lastYear = lastYear;
	}

	@Override
	public long getId() {
		return albumId;
	}
}
