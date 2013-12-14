package za.jamie.soundstage.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

public final class MusicItem implements Parcelable, Comparable<MusicItem> {

	public static final int TYPE_TRACK = 1;
	public static final int TYPE_ARTIST = 2;
	public static final int TYPE_ALBUM = 3;
	public static final int TYPE_PLAYLIST = 4;
	
	private final long mId;
	private final String mTitle;
	private final int mType;
	
	public MusicItem(long id, String title, int type) {
		mId = id;
		mTitle = title;
		mType = type;
	}
	
	public long getId() {
		return mId;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public int getType() {
		return mType;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		dest.writeString(mTitle);
		dest.writeInt(mType);
	}
	
	public static final Parcelable.Creator<MusicItem> CREATOR
			= new Parcelable.Creator<MusicItem>() {
		
		@Override
		public MusicItem createFromParcel(Parcel in) {
			if (in != null) {
				return new MusicItem(in);
			} 
			return null;
		}
		
		@Override
		public MusicItem[] newArray(int size) {
			return new MusicItem[size];
		}
	};
	
	private MusicItem(Parcel in) {
		mId = in.readLong();
		mTitle = in.readString();
		mType = in.readInt();
	}

	@Override
	public int compareTo(MusicItem another) {
		final String thisKey = MediaStore.Audio.keyFor(mTitle);
		final String anotherKey = MediaStore.Audio.keyFor(another.mTitle);
		return thisKey.compareTo(anotherKey);
	}
	
	@Override
	public String toString() {
		return mTitle;
	}
	
	@Override
	public boolean equals(Object o) {
		boolean equal = false;
		if (o instanceof MusicItem) {
			MusicItem item = (MusicItem) o;
			equal = (item.mType == mType && item.mId == mId);
		}	
		
		return equal;		
	}
	
	@Override
	public int hashCode() {
		return ((Long) mId).hashCode();
	}
}
