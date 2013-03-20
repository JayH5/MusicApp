package za.jamie.soundstage.models;

public class ArtistAlbum {

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
}
