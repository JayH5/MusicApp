package za.jamie.soundstage.adapters.abs;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public abstract class BasicTrackAdapter extends BasicCursorAdapter {

	private int mIdColIdx;
	private int mTitleColIdx;
	private int mArtistIdColIdx;
	private int mArtistColIdx;
	private int mAlbumIdColIdx;
	private int mAlbumColIdx;
	private int mDurationColIdx;

	public BasicTrackAdapter(Context context, int layout,
			Cursor c, int flags) {
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

	public int getDurationColIdx() {
		return mDurationColIdx;
	}

	@Override
	protected void getColumnIndices(Cursor cursor) {
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
		mDurationColIdx = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
	}

}