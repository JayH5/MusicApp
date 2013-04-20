package za.jamie.soundstage.adapters.abs;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public abstract class AlbumAdapter extends BasicCursorAdapter {

	private int mIdColIdx;
	private int mAlbumColIdx;
	private int mArtistColIdx;
	
	public AlbumAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}
	
	public int getIdColIdx() {
		return mIdColIdx;
	}
	
	public int getAlbumColIdx() {
		return mAlbumColIdx;
	}
	
	public int getArtistColIdx() {
		return mArtistColIdx;
	}
	
	@Override
	protected void getColumnIndices(Cursor cursor) {
		mIdColIdx = cursor.getColumnIndexOrThrow(
				MediaStore.Audio.Albums._ID);
		mAlbumColIdx = cursor.getColumnIndexOrThrow(
				MediaStore.Audio.Albums.ALBUM);
		mArtistColIdx = cursor.getColumnIndexOrThrow(
				MediaStore.Audio.Albums.ARTIST);
	}

}
