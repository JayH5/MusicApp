package za.jamie.soundstage.models;


public class Artist implements Comparable<Artist> {	
	private final String mArtistKey;
	private final String mArtist;
	private final long mArtistId;
	
	public Artist(String key, long artistId, String artist) {
		mArtistKey = key;
		mArtist = artist;
		mArtistId = artistId;
	}
	
	public String getKey() {
		return mArtistKey;
	}
	
	public long getId() {
		return mArtistId;
	}

	@Override
	public int compareTo(Artist other) {
		return mArtistKey.compareTo(other.getKey());
	}
	
	@Override
	public String toString() {
		return mArtist;
	}
}
