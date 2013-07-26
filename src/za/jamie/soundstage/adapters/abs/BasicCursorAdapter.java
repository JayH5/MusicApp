package za.jamie.soundstage.adapters.abs;

import za.jamie.soundstage.adapters.interfaces.SearchableAdapter;
import android.content.Context;
import android.database.Cursor;
import android.widget.ResourceCursorAdapter;

public abstract class BasicCursorAdapter extends ResourceCursorAdapter implements 
		SearchableAdapter {

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
	
	@Override
	public int getItemPosition(long itemId) {
		final int len = getCount();
		for (int i = 0; i < len; i++) {
			if (getItemId(i) == itemId) {
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