package com.jamie.play.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ResourceCursorAdapter;

import com.jamie.play.service.Track;

public abstract class TrackAdapter extends ResourceCursorAdapter {

	private static final String TAG = "TrackAdapter";
	
	private int mIdColIdx;
	private int mTitleColIdx;
	private int mArtistIdColIdx;
	private int mArtistColIdx;
	private int mAlbumIdColIdx;
	private int mAlbumColIdx;
	
	public TrackAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}
	
	public int getIdColIdx() {
		return mIdColIdx;
	}
	
	public int getTitleColIdx() {
		return mTitleColIdx;
	}
	
	public int getArtistIdColIdx() {
		return mArtistIdColIdx;
	}
	
	public int getArtistColIdx() {
		return mArtistColIdx;
	}
	
	public int getAlbumIdColIdx() {
		return mAlbumIdColIdx;
	}
	
	public int getAlbumColIdx() {
		return mAlbumColIdx;
	}
	
	protected void getColumnIndices(Cursor cursor) {
		if (cursor != null) {
			mIdColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
			mTitleColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
			mArtistIdColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
			mArtistColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
			mAlbumIdColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
			mAlbumColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
		}
	}
	
	public List<Track> getTrackList() {
		Cursor cursor = getCursor();
		List<Track> trackList = null;
		if (cursor != null & cursor.moveToFirst()) {
			trackList = new ArrayList<Track>(cursor.getCount());
			do {
				Log.d(TAG, "Adding track: " + cursor.getString(mTitleColIdx));
				trackList.add(new Track(
						cursor.getLong(mIdColIdx),
						cursor.getString(mTitleColIdx),
						cursor.getLong(mArtistIdColIdx),
						cursor.getString(mArtistColIdx),
						cursor.getLong(mAlbumIdColIdx),
						cursor.getString(mAlbumColIdx)));
			} while (cursor.moveToNext());
		}
		return trackList;
	}
	
	@Override
	public Cursor swapCursor(Cursor newCursor) {
		getColumnIndices(newCursor);
		return super.swapCursor(newCursor);
	}
	
}