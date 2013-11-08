package za.jamie.soundstage.models;

import android.os.Parcel;
import android.os.Parcelable;

public final class MusicItem implements Parcelable {

	public static final int TYPE_TRACK = 1;
	public static final int TYPE_ARTIST = 2;
	public static final int TYPE_ALBUM = 3;
	public static final int TYPE_PLAYLIST = 4;
	
	public final long id;
	public final String title;
	public final int type;
	
	public MusicItem(long id, String title, int type) {
		this.id = id;
		this.title = title;
		this.type = type;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(title);
		dest.writeInt(type);
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
		id = in.readLong();
		title = in.readString();
		type = in.readInt();
	}
}
