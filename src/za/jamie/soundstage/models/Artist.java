package za.jamie.soundstage.models;

public class Artist {

	public final String mArtist;
	public final long mArtistId;
	public final int mNumTracks;
	public final int mNumAlbums;
	
	public Artist(String artist, long artistId, int numTracks, int numAlbums) {
		mArtist = artist;
		mArtistId = artistId;
		mNumTracks = numTracks;
		mNumAlbums = numAlbums;
	}	
	
}
