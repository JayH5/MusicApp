package za.jamie.soundstage.adapters.abs;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;

public abstract class BasicCursorAdapter extends ResourceCursorAdapter {

	public BasicCursorAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor != null) {
			onCursorLoad(newCursor);
		}
		return super.swapCursor(newCursor);
	}
	
	/**
	 * Returns the position of the item specified by an id in the Cursor.
	 * @param id The id of the item to retrieve the position of.
	 * @return The position of the item with the specified id.
	 */
	public int getPosition(long id) {
		final int len = getCount();
		for (int i = 0; i < len; i++) {
			if (getItemId(i) == id) {
				return i;
			}
		}
		return -1;
	}
	
	protected void onCursorLoad(Cursor cursor) {
		getColumnIndices(cursor);
	}
	
	protected abstract void getColumnIndices(Cursor cursor);
	
}
