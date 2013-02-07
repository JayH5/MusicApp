package com.jamie.play.service;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

public class Track implements Parcelable {
	private long mId;
	private String mTitle;
    
	private long mArtistId;
	private String mArtist;
    
    private long mAlbumId;
    private String mAlbum;

    public Track(long id, String title, long artistId, String artist, 
    		long albumId, String album) {
        mId = id;
        mTitle = title;
        
        mArtistId = artistId;
        mArtist = artist;
        
        mAlbumId = albumId;
        mAlbum = album;
    }
    
    /**
     * Dummy constructor for creating comparison tracks
     * @param id
     */
    public Track(long id) {
    	mId = id;
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
    
    @Override
    public boolean equals(Object object) {
    	if (object instanceof Track) {
    		Track track = (Track) object;
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
	
	public static final Parcelable.Creator<Track> CREATOR
			= new Parcelable.Creator<Track>() {
		
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
		//Log.d("Track", "Creating from parcel...");
		mId = in.readLong();
		mTitle = in.readString();
		
		mArtistId = in.readLong();
		mArtist = in.readString();
		
		mAlbumId = in.readLong();
		mAlbum = in.readString();		
	}
}
