package za.jamie.soundstage.adapters.abs;

import java.util.LinkedList;
import java.util.List;

import za.jamie.soundstage.adapters.interfaces.TrackAdapter;
import za.jamie.soundstage.models.Track;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public abstract class BasicTrackAdapter extends BasicCursorAdapter 
		implements TrackAdapter {
	
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
	
	@Override
	public List<Track> getTrackList() {
		Cursor cursor = getCursor();
		List<Track> trackList = null;
		if (cursor != null && cursor.moveToFirst()) {
			trackList = new LinkedList<Track>();
			do {
				trackList.add(new Track(
						cursor.getLong(mIdColIdx),
						cursor.getString(mTitleColIdx),
						cursor.getLong(mArtistIdColIdx),
						cursor.getString(mArtistColIdx),
						cursor.getLong(mAlbumIdColIdx),
						cursor.getString(mAlbumColIdx),
						cursor.getLong(mDurationColIdx)));
			} while (cursor.moveToNext());
		}
		return trackList;
	}
	
	@Override
	public Track getTrack(int position) {
		Cursor cursor = getCursor();
		Track track = null;
		if (cursor != null && cursor.moveToPosition(position)) {
			track = new Track(cursor.getLong(mIdColIdx),
					cursor.getString(mTitleColIdx),
					cursor.getLong(mArtistIdColIdx),
					cursor.getString(mArtistColIdx),
					cursor.getLong(mAlbumIdColIdx),
					cursor.getString(mAlbumColIdx),
					cursor.getLong(mDurationColIdx));
		}
		return track;
	}
	
}