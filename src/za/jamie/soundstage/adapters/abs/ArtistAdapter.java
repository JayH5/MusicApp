package za.jamie.soundstage.adapters.abs;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.ResourceCursorAdapter;

public abstract class ArtistAdapter extends ResourceCursorAdapter {

	private int mIdColIdx;
	private int mArtistColIdx;
	
	public ArtistAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}
	
	protected void getColumnIndices(Cursor cursor) {
		if (cursor != null) {
			mIdColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
			mArtistColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
		}
	}
	
	public int getIdColIdx() {
		return mIdColIdx;
	}
	
	public int getArtistColIdx() {
		return mArtistColIdx;
	}
	
	@Override
	public Cursor swapCursor(Cursor newCursor) {
		getColumnIndices(newCursor);
		return super.swapCursor(newCursor);
	}

}
