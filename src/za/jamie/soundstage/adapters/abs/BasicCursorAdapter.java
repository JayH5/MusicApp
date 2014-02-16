package za.jamie.soundstage.adapters.abs;

import android.content.Context;
import android.database.Cursor;
import android.widget.ResourceCursorAdapter;

import za.jamie.soundstage.adapters.interfaces.SearchableAdapter;

public abstract class BasicCursorAdapter extends ResourceCursorAdapter implements 
		SearchableAdapter {

	public BasicCursorAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor != null && newCursor != getCursor()) {
            getColumnIndices(newCursor);
		}
		return super.swapCursor(newCursor);
	}
	
	@Override
	public int getItemPosition(long itemId) {
		for (int i = 0, n = getCount(); i < n; i++) {
			if (getItemId(i) == itemId) {
				return i;
			}
		}
		return -1;
	}
	
	protected abstract void getColumnIndices(Cursor cursor);
	
}