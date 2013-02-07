package com.jamie.play.adapters;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.ResourceCursorAdapter;

public abstract class AlbumAdapter extends ResourceCursorAdapter {

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
	
	protected void getColumnIndices(Cursor cursor) {
		if (cursor != null) {
			mIdColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
			mAlbumColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
			mArtistColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
		}
	}
	
	@Override
	public Cursor swapCursor(Cursor newCursor) {
		getColumnIndices(newCursor);
		return super.swapCursor(newCursor);
	}

}
