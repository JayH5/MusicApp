package za.jamie.soundstage.adapters;

import za.jamie.soundstage.R;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;

public class PlaylistsAdapter extends ResourceCursorAdapter {

	private int mIdColIdx;
	private int mNameColIdx;
	
	public PlaylistsAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TextView nameText = (TextView) view.findViewById(R.id.title);
		
		nameText.setText(cursor.getString(mNameColIdx));
	}
	
	@Override
	public Cursor swapCursor(Cursor newCursor) {
		getColumnIndices(newCursor);
		return super.swapCursor(newCursor);
	}
	
	protected void getColumnIndices(Cursor cursor) {
		if (cursor != null) {
			mIdColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID);
			mNameColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME);
		}
	}
	
	public int getIdColIdx() {
		return mIdColIdx;
	}

}
