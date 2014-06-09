package za.jamie.soundstage.models;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;


import static android.provider.MediaStore.Audio.Media;

/**
 * Container class for track metadata.
 */
public final class Track implements Parcelable {
	private final long mId;
	private final String mTitle;

	private final long mArtistId;
	private final String mArtist;

    private final long mAlbumId;
    private final String mAlbum;

    public Track(long id, String title, long artistId, String artist, long albumId, String album) {
        mId = id;
        mTitle = title;

        mArtistId = artistId;
        mArtist = artist;

        mAlbumId = albumId;
        mAlbum = album;
    }

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public long getArtistId() {
    	return mArtistId;
    }

    public String getArtist() {
        return mArtist;
    }

    public long getAlbumId() {
    	return mAlbumId;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public Uri getUri() {
        return ContentUris.withAppendedId(
        		MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mId);
    }

    public Uri getArtistUri() {
    	return ContentUris.withAppendedId(
    			MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, mArtistId);
    }

    public Uri getAlbumUri() {
    	return ContentUris.withAppendedId(
    			MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, mAlbumId);
    }

    /**
     * Writes the contents of the track to a ContentValues key-value store using the
     * column keys used in MediaStore.Audio.Media
     * @param out the ContentValues object to write to
     * @return the id of the track
     */
    public long writeToContentValues(ContentValues out) {
    	out.put(Media.TITLE, mTitle);
    	out.put(Media.ARTIST_ID, mArtistId);
    	out.put(Media.ARTIST, mArtist);
    	out.put(Media.ALBUM_ID, mAlbumId);
    	out.put(Media.ALBUM, mAlbum);

    	return mId;
    }

    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof Track) {
    		Track track = (Track) obj;
    		return track.getId() == mId;
    	}
    	return false;
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(mId);
		out.writeString(mTitle);

		out.writeLong(mArtistId);
		out.writeString(mArtist);

		out.writeLong(mAlbumId);
		out.writeString(mAlbum);
	}

	public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
		@Override
		public Track createFromParcel(Parcel in) {
			if (in != null) {
				return new Track(in);
			}
			return null;
		}

		@Override
		public Track[] newArray(int size) {
			return new Track[size];
		}
	};

	private Track(Parcel in) {
		mId = in.readLong();
		mTitle = in.readString();

		mArtistId = in.readLong();
		mArtist = in.readString();

		mAlbumId = in.readLong();
		mAlbum = in.readString();
	}

}
